package org.dromara.redisfront.commons.codec;

import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class Utf8KeyByteArrayValueCodec implements RedisCodec<String, byte[]> {

    @Override
    public String decodeKey(ByteBuffer bytes) {
        if (bytes == null) {
            return "";
        }
        byte[] arr = new byte[bytes.remaining()];
        bytes.get(arr);
        return new String(arr, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decodeValue(ByteBuffer bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        byte[] arr = new byte[bytes.remaining()];
        bytes.get(arr);
        return arr;
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        if (key == null) {
            return ByteBuffer.wrap(new byte[0]);
        }
        return ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ByteBuffer encodeValue(byte[] value) {
        if (value == null) {
            return ByteBuffer.wrap(new byte[0]);
        }
        return ByteBuffer.wrap(value);
    }
}

