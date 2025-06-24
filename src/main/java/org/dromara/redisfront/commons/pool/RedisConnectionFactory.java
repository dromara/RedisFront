package org.dromara.redisfront.commons.pool;

import io.lettuce.core.api.StatefulConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@Slf4j
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
        if (p.getObject() instanceof StatefulConnection<?, ?> statefulConnection) {
            return statefulConnection.isOpen();
        }
        return false;
    }

    @Override
    public void destroyObject(PooledObject<T> p) {
        if (p.getObject() instanceof StatefulConnection<?, ?> statefulConnection) {
            try {
                if (statefulConnection.isOpen()) {
                    statefulConnection.flushCommands();
                    statefulConnection.close();
                }
            } catch (Exception e) {
                log.error("Close connection failed", e);
            }
        }
    }

}
