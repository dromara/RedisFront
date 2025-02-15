package org.dromara.redisfront.commons.scanner.core;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import io.lettuce.core.ScoredValue;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.scanner.RedisDataScanner;
import org.dromara.redisfront.commons.scanner.context.RedisScanContext;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.SortedSetTableModel;
import org.dromara.redisfront.model.turbo.Turbo5;
import org.dromara.redisfront.service.RedisZSetService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ZSetRedisDataScanner implements RedisDataScanner {
    private final RedisConnectContext redisConnectContext;
    private final Consumer<Turbo5<Long, SortedSetTableModel, String, String, Boolean>> consumer;
    private final Map<String, RedisScanContext<ScoredValue<String>>> scanZSetContextMap;
    private final ResourceBundle tr;
    @Setter
    @Getter
    private String key;
    @Setter
    @Getter
    private String skey;

    private SortedSetTableModel sortedSetTableModel;
    private Long len;
    private String dataSize;
    private String loadSize;
    private Boolean finished;

    public ZSetRedisDataScanner(RedisConnectContext redisConnectContext, String key, Consumer<Turbo5<Long, SortedSetTableModel, String, String, Boolean>> consumer, ResourceBundle tr) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
        this.key = key;
        this.tr = tr;
        this.scanZSetContextMap = new LinkedHashMap<>();
    }

    @Override
    public void fetchData(String fetchKey) {
        len = RedisZSetService.service.zcard(redisConnectContext, key);

        var scanContext = scanZSetContextMap.getOrDefault(key, new RedisScanContext<>());

        var lastSearchKey = scanContext.getSearchKey();
        if (StrUtil.isNotEmpty(skey)) {
            scanContext.setSearchKey(skey);
        } else {
            scanContext.setSearchKey("*");
        }
        scanContext.setLimit(500L);

        var valueScanCursor = RedisZSetService.service.zscan(redisConnectContext, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(valueScanCursor);

        if (RedisFrontUtils.equal(scanContext.getSearchKey(), lastSearchKey) && RedisFrontUtils.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 2000) {
                scanContext.getKeyList().clear();
                System.gc();
                throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(valueScanCursor.getValues());
        } else {
            scanContext.setKeyList(valueScanCursor.getValues());
        }

        scanZSetContextMap.put(key, scanContext);

        sortedSetTableModel = new SortedSetTableModel((List<ScoredValue<String>>) scanContext.getKeyList());

        dataSize = DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0));
        loadSize = String.valueOf(scanContext.getKeyList().size());
        finished = valueScanCursor.isFinished();

    }

    @Override
    public void refreshUI() {
        consumer.accept(new Turbo5<>(len, sortedSetTableModel, dataSize, loadSize, finished));
    }

    public void reset() {
        scanZSetContextMap.put(key, new RedisScanContext<>());
    }
}
