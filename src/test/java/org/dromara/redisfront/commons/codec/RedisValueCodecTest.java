package org.dromara.redisfront.commons.codec;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RedisValueCodecTest {

    @Test
    void auto_should_roundtrip_utf8() {
        byte[] raw = "hello".getBytes(StandardCharsets.UTF_8);
        String text = RedisValueCodec.encode(raw, ValueViewType.AUTO);
        assertEquals("hello", text);
        assertArrayEquals(raw, RedisValueCodec.decode(text, ValueViewType.AUTO));
    }

    @Test
    void auto_should_roundtrip_binary_as_base64_prefixed() {
        byte[] raw = new byte[]{0x00, 0x01, 0x7F, (byte) 0xFF};
        String text = RedisValueCodec.encode(raw, ValueViewType.AUTO);
        assertTrue(text.startsWith("base64:"));
        assertArrayEquals(raw, RedisValueCodec.decode(text, ValueViewType.AUTO));
    }

    @Test
    void base64_should_roundtrip() {
        byte[] raw = new byte[]{1, 2, 3, 4, 5};
        String text = RedisValueCodec.encode(raw, ValueViewType.BASE64);
        assertEquals(Base64.getEncoder().encodeToString(raw), text);
        assertArrayEquals(raw, RedisValueCodec.decode(text, ValueViewType.BASE64));
    }

    @Test
    void hex_should_roundtrip() {
        byte[] raw = new byte[]{0x00, 0x10, 0x2A, (byte) 0xFF};
        String text = RedisValueCodec.encode(raw, ValueViewType.HEX);
        assertEquals("00102aff", text);
        assertArrayEquals(raw, RedisValueCodec.decode(text, ValueViewType.HEX));
    }
}

