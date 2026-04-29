package org.dromara.redisfront.commons.codec;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class RedisValueCodec {

    private static final String BASE64_PREFIX = "base64:";
    private static final String HEX_PREFIX = "hex:";
    private static final Charset GBK = Charset.forName("GBK");
    private static final Charset GB18030 = Charset.forName("GB18030");

    private RedisValueCodec() {
    }

    public static String encode(byte[] value, ValueViewType type) {
        if (value == null) {
            return "";
        }
        return switch (type) {
            case AUTO -> encodeAuto(value);
            case UTF8 -> new String(value, StandardCharsets.UTF_8);
            case GBK -> new String(value, GBK);
            case GB18030 -> new String(value, GB18030);
            case LATIN1 -> new String(value, StandardCharsets.ISO_8859_1);
            case ESCAPED -> encodeEscaped(value);
            case BASE64 -> Base64.getEncoder().encodeToString(value);
            case HEX -> toHex(value);
        };
    }

    public static byte[] decode(String text, ValueViewType type) {
        if (text == null) {
            return new byte[0];
        }
        return switch (type) {
            case AUTO -> decodeAuto(text);
            case UTF8 -> text.getBytes(StandardCharsets.UTF_8);
            case GBK -> text.getBytes(GBK);
            case GB18030 -> text.getBytes(GB18030);
            case LATIN1 -> text.getBytes(StandardCharsets.ISO_8859_1);
            case ESCAPED -> throw new UnsupportedOperationException("ESCAPED view is read-only");
            case BASE64 -> decodeBase64(text);
            case HEX -> decodeHex(text);
        };
    }

    public static boolean isValidUtf8(byte[] value) {
        if (value == null) {
            return true;
        }
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            decoder.decode(ByteBuffer.wrap(value));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    private static String encodeAuto(byte[] value) {
        if (isValidUtf8(value)) {
            return new String(value, StandardCharsets.UTF_8);
        }
        return BASE64_PREFIX + Base64.getEncoder().encodeToString(value);
    }

    private static String encodeEscaped(byte[] value) {
        StringBuilder sb = new StringBuilder(value.length * 4);
        for (byte b : value) {
            int v = b & 0xFF;
            if (v == '\\') {
                sb.append("\\\\");
            } else if (v == '\"') {
                sb.append("\\\"");
            } else if (v == '\n') {
                sb.append("\\n");
            } else if (v == '\r') {
                sb.append("\\r");
            } else if (v == '\t') {
                sb.append("\\t");
            } else if (v >= 0x20 && v <= 0x7E) {
                sb.append((char) v);
            } else {
                sb.append("\\x");
                sb.append(toHexChar(v >>> 4));
                sb.append(toHexChar(v & 0x0F));
            }
        }
        return sb.toString();
    }

    private static byte[] decodeAuto(String text) {
        String trimmed = text.trim();
        if (trimmed.regionMatches(true, 0, BASE64_PREFIX, 0, BASE64_PREFIX.length())) {
            return decodeBase64(trimmed.substring(BASE64_PREFIX.length()).trim());
        }
        if (trimmed.regionMatches(true, 0, HEX_PREFIX, 0, HEX_PREFIX.length())) {
            return decodeHex(trimmed.substring(HEX_PREFIX.length()).trim());
        }
        if (trimmed.regionMatches(true, 0, "0x", 0, 2)) {
            return decodeHex(trimmed.substring(2).trim());
        }
        return trimmed.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] decodeBase64(String text) {
        String trimmed = text.trim();
        if (trimmed.regionMatches(true, 0, BASE64_PREFIX, 0, BASE64_PREFIX.length())) {
            trimmed = trimmed.substring(BASE64_PREFIX.length()).trim();
        }
        return Base64.getDecoder().decode(trimmed);
    }

    private static byte[] decodeHex(String text) {
        String trimmed = text.trim();
        if (trimmed.regionMatches(true, 0, HEX_PREFIX, 0, HEX_PREFIX.length())) {
            trimmed = trimmed.substring(HEX_PREFIX.length()).trim();
        }
        if (trimmed.regionMatches(true, 0, "0x", 0, 2)) {
            trimmed = trimmed.substring(2).trim();
        }
        trimmed = trimmed.replaceAll("\\s+", "");
        if ((trimmed.length() & 1) == 1) {
            throw new IllegalArgumentException("Hex length must be even");
        }
        int len = trimmed.length() / 2;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            int hi = hexValue(trimmed.charAt(i * 2));
            int lo = hexValue(trimmed.charAt(i * 2 + 1));
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    private static int hexValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        throw new IllegalArgumentException("Invalid hex char: " + c);
    }

    private static String toHex(byte[] value) {
        char[] out = new char[value.length * 2];
        int i = 0;
        for (byte b : value) {
            int v = b & 0xFF;
            out[i++] = toHexChar(v >>> 4);
            out[i++] = toHexChar(v & 0x0F);
        }
        return new String(out);
    }

    private static char toHexChar(int v) {
        return (char) (v < 10 ? ('0' + v) : ('a' + (v - 10)));
    }
}
