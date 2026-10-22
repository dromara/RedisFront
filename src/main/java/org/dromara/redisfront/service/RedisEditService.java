package org.dromara.redisfront.service;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.cluster.SlotHash;
import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

/** Safe editor mutations, kept separate from the ordinary Redis SET/add commands. */
public final class RedisEditService {
    public static final RedisEditService service = new RedisEditService();

    public enum Reason { MISSING, CONFLICT, TARGET_EXISTS, WRONG_TYPE, PERMISSION, INVALID_TTL, INVALID_SCORE, CROSS_SLOT, INVALID_KEY }

    public static final class EditException extends RuntimeException {
        private final Reason reason;
        public EditException(Reason reason) {
            super(ResourceBundle.getBundle("org.dromara.redisfront.RedisFront").getString("DataEdit." + reason.name()));
            this.reason = reason;
        }
        public Reason reason() { return reason; }
    }

    /** Snapshot of the selected item, captured before starting background work. */
    public record Target(KeyTypeEnum type, String key, byte[] value, String field, Double score, Long index) {
        public Target {
            Objects.requireNonNull(type);
            Objects.requireNonNull(key);
            value = Objects.requireNonNull(value).clone();
        }
    }

    @FunctionalInterface
    interface Executor {
        Long eval(RedisConnectContext context, String script, String[] keys, byte[]... args);
    }
    private final Executor executor;

    public RedisEditService() {
        this((context, script, keys, args) -> LettuceUtils.execByMode(context,
                c -> c.<Long>eval(script, ScriptOutputType.INTEGER, keys, args),
                c -> c.<Long>eval(script, ScriptOutputType.INTEGER, keys, args)));
    }
    RedisEditService(Executor executor) { this.executor = executor; }

    private static final String PRELUDE = """
            local key = KEYS[1]
            local function kind(expected, allowMissing)
                local actual = redis.call('TYPE', key).ok
                if actual == 'none' then if allowMissing then return 1 else return 0 end end
                if actual ~= expected then return -3 end
                return 1
            end
            local function allowed(command, ...)
                return not redis.acl_check_cmd or redis.acl_check_cmd(command, ...)
            end
            """;

