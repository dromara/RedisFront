package org.dromara.redisfront.commons.scanner;

import org.dromara.redisfront.commons.scanner.context.RedisScanContext;
import org.dromara.redisfront.commons.scanner.context.RedisScanContextManager;
import org.dromara.redisfront.commons.scanner.model.ScanDataResult;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.handler.RefreshHandler;

import java.util.*;

public abstract class AbstractRedisDataScanner<T, M> implements RedisDataScanner {
    protected final RedisConnectContext redisConnectContext;
    protected final ResourceBundle tr;
    protected final Map<String, RedisScanContext<T>> contextMap;
    protected final RedisScanContextManager<T> redisScanContextManager = new RedisScanContextManager<>();
    protected final RefreshHandler<ScanDataResult<M>> refreshHandler;

    private final ScanDataResult<M> scanDataResult = new ScanDataResult<>();


    public AbstractRedisDataScanner(RedisConnectContext redisConnectContext, ResourceBundle tr, RefreshHandler<ScanDataResult<M>> refreshHandler) {
        this.redisConnectContext = redisConnectContext;
        this.tr = tr;
        this.refreshHandler = refreshHandler;
        this.contextMap = new LinkedHashMap<>();
    }

    protected abstract M createModel(Collection<T> data);

    protected abstract Long getLen();

    protected void updateState(RedisScanContext<T> scanContext) {
        scanDataResult.setTableModel(createModel(scanContext.getKeyList()));
        scanDataResult.setLen(getLen());
        scanDataResult.setLoadSize(scanContext.getLoadSize());
        scanDataResult.setDataSize(scanContext.getDataLength());
        scanDataResult.setIsFinished(scanContext.getScanCursor().isFinished());
    }

    @Override
    public void refreshUI() {
        refreshHandler.accept(scanDataResult);
    }

}
