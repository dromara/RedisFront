package org.dromara.redisfront.commons.codec;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
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

    @Test
    void gbk_should_roundtrip() {
        Charset gbk = Charset.forName("GBK");
        byte[] raw = "中文".getBytes(gbk);
        String text = RedisValueCodec.encode(raw, ValueViewType.GBK);
        assertEquals("中文", text);
        assertArrayEquals(raw, RedisValueCodec.decode(text, ValueViewType.GBK));
    }

    @Test
    void gb18030_should_roundtrip() {
        Charset gb18030 = Charset.forName("GB18030");
        byte[] raw = "中文".getBytes(gb18030);
        String text = RedisValueCodec.encode(raw, ValueViewType.GB18030);
        assertEquals("中文", text);
        assertArrayEquals(raw, RedisValueCodec.decode(text, ValueViewType.GB18030));
    }

    @Test
    void latin1_should_roundtrip_arbitrary_bytes() {
        byte[] raw = new byte[]{(byte) 0xC3, 0x28, (byte) 0xFF, 0x00};
        String text = RedisValueCodec.encode(raw, ValueViewType.LATIN1);
        assertArrayEquals(raw, RedisValueCodec.decode(text, ValueViewType.LATIN1));
    }

    @Test
    void escaped_should_encode_non_printable_as_hex_escape_and_be_readonly() {
        byte[] raw = new byte[]{0x00, 0x0A, 0x09, 0x22, 0x5C, 0x41, (byte) 0xFF};
        String text = RedisValueCodec.encode(raw, ValueViewType.ESCAPED);
        assertEquals("\\x00\\n\\t\\\"\\\\A\\xff", text);
        assertThrows(UnsupportedOperationException.class, () -> RedisValueCodec.decode(text, ValueViewType.ESCAPED));
    }
}
