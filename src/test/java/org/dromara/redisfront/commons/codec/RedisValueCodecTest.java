package org.dromara.redisfront.commons.codec;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RedisValueCodecTest {

    @Test
    void auto_should_preserve_whitespace_and_control_bytes() {
        String value = "\u0000 \thello\r\n ";
        assertArrayEquals(value.getBytes(StandardCharsets.UTF_8), RedisValueCodec.decode(value, ValueViewType.AUTO));
    }

    @Test
    void auto_should_preserve_literal_encoding_prefixes() {
        for (String text : new String[]{"0x4142", "hex:4142", "base64:QUI=", "base64:not-valid!", " 0x4142 "}) {
            assertArrayEquals(text.getBytes(StandardCharsets.UTF_8), RedisValueCodec.decode(text, ValueViewType.AUTO), text);
        }
    }

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
        var view = RedisValueCodec.forEditing(raw, ValueViewType.AUTO);
        assertTrue(view.text().startsWith("base64:"));
        assertEquals(ValueViewType.BASE64, view.effectiveType());
        assertArrayEquals(raw, RedisValueCodec.decode(view.text(), view.effectiveType()));
    }

    @Test
    void editing_view_should_keep_literal_prefixes_as_utf8() {
        for (String text : new String[]{"0x4142", "hex:4142", "base64:QUI=", " \thello\r\n"}) {
            byte[] raw = text.getBytes(StandardCharsets.UTF_8);
            var view = RedisValueCodec.forEditing(raw, ValueViewType.AUTO);
            assertEquals(ValueViewType.UTF8, view.effectiveType());
            assertArrayEquals(raw, RedisValueCodec.decode(view.text(), view.effectiveType()));
        }
    }

    @Test
    void editing_view_should_roundtrip_every_explicit_editable_encoding() {
        byte[] raw = new byte[]{0, 1, (byte) 0xff, (byte) 0xc3, 0x28};
        for (ValueViewType type : new ValueViewType[]{ValueViewType.AUTO, ValueViewType.BASE64, ValueViewType.HEX, ValueViewType.LATIN1}) {
            var view = RedisValueCodec.forEditing(raw, type);
            assertArrayEquals(raw, RedisValueCodec.decode(view.text(), view.effectiveType()), type.name());
        }
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
