package org.dromara.redisfront.commons.pool;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RedisConnectionFactory<T> extends BasePooledObjectFactory<T> {
    private final ConnectionSupplier<T> supplier;

    public RedisConnectionFactory(ConnectionSupplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T create() throws Exception {
        return supplier.get();
    }

    @Override
    public PooledObject<T> wrap(T obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public boolean validateObject(PooledObject<T> p) {
        return ((StatefulConnection<?, ?>) p.getObject()).isOpen();
    }

    @Override
    public void destroyObject(PooledObject<T> p) {
        ((StatefulConnection<?, ?>) p.getObject()).close();
    }
}
