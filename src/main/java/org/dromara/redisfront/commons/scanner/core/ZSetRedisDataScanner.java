package org.dromara.redisfront.commons.scanner.core;

import io.lettuce.core.ScoredValue;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.scanner.AbstractRedisDataScanner;
import org.dromara.redisfront.commons.scanner.handler.ScanDataRefreshHandler;
import org.dromara.redisfront.commons.scanner.model.ScanDataResult;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.SortedSetTableModel;
import org.dromara.redisfront.service.RedisZSetService;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

@Setter
@Getter
public class ZSetRedisDataScanner extends AbstractRedisDataScanner<ScoredValue<String>, SortedSetTableModel> {
    private String key;

    public ZSetRedisDataScanner(RedisConnectContext redisConnectContext, String key, ScanDataRefreshHandler<ScanDataResult<SortedSetTableModel>> scanDataRefreshHandler, ResourceBundle tr) {
        super(redisConnectContext, tr, scanDataRefreshHandler);
        this.key = key;
    }


    @Override
    public void fetchData(String fetchKey) {

        var scanContext = redisScanContextManager.getContext(key);

        if (RedisFrontUtils.isNotEmpty(scanContext.getKeyList()) && scanContext.getKeyList().size() >= 1000) {
            throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
        }

        scanContext.setSearchKey(fetchKey);

        var valueScanCursor = RedisZSetService.service.zscan(redisConnectContext, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(valueScanCursor);

        scanContext.setScanCursor(valueScanCursor);

        scanContext.setKeyList(valueScanCursor.getValues());
    }

    @Override
    protected SortedSetTableModel createModel(Collection<ScoredValue<String>> data) {
        return new SortedSetTableModel((List<ScoredValue<String>>) data);
    }

    @Override
    protected Long getLen() {
        return RedisZSetService.service.zcard(redisConnectContext, key);
    }

    public void reset() {
        redisScanContextManager.reset(key);
    }
}
