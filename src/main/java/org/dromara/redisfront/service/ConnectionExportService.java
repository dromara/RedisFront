package org.dromara.redisfront.service;

import cn.hutool.json.JSONUtil;
import org.dromara.redisfront.model.ExportData;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Compatible plain JSON without secrets, or a versioned authenticated encrypted backup. */
public final class ConnectionExportService {
    private static final int ITERATIONS = 210_000;
    private ConnectionExportService() { }

    public static String withoutSecrets(ExportData source) {
        var copy = JSONUtil.toBean(JSONUtil.toJsonStr(source), ExportData.class);
        if (copy.getDetails() != null) copy.getDetails().forEach(detail -> {
            detail.setPassword(null);
            detail.setSshConfig(sanitizeConfig(detail.getSshConfig()));
            detail.setSslConfig(sanitizeConfig(detail.getSslConfig()));
        });
        return JSONUtil.toJsonStr(copy);
    }
    private static String sanitizeConfig(String text) {
        if (text == null || text.isBlank()) return text;
        var config = JSONUtil.parseObj(text);
        for (String key : new ArrayList<>(config.keySet())) {
            String normalized = key.toLowerCase(Locale.ROOT);
            if (normalized.contains("password") || normalized.contains("privatekey") || normalized.contains("secret")
                    || normalized.contains("passphrase") || normalized.contains("token")) config.remove(key);
        }
        return JSONUtil.toJsonStr(config);
    }
    public static boolean encrypted(String text) { return JSONUtil.parseObj(text).containsKey("redisfrontEncrypted"); }
    public static String encrypt(ExportData source, char[] password) {
        if (password.length < 8) throw new IllegalArgumentException("Export password must contain at least 8 characters");
        byte[] salt = new byte[16], iv = new byte[12];
        new SecureRandom().nextBytes(salt);new SecureRandom().nextBytes(iv);
        try {
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(password, salt, ITERATIONS), new GCMParameterSpec(128, iv));
            var b64 = Base64.getEncoder();
            return JSONUtil.toJsonStr(Map.of("redisfrontEncrypted", 1, "iterations", ITERATIONS,
                    "salt", b64.encodeToString(salt), "iv", b64.encodeToString(iv),
                    "ciphertext", b64.encodeToString(cipher.doFinal(JSONUtil.toJsonStr(source).getBytes(StandardCharsets.UTF_8)))));
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Unable to encrypt connection backup", e); }
    }
    public static ExportData read(String text, char[] password) {
        if (!encrypted(text)) return JSONUtil.toBean(text, ExportData.class);
        var envelope = JSONUtil.parseObj(text);
        try {
            if (envelope.getInt("redisfrontEncrypted") != 1 || envelope.getInt("iterations") != ITERATIONS) {
                throw new IllegalArgumentException("Unsupported encrypted backup version");
            }
            var b64 = Base64.getDecoder();
            byte[] salt = b64.decode(envelope.getStr("salt")), iv = b64.decode(envelope.getStr("iv"));
            if (salt.length != 16 || iv.length != 12) throw new IllegalArgumentException("Invalid encrypted backup");
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(password, salt, ITERATIONS), new GCMParameterSpec(128, iv));
            byte[] plain = cipher.doFinal(b64.decode(envelope.getStr("ciphertext")));
            try { return JSONUtil.toBean(new String(plain, StandardCharsets.UTF_8), ExportData.class); }
            finally { Arrays.fill(plain, (byte) 0); }
        } catch (GeneralSecurityException e) { throw new IllegalArgumentException("Incorrect password or damaged encrypted backup"); }
    }
    private static SecretKey key(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        var spec = new PBEKeySpec(password, salt, iterations, 256);
        byte[] encoded = null;
        try {
            encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return new SecretKeySpec(encoded, "AES");
        } finally { spec.clearPassword();if (encoded != null) Arrays.fill(encoded, (byte) 0); }
    }
}
