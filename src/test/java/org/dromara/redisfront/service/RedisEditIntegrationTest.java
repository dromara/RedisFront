package org.dromara.redisfront.service;

import io.lettuce.core.cluster.SlotHash;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import org.dromara.redisfront.commons.codec.*;
import org.dromara.redisfront.commons.enums.*;
import org.dromara.redisfront.commons.jsch.JschManager;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.info.LogStatusHolder;
import org.dromara.redisfront.ui.components.scanner.core.HashRedisDataScanner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in live Redis contract checks. All fixtures are uniquely namespaced and cleaned up. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "REDISFRONT_TEST_HOST", matches = ".+")
class RedisEditIntegrationTest {
    private final String prefix = "redisfront:safety:" + UUID.randomUUID() + ":";
    private final Set<String> keys = new LinkedHashSet<>();
    private RedisConnectContext context;
    private int sequence;
    private final RedisEditService edits = RedisEditService.service;

    @BeforeAll void connect() {
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
                .setLevel(ch.qos.logback.classic.Level.WARN);
        LogStatusHolder.ignoredLog();
        context = new RedisConnectContext();
        context.setId(ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE));
        context.setHost(System.getenv("REDISFRONT_TEST_HOST"));
        context.setPort(Integer.parseInt(env("REDISFRONT_TEST_PORT", "6379")));
        context.setUsername(env("REDISFRONT_TEST_USER", "default"));
        context.setPassword(System.getenv("REDISFRONT_TEST_PASSWORD"));
        context.setSetting(new RedisConnectContext.SettingInfo(1000, ":", 5000, 10000));
        context.setRedisMode(RedisMode.valueOf(env("REDISFRONT_TEST_MODE", "STANDALONE")));
        context.setConnectTypeMode(ConnectType.NORMAL);
        if (System.getenv("REDISFRONT_TEST_SSH_HOST") != null) {
            context.setConnectTypeMode(ConnectType.SSH);
            context.setSshInfo(new RedisConnectContext.SshInfo(null, env("REDISFRONT_TEST_SSH_USER", "root"),
                    System.getenv("REDISFRONT_TEST_SSH_HOST"), Integer.parseInt(env("REDISFRONT_TEST_SSH_PORT", "22")),
                    System.getenv("REDISFRONT_TEST_SSH_PASSWORD")));
            JschManager.MANAGER.openSession(context);
        }
        if (context.getRedisMode() == RedisMode.SENTINEL) {
            var master = LettuceUtils.sentinelExec(context, c -> c.masters()).getFirst();
            context.setHost(master.get("ip"));context.setPort(Integer.parseInt(master.get("port")));
            if (context.getConnectTypeMode() == ConnectType.SSH) JschManager.MANAGER.redirectSession(context);
        } else if (context.getRedisMode() == RedisMode.CLUSTER && context.getConnectTypeMode() == ConnectType.SSH) {
            JschManager.MANAGER.openClusterSession(context);
        }
        assertTrue(RedisBasicService.service.ping(context));
    }

    private static String env(String name, String fallback) { return Objects.requireNonNullElse(System.getenv(name), fallback); }
    private static byte[] b(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private String key(String suffix) { return own(prefix + "{" + (++sequence) + "}:" + suffix); }
    private String own(String key) { keys.add(key);return key; }
    private RedisEditService.Target target(KeyTypeEnum type, String key, byte[] value, String field, Double score, Long index) {
        return new RedisEditService.Target(type, key, value, field, score, index);
    }
    private void string(String key, byte[] value) { RedisStringService.service.set(context, key, value);expire(key); }
    private byte[] get(String key) { return RedisStringService.service.get(context, key); }
    private long ttl(String key) { return LettuceUtils.execByMode(context, c -> c.pttl(key), c -> c.pttl(key)); }
    private void expire(String key) { assertTrue(Boolean.TRUE.equals(LettuceUtils.execByMode(context, c -> c.pexpire(key, 60000), c -> c.pexpire(key, 60000)))); }
    private void keptTtl(String key, long before) { long after = ttl(key);assertTrue(after > 0 && after <= before, "TTL must remain expiring"); }
    private void failure(RedisEditService.Reason reason, Runnable operation) {
        assertEquals(reason, assertThrows(RedisEditService.EditException.class, operation::run).reason());
    }

    @BeforeEach void ignoreLogs() { LogStatusHolder.ignoredLog(); }
    @AfterEach void cleanupFixtures() {
        try { for (String key : keys) RedisBasicService.service.del(context, key); }
        finally { keys.clear();LogStatusHolder.clear(); }
    }
    @AfterAll void disconnect() {
        if (context != null) {
            if (context.getConnectTypeMode() == ConnectType.SSH) JschManager.MANAGER.closeSession(context);
            RedisConnectionPoolManager.cleanupContextPool(context);LettuceUtils.close(context);
        }
        LogStatusHolder.clear();
    }

    @Test void autoLiteralAndBinaryEditsPreserveExactBytes() {
        for (byte[] original : new byte[][]{b("  hello\r\n"), b("0x4142"), b("hex:4142"), b("base64:QUI="), new byte[]{0, 1, (byte) 0xff}}) {
            String key = key("codec");string(key, original);
            var display = RedisValueCodec.forEditing(original, ValueViewType.AUTO);
            edits.save(context, target(KeyTypeEnum.STRING, key, original, null, null, null),
                    RedisValueCodec.decode(display.text(), display.effectiveType()), "");
            assertArrayEquals(original, get(key));
        }
    }

    @Test void stringEditPreservesTtlAndRejectsConcurrentChanges() {
        String key = key("string");string(key, b("old"));long before = ttl(key);
        var original = target(KeyTypeEnum.STRING, key, b("old"), null, null, null);
        edits.save(context, original, b("new"), "");assertArrayEquals(b("new"), get(key));keptTtl(key, before);
        failure(RedisEditService.Reason.CONFLICT, () -> edits.save(context, original, b("stale"), ""));
        assertArrayEquals(b("new"), get(key));
        RedisBasicService.service.del(context, key);
        failure(RedisEditService.Reason.MISSING, () -> edits.save(context, original, b("resurrect"), ""));
        assertNull(get(key));
    }

    @Test void renameAndTtlChangesAreSafeAndDoNotOverwriteTargets() {
        String old = key("old"), renamed = own(old + ":renamed"), occupied = own(old + ":occupied");
        string(old, b("value"));string(occupied, b("keep"));long before = ttl(old);
        failure(RedisEditService.Reason.TARGET_EXISTS, () -> edits.updateMetadata(context, old, occupied, -1L));
        assertArrayEquals(b("value"), get(old));assertArrayEquals(b("keep"), get(occupied));keptTtl(old, before);
        edits.updateMetadata(context, old, renamed, null);assertNull(get(old));assertArrayEquals(b("value"), get(renamed));keptTtl(renamed, before);
        edits.updateMetadata(context, renamed, renamed, -1L);assertEquals(-1, ttl(renamed));
        for (long invalid : new long[]{0, -2, Long.MAX_VALUE}) {
            failure(RedisEditService.Reason.INVALID_TTL, () -> edits.updateMetadata(context, renamed, renamed, invalid));
            assertArrayEquals(b("value"), get(renamed));assertEquals(-1, ttl(renamed));
        }
        edits.updateMetadata(context, renamed, renamed, 120L);assertTrue(ttl(renamed) > 110_000);
    }

    @Test void hashLastFieldRenameKeepsTtlAndRejectsCollisions() {
        String key = key("hash");RedisHashService.service.hset(context, key, "old", b("value"));expire(key);long before = ttl(key);
        edits.save(context, target(KeyTypeEnum.HASH, key, b("value"), "old", null, null), b("changed"), "new");
        assertNull(RedisHashService.service.hget(context, key, "old"));
        assertArrayEquals(b("changed"), RedisHashService.service.hget(context, key, "new"));keptTtl(key, before);
        RedisHashService.service.hset(context, key, "occupied", b("keep"));
        failure(RedisEditService.Reason.TARGET_EXISTS, () -> edits.save(context,
                target(KeyTypeEnum.HASH, key, b("changed"), "new", null, null), b("overwrite"), "occupied"));
        assertArrayEquals(b("keep"), RedisHashService.service.hget(context, key, "occupied"));
        failure(RedisEditService.Reason.CONFLICT, () -> edits.save(context,
                target(KeyTypeEnum.HASH, key, b("stale"), "new", null, null), b("overwrite"), "new"));
    }

    @Test void repeatedSetReplacementKeepsExactlyOneMemberAndTheTtl() {
        String key = key("set");RedisSetService.service.sadd(context, key, b("old"));expire(key);long before = ttl(key);
        edits.save(context, target(KeyTypeEnum.SET, key, b("old"), null, null, null), b("new"), "");
        edits.save(context, target(KeyTypeEnum.SET, key, b("new"), null, null, null), b("third"), "");
        assertEquals(1L, RedisSetService.service.scard(context, key));assertTrue(RedisSetService.service.sismember(context, key, b("third")));keptTtl(key, before);
        failure(RedisEditService.Reason.CONFLICT, () -> edits.save(context, target(KeyTypeEnum.SET, key, b("old"), null, null, null), b("stale"), ""));
        RedisSetService.service.sadd(context, key, b("occupied"));
        failure(RedisEditService.Reason.TARGET_EXISTS, () -> edits.save(context, target(KeyTypeEnum.SET, key, b("third"), null, null, null), b("occupied"), ""));
        assertEquals(2L, RedisSetService.service.scard(context, key));
    }

    @Test void zsetLastMemberReplacementKeepsTtlAndValidatesTheScoreBeforeWriting() {
        String key = key("zset");RedisZSetService.service.zadd(context, key, 1, b("old"));expire(key);long before = ttl(key);
        edits.save(context, target(KeyTypeEnum.ZSET, key, b("old"), null, 1.0, null), b("new"), "7");
        assertNull(RedisZSetService.service.zscore(context, key, b("old")));assertEquals(7.0, RedisZSetService.service.zscore(context, key, b("new")));keptTtl(key, before);
        failure(RedisEditService.Reason.INVALID_SCORE, () -> edits.save(context, target(KeyTypeEnum.ZSET, key, b("new"), null, 7.0, null), b("bad"), "NaN"));
        failure(RedisEditService.Reason.CONFLICT, () -> edits.save(context, target(KeyTypeEnum.ZSET, key, b("new"), null, 1.0, null), b("bad"), "2"));
        assertEquals(1L, RedisZSetService.service.zcard(context, key));assertEquals(7.0, RedisZSetService.service.zscore(context, key, b("new")));
    }

    @Test void listEditPreservesPositionAndDuplicateElements() {
        String key = key("list");RedisListService.service.rpush(context, key, b("same"), b("middle"), b("same"));expire(key);long before = ttl(key);
        edits.save(context, target(KeyTypeEnum.LIST, key, b("same"), null, null, 2L), b("tail"), "");
        var values = RedisListService.service.lrange(context, key, 0, -1);
        assertEquals(3, values.size());assertArrayEquals(b("same"), values.get(0));assertArrayEquals(b("middle"), values.get(1));assertArrayEquals(b("tail"), values.get(2));keptTtl(key, before);
        failure(RedisEditService.Reason.CONFLICT, () -> edits.save(context, target(KeyTypeEnum.LIST, key, b("same"), null, null, 2L), b("stale"), ""));
    }

    @Test void changedKeyTypesAreRejectedWithoutDeletingTheCurrentValue() {
        for (KeyTypeEnum type : new KeyTypeEnum[]{KeyTypeEnum.HASH, KeyTypeEnum.SET, KeyTypeEnum.ZSET, KeyTypeEnum.LIST}) {
            String key = key("wrong-type");string(key, b("keep"));
            failure(RedisEditService.Reason.WRONG_TYPE, () -> edits.save(context, target(type, key, b("old"), "field", 1.0, 0L), b("new"), "2"));
            assertArrayEquals(b("keep"), get(key));
        }
    }

    @Test void addingCollectionItemsDoesNotOverwriteExistingFieldsOrMembers() {
        String hash = key("add-hash");edits.add(context, KeyTypeEnum.HASH, hash, b("old"), "field");expire(hash);
        failure(RedisEditService.Reason.TARGET_EXISTS, () -> edits.add(context, KeyTypeEnum.HASH, hash, b("new"), "field"));
        assertArrayEquals(b("old"), RedisHashService.service.hget(context, hash, "field"));
        String zset = key("add-zset");edits.add(context, KeyTypeEnum.ZSET, zset, b("member"), "1");expire(zset);
        failure(RedisEditService.Reason.TARGET_EXISTS, () -> edits.add(context, KeyTypeEnum.ZSET, zset, b("member"), "2"));
        assertEquals(1.0, RedisZSetService.service.zscore(context, zset, b("member")));
    }

    @Test void renamedHashScannerReadsTheNewKey() {
        String old = key("scan-old"), renamed = own(old + ":new");
        RedisHashService.service.hset(context, old, "field", b("value"));expire(old);
        var result = new AtomicReference<org.dromara.redisfront.ui.components.scanner.model.ScanDataResult<org.dromara.redisfront.model.table.HashTableModel>>();
        var scanner = new HashRedisDataScanner(context, old, result::set, ResourceBundle.getBundle("org.dromara.redisfront.RedisFront"));
        edits.updateMetadata(context, old, renamed, null);scanner.setKey(renamed);scanner.reset(renamed);scanner.fetchData("*");scanner.refreshUI();
        assertEquals(1, result.get().getData().getRowCount());assertEquals("field", result.get().getData().getValueAt(0, 0));
    }

    @Test void missingSecondStepPermissionsCauseNoPartialMutation() {
        Assumptions.assumeTrue(context.getRedisMode() != RedisMode.CLUSTER, "ACL fixture is scoped to the standalone test master");
        String user = "redisfront_safety_" + UUID.randomUUID().toString().replace("-", "");
        String password = UUID.randomUUID().toString();
        var codec = new Utf8KeyByteArrayValueCodec();
        LettuceUtils.exec(context, c -> c.dispatch(CommandType.ACL, new StatusOutput<>(codec),
                new CommandArgs<>(codec).add("SETUSER").add(user).add("on").add(">" + password)
                        .add("~" + prefix + "*").add("+@all").add("-srem").add("-pexpire").add("-persist")));
        var restricted = context.clone();restricted.setUid(UUID.randomUUID().toString());restricted.setUsername(user);restricted.setPassword(password);
        try {
            String set = key("permissions-set");RedisSetService.service.sadd(context, set, b("old"));expire(set);
            failure(RedisEditService.Reason.PERMISSION, () -> edits.save(restricted, target(KeyTypeEnum.SET, set, b("old"), null, null, null), b("new"), ""));
            assertTrue(RedisSetService.service.sismember(context, set, b("old")));assertEquals(1L, RedisSetService.service.scard(context, set));
            String string = key("permissions-string");string(string, b("old"));
            failure(RedisEditService.Reason.PERMISSION, () -> edits.save(restricted, target(KeyTypeEnum.STRING, string, b("old"), null, null, null), b("new"), ""));
            assertArrayEquals(b("old"), get(string));assertTrue(ttl(string) > 0);
            String renamed = own(string + ":new");
            failure(RedisEditService.Reason.PERMISSION, () -> edits.updateMetadata(restricted, string, renamed, -1L));
            assertArrayEquals(b("old"), get(string));assertNull(get(renamed));
        } finally {
            RedisConnectionPoolManager.cleanupContextPool(restricted);LettuceUtils.close(restricted);
            LettuceUtils.exec(context, c -> c.aclDeluser(user));
        }
    }

    @Test void clusterScriptsRouteToEveryMasterAndCrossSlotRenameIsRejected() {
        Assumptions.assumeTrue(context.getRedisMode() == RedisMode.CLUSTER);
        var masters = RedisBasicService.service.getClusterNodes(context).stream().filter(n -> Arrays.asList(n.flags().split(",")).contains("master")).toList();
        assertFalse(masters.isEmpty());
        for (var master : masters) {
            String key;
            do { key = key("route"); } while (!owns(master.slot(), SlotHash.getSlot(key)));
            string(key, b("old"));edits.save(context, target(KeyTypeEnum.STRING, key, b("old"), null, null, null), b("new"), "");
            assertArrayEquals(b("new"), get(key));
        }
        String source = key("cross-source"), destination;
        do { destination = key("cross-target"); } while (SlotHash.getSlot(source) == SlotHash.getSlot(destination));
        string(source, b("keep"));String target = destination;
        failure(RedisEditService.Reason.CROSS_SLOT, () -> edits.updateMetadata(context, source, target, null));
        assertArrayEquals(b("keep"), get(source));assertNull(get(destination));
    }

    private static boolean owns(String slots, int slot) {
        for (String range : slots.split(" ")) {
            if (!range.matches("\\d+(-\\d+)?")) continue;
            String[] bounds = range.split("-");int lower = Integer.parseInt(bounds[0]);
            int upper = Integer.parseInt(bounds[bounds.length - 1]);if (slot >= lower && slot <= upper) return true;
        }
        return false;
    }
}