    private static final String STRING = PRELUDE + """
            local valid = kind('string', false); if valid ~= 1 then return valid end
            if redis.call('GET', key) ~= ARGV[1] then return -1 end
            if ARGV[1] == ARGV[2] then return 1 end
            local ttl = redis.call('PTTL', key)
            if not allowed('SET', key, ARGV[2]) then return -5 end
            if ttl >= 0 and not allowed('PEXPIRE', key, ttl) then return -5 end
            redis.call('SET', key, ARGV[2])
            if ttl >= 0 then redis.call('PEXPIRE', key, ttl) end
            return 1
            """;
    private static final String HASH = PRELUDE + """
            local valid = kind('hash', false); if valid ~= 1 then return valid end
            local old = redis.call('HGET', key, ARGV[1])
            if old == false then return 0 end
            if old ~= ARGV[3] then return -1 end
            if ARGV[1] ~= ARGV[2] and redis.call('HEXISTS', key, ARGV[2]) == 1 then return -2 end
            if not allowed('HSET', key, ARGV[2], ARGV[4]) then return -5 end
            if ARGV[1] ~= ARGV[2] and not allowed('HDEL', key, ARGV[1]) then return -5 end
            redis.call('HSET', key, ARGV[2], ARGV[4])
            if ARGV[1] ~= ARGV[2] then redis.call('HDEL', key, ARGV[1]) end
            return 1
            """;
    private static final String SET = PRELUDE + """
            local valid = kind('set', false); if valid ~= 1 then return valid end
            if redis.call('SISMEMBER', key, ARGV[1]) ~= 1 then return -1 end
            if ARGV[1] == ARGV[2] then return 1 end
            if redis.call('SISMEMBER', key, ARGV[2]) == 1 then return -2 end
            if not allowed('SADD', key, ARGV[2]) or not allowed('SREM', key, ARGV[1]) then return -5 end
            redis.call('SADD', key, ARGV[2])
            redis.call('SREM', key, ARGV[1])
            return 1
            """;
    private static final String ZSET = PRELUDE + """
            local valid = kind('zset', false); if valid ~= 1 then return valid end
            local score = redis.call('ZSCORE', key, ARGV[1])
            if score == false then return 0 end
            if tonumber(score) ~= tonumber(ARGV[3]) then return -1 end
            if ARGV[1] ~= ARGV[2] and redis.call('ZSCORE', key, ARGV[2]) ~= false then return -2 end
            if not allowed('ZADD', key, ARGV[4], ARGV[2]) then return -5 end
            if ARGV[1] ~= ARGV[2] and not allowed('ZREM', key, ARGV[1]) then return -5 end
            redis.call('ZADD', key, ARGV[4], ARGV[2])
            if ARGV[1] ~= ARGV[2] then redis.call('ZREM', key, ARGV[1]) end
            return 1
            """;
    private static final String LIST = PRELUDE + """
            local valid = kind('list', false); if valid ~= 1 then return valid end
            local old = redis.call('LINDEX', key, ARGV[1])
            if old == false then return 0 end
            if old ~= ARGV[2] then return -1 end
            if not allowed('LSET', key, ARGV[1], ARGV[3]) then return -5 end
            redis.call('LSET', key, ARGV[1], ARGV[3])
            return 1
            """;
    private static final String METADATA = PRELUDE + """
            if redis.call('EXISTS', key) == 0 then return 0 end
            local target = KEYS[2]
            if key ~= target and redis.call('EXISTS', target) == 1 then return -2 end
            if key ~= target and not allowed('RENAMENX', key, target) then return -5 end
            if ARGV[1] == 'persist' and not allowed('PERSIST', target) then return -5 end
            if ARGV[1] ~= 'persist' and ARGV[1] ~= 'keep' and not allowed('EXPIRE', target, ARGV[1]) then return -5 end
            if key ~= target then redis.call('RENAMENX', key, target) end
            if ARGV[1] == 'persist' then redis.call('PERSIST', target)
            elseif ARGV[1] ~= 'keep' then redis.call('EXPIRE', target, ARGV[1]) end
            return 1
            """;
    private static final String ADD = PRELUDE + """
            local valid = kind(ARGV[1], true); if valid ~= 1 then return valid end
            if ARGV[1] == 'hash' then
                if redis.call('HEXISTS', key, ARGV[2]) == 1 then return -2 end
                if not allowed('HSET', key, ARGV[2], ARGV[3]) then return -5 end
                redis.call('HSET', key, ARGV[2], ARGV[3])
            elseif ARGV[1] == 'zset' then
                if redis.call('ZSCORE', key, ARGV[3]) ~= false then return -2 end
                if not allowed('ZADD', key, ARGV[2], ARGV[3]) then return -5 end
                redis.call('ZADD', key, ARGV[2], ARGV[3])
            elseif ARGV[1] == 'set' then
                if not allowed('SADD', key, ARGV[3]) then return -5 end
                redis.call('SADD', key, ARGV[3])
            elseif ARGV[1] == 'list' then
                if not allowed('LPUSH', key, ARGV[3]) then return -5 end
                redis.call('LPUSH', key, ARGV[3])
            end
            return 1
            """;

    public void add(RedisConnectContext context, KeyTypeEnum type, String key, byte[] value, String fieldOrScore) {
        String parameter = switch (type) {
            case HASH -> Objects.requireNonNull(fieldOrScore);
            case ZSET -> String.valueOf(parseScore(fieldOrScore));
            case LIST, SET -> "";
            default -> throw new IllegalArgumentException("Unsupported collection type: " + type);
        };
        execute(context, ADD, new String[]{key}, utf8(type.name().toLowerCase(java.util.Locale.ROOT)), utf8(parameter), value);
    }

    public void save(RedisConnectContext context, Target original, byte[] value, String fieldOrScore) {
        Objects.requireNonNull(value);
        switch (original.type()) {
            case STRING -> execute(context, STRING, new String[]{original.key()}, original.value(), value);
            case HASH -> execute(context, HASH, new String[]{original.key()}, utf8(original.field()),
                    utf8(Objects.requireNonNull(fieldOrScore)), original.value(), value);
            case SET -> execute(context, SET, new String[]{original.key()}, original.value(), value);
            case ZSET -> {
                double score = parseScore(fieldOrScore);
                if (original.score() == null || !Double.isFinite(original.score())) throw new EditException(Reason.INVALID_SCORE);
                execute(context, ZSET, new String[]{original.key()}, original.value(), value,
                        utf8(String.valueOf(original.score())), utf8(String.valueOf(score)));
            }
            case LIST -> {
                if (original.index() == null || original.index() < 0) throw new IllegalArgumentException("Missing list index");
                execute(context, LIST, new String[]{original.key()}, utf8(original.index().toString()), original.value(), value);
            }
            default -> throw new IllegalArgumentException("Unsupported editable type: " + original.type());
        }
    }

