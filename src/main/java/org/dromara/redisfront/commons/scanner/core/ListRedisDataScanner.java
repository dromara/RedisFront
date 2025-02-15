package org.dromara.redisfront.commons.scanner.core;

import io.lettuce.core.ScanCursor;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.scanner.AbstractRedisDataScanner;
import org.dromara.redisfront.commons.scanner.model.ScanDataResult;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.ListTableModel;
import org.dromara.redisfront.service.RedisListService;
import org.dromara.redisfront.ui.handler.RefreshHandler;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class ListRedisDataScanner extends AbstractRedisDataScanner<String, ListTableModel> {
    @Setter
    @Getter
    private String key;

    public ListRedisDataScanner(RedisConnectContext redisConnectContext, String key, RefreshHandler<ScanDataResult<ListTableModel>> refreshHandler, ResourceBundle tr) {
        super(redisConnectContext, tr, refreshHandler);
        this.key = key;
    }


    @Override
    public void fetchData(String fetchKey) {
        var scanContext = redisScanContextManager.getContext(key);

        var start = Long.parseLong(scanContext.getScanCursor().getCursor());
        var stop = start + (scanContext.getLimit() - 1);
        var value = RedisListService.service.lrange(redisConnectContext, key, start, stop);

        scanContext.setKeyList(value);

        var nextCursor = start + scanContext.getLimit();
        if (nextCursor >= getLen()) {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(getLen()), true));
        } else {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(nextCursor), false));
        }

        updateState(scanContext);
    }

    @Override
    protected ListTableModel createModel(Collection<String> data) {
        return new ListTableModel((List<String>) data);
    }

    public void reset() {
        redisScanContextManager.reset(key);
    }

    @Override
    protected Long getLen() {
        return RedisListService.service.llen(redisConnectContext, key);
    }
}
