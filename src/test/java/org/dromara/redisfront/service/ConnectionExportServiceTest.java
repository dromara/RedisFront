package org.dromara.redisfront.service;

import cn.hutool.json.JSONUtil;
import org.dromara.redisfront.model.ExportData;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionExportServiceTest {
    private ExportData fixture() {
        var detail = new ConnectDetailEntity();detail.setName("test");detail.setHost("redis.internal");detail.setPort(6379);
        detail.setPassword("redis-secret");detail.setSshConfig("{\"host\":\"jump\",\"password\":\"ssh-secret\",\"privateKeyPath\":\"/private/key\"}");
        detail.setSslConfig("{\"password\":\"ssl-secret\",\"publicKeyFilePath\":\"ca.pem\"}");
        var data = new ExportData();data.setGroups(List.of());data.setDetails(List.of(detail));return data;
    }
    @Test void safeExportRemovesAllCredentialsWithoutMutatingSavedConnections() {
        var original = fixture();String text = ConnectionExportService.withoutSecrets(original);
        assertFalse(text.contains("secret"));assertFalse(text.contains("/private/key"));assertTrue(text.contains("ca.pem"));
        assertEquals("redis-secret", original.getDetails().getFirst().getPassword());
        assertEquals("redis.internal", ConnectionExportService.read(text, new char[0]).getDetails().getFirst().getHost());
    }
    @Test void encryptedBackupRoundTripsSecretsAndUsesFreshSaltAndNonce() {
        char[] password = "backup-password".toCharArray();var data = fixture();
        String one = ConnectionExportService.encrypt(data, password), two = ConnectionExportService.encrypt(data, password);
        assertNotEquals(one, two);assertFalse(one.contains("redis.internal"));assertFalse(one.contains("secret"));
        assertEquals(JSONUtil.toJsonStr(data), JSONUtil.toJsonStr(ConnectionExportService.read(one, password)));
    }
    @Test void wrongPasswordAndTamperingNeverProduceImportableData() {
        String text = ConnectionExportService.encrypt(fixture(), "correct-password".toCharArray());
        assertThrows(IllegalArgumentException.class, () -> ConnectionExportService.read(text, "wrong-password".toCharArray()));
        var json = JSONUtil.parseObj(text);String encrypted = json.getStr("ciphertext");
        json.set("ciphertext", (encrypted.charAt(0) == 'A' ? "B" : "A") + encrypted.substring(1));
        assertThrows(IllegalArgumentException.class, () -> ConnectionExportService.read(json.toString(), "correct-password".toCharArray()));
    }
}
