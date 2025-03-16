package org.dromara.redisfront.ui.components.scanner.core;

import com.google.common.collect.Lists;
import io.lettuce.core.MapScanCursor;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.ui.components.scanner.AbstractRedisDataScanner;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.HashTableModel;
import org.dromara.redisfront.service.RedisHashService;
import org.dromara.redisfront.ui.components.scanner.handler.ScanDataRefreshHandler;
import org.dromara.redisfront.ui.components.scanner.model.ScanDataResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class HashRedisDataScanner extends AbstractRedisDataScanner<Map.Entry<String, String>, HashTableModel> {
    private final String key;

    public HashRedisDataScanner(RedisConnectContext redisConnectContext, String key, ScanDataRefreshHandler<ScanDataResult<HashTableModel>> consumer, ResourceBundle tr) {
        super(redisConnectContext, tr, consumer);
        this.key = key;
    }

    @Override
    public void fetchData(String fetchKey) {
        var scanContext = getContext(key);

        if (RedisFrontUtils.isNotEmpty(scanContext.getKeyList()) && scanContext.getKeyList().size() >= 1000) {
            throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
        }

        scanContext.setSearchKey(fetchKey);

        MapScanCursor<String, String> mapScanCursor = RedisHashService.service.hscan(redisConnectContext, key, scanContext.getScanCursor(), scanContext.getScanArgs());

        scanContext.setScanCursor(mapScanCursor);

        scanContext.setKeyList(Lists.newArrayList(mapScanCursor.getMap().entrySet()));

        updateState(scanContext);
    }

    @Override
    protected HashTableModel createModel(Collection<Map.Entry<String, String>> data) {
        return new HashTableModel((List<Map.Entry<String, String>>) data);
    }

    @Override
    protected Long getLen() {
        return RedisHashService.service.hlen(redisConnectContext, key);
    }

}
