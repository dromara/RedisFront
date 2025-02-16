package org.dromara.redisfront.ui.components.scanner;

import org.dromara.redisfront.ui.components.scanner.context.RedisScanContext;
import org.dromara.redisfront.ui.components.scanner.context.RedisScanContextManager;
import org.dromara.redisfront.ui.components.scanner.model.ScanDataResult;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.scanner.handler.ScanDataRefreshHandler;

import java.util.*;

public abstract class AbstractRedisDataScanner<T, M> implements RedisDataScanner {
    protected final RedisConnectContext redisConnectContext;
    protected final ResourceBundle tr;
    protected final Map<String, RedisScanContext<T>> contextMap;
    protected final RedisScanContextManager<T> redisScanContextManager = new RedisScanContextManager<>();
    protected final ScanDataRefreshHandler<ScanDataResult<M>> scanDataRefreshHandler;

    private final ScanDataResult<M> scanDataResult = new ScanDataResult<>();


    public AbstractRedisDataScanner(RedisConnectContext redisConnectContext, ResourceBundle tr, ScanDataRefreshHandler<ScanDataResult<M>> scanDataRefreshHandler) {
        this.redisConnectContext = redisConnectContext;
        this.tr = tr;
        this.scanDataRefreshHandler = scanDataRefreshHandler;
        this.contextMap = new LinkedHashMap<>();
    }

    protected abstract M createModel(Collection<T> data);

    protected abstract Long getLen();

    protected void updateState(RedisScanContext<T> scanContext) {
        scanDataResult.setData(createModel(scanContext.getKeyList()));
        scanDataResult.setLen(getLen());
        scanDataResult.setLoadSize(scanContext.getLoadSize());
        scanDataResult.setDataSize(scanContext.getDataLength());
        scanDataResult.setIsFinished(scanContext.getScanCursor().isFinished());
    }

    @Override
    public void refreshUI() {
        scanDataRefreshHandler.accept(scanDataResult);
    }

    protected RedisScanContext<T> getContextByKey(String key) {
        return redisScanContextManager.getContext(key);
    }

    public void reset(String key)  {
        redisScanContextManager.reset(key);
    }

}