    public record RemoteValue(byte[] value, String identity, boolean partial) { }

    /** A fresh comparison only; this never replaces the optimistic snapshot used by save. */
    public RemoteValue currentValue(RedisConnectContext context, Target target) {
        if (target.type() == KeyTypeEnum.STRING) {
            var current = RedisValueLoader.load(context, target.key(), 0, false);
            return new RemoteValue(current.value(), null, current.partial());
        }
        return LettuceUtils.execByMode(context, c -> switch (target.type()) {
            case HASH -> new RemoteValue(c.hget(target.key(), target.field()), target.field(), false);
            case LIST -> new RemoteValue(c.lindex(target.key(), target.index()), null, false);
            case SET -> new RemoteValue(c.sismember(target.key(), target.value()) ? target.value() : null, null, false);
            case ZSET -> { Double score = c.zscore(target.key(), target.value());yield new RemoteValue(score == null ? null : target.value(), String.valueOf(score), false); }
            default -> throw new IllegalArgumentException("Unsupported comparison type");
        }, c -> switch (target.type()) {
            case HASH -> new RemoteValue(c.hget(target.key(), target.field()), target.field(), false);
            case LIST -> new RemoteValue(c.lindex(target.key(), target.index()), null, false);
            case SET -> new RemoteValue(c.sismember(target.key(), target.value()) ? target.value() : null, null, false);
            case ZSET -> { Double score = c.zscore(target.key(), target.value());yield new RemoteValue(score == null ? null : target.value(), String.valueOf(score), false); }
            default -> throw new IllegalArgumentException("Unsupported comparison type");
        });
    }

    public void updateMetadata(RedisConnectContext context, String oldKey, String newKey, Long ttlSeconds) {
        if (newKey == null || newKey.isEmpty()) throw new EditException(Reason.INVALID_KEY);
        validateTtl(ttlSeconds);
        if (context.getRedisMode() == RedisMode.CLUSTER && SlotHash.getSlot(oldKey) != SlotHash.getSlot(newKey)) {
            throw new EditException(Reason.CROSS_SLOT);
        }
        String ttl = ttlSeconds == null ? "keep" : ttlSeconds == -1 ? "persist" : ttlSeconds.toString();
        execute(context, METADATA, new String[]{oldKey, newKey}, utf8(ttl));
    }

    public static Long parseTtl(String text) {
        try {
            long value = Long.parseLong(text.trim());
            validateTtl(value);
            return value;
        } catch (NumberFormatException | ArithmeticException e) { throw new EditException(Reason.INVALID_TTL); }
    }

    private static void validateTtl(Long ttl) {
        if (ttl == null || ttl == -1) return;
        if (ttl <= 0) throw new EditException(Reason.INVALID_TTL);
        try { Math.addExact(System.currentTimeMillis(), Math.multiplyExact(ttl, 1000)); }
        catch (ArithmeticException e) { throw new EditException(Reason.INVALID_TTL); }
    }

    public static double parseScore(String text) {
        try {
            double score = Double.parseDouble(text);
            if (!Double.isFinite(score)) throw new EditException(Reason.INVALID_SCORE);
            return score;
        } catch (NumberFormatException | NullPointerException e) { throw new EditException(Reason.INVALID_SCORE); }
    }

    private static byte[] utf8(String value) { return Objects.requireNonNull(value).getBytes(StandardCharsets.UTF_8); }

    private void execute(RedisConnectContext context, String script, String[] keys, byte[]... args) {
        long result = executor.eval(context, script, keys, args);
        if (result == 1) return;
        throw new EditException(switch ((int) result) {
            case 0 -> Reason.MISSING; case -1 -> Reason.CONFLICT; case -2 -> Reason.TARGET_EXISTS;
            case -3 -> Reason.WRONG_TYPE; case -5 -> Reason.PERMISSION;
            default -> throw new IllegalStateException("Unexpected edit result: " + result);
        });
    }
}
