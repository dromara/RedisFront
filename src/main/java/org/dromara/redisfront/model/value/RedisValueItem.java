package org.dromara.redisfront.model.value;

import org.dromara.redisfront.commons.codec.RedisValueCodec;
import org.dromara.redisfront.commons.codec.ValueViewType;

public final class RedisValueItem {
    private final byte[] raw;
    private final String display;

    public RedisValueItem(byte[] raw) {
        this.raw = raw == null ? new byte[0] : raw;
        this.display = RedisValueCodec.encode(this.raw, ValueViewType.AUTO);
    }

    public byte[] raw() {
        return raw;
    }

    public int byteLength() {
        return raw.length;
    }

    @Override
    public String toString() {
        return display;
    }
}

