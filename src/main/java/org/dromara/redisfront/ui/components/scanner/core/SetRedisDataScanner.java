package org.dromara.redisfront.ui.components.scanner.core;

import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.ui.components.scanner.AbstractRedisDataScanner;
import org.dromara.redisfront.ui.components.scanner.handler.ScanDataRefreshHandler;
import org.dromara.redisfront.ui.components.scanner.model.ScanDataResult;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.SetTableModel;
import org.dromara.redisfront.service.RedisSetService;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

@Setter
@Getter
public class SetRedisDataScanner extends AbstractRedisDataScanner<String, SetTableModel> {
    private String key;

    public SetRedisDataScanner(RedisConnectContext redisConnectContext, String key, ScanDataRefreshHandler<ScanDataResult<SetTableModel>> scanDataRefreshHandler, ResourceBundle tr) {
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

        var valueScanCursor = RedisSetService.service.sscan(redisConnectContext, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(valueScanCursor);

        scanContext.setKeyList(valueScanCursor.getValues());

        updateState(scanContext);
    }

    @Override
    protected SetTableModel createModel(Collection<String> data) {
        return new SetTableModel((List<String>) data);
    }

    @Override
    protected Long getLen() {
        return RedisSetService.service.scard(redisConnectContext, key);
    }

}
