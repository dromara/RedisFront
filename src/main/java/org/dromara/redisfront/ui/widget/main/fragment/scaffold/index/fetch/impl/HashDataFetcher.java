package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.fetch.impl;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.context.RedisScanContext;
import org.dromara.redisfront.model.table.HashTableModel;
import org.dromara.redisfront.model.turbo.Turbo5;
import org.dromara.redisfront.service.RedisHashService;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.fetch.DataFetcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class HashDataFetcher implements DataFetcher {
    private final RedisConnectContext redisConnectContext;
    private final Consumer<Turbo5<Long, HashTableModel, String, String, Boolean>> consumer;
    private final Map<String, RedisScanContext<Map.Entry<String, String>>> scanHashContextMap;
    private final ResourceBundle tr;
    @Setter
    @Getter
    private String key;
    @Setter
    @Getter
    private String hkey;

    private HashTableModel hashTableModel;
    private Long len;
    private String dataSize;
    private String loadSize;
    private Boolean finished;

    public HashDataFetcher(RedisConnectContext redisConnectContext, String key, Consumer<Turbo5<Long, HashTableModel, String, String, Boolean>> consumer, ResourceBundle tr) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
        this.key = key;
        this.tr = tr;
        this.scanHashContextMap = new LinkedHashMap<>();
    }

    @Override
    public void fetchData() {
        len = RedisHashService.service.hlen(redisConnectContext, key);
        var scanContext = scanHashContextMap.getOrDefault(key, new RedisScanContext<>());
        var lastSearchKey = scanContext.getSearchKey();
        if (StrUtil.isEmpty(hkey)) {
            scanContext.setSearchKey(hkey);
        } else {
            scanContext.setSearchKey("*");
        }

        scanContext.setLimit(500L);

        var mapScanCursor = RedisHashService.service.hscan(redisConnectContext, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(mapScanCursor);

        if (RedisFrontUtils.equal(scanContext.getSearchKey(), lastSearchKey) && RedisFrontUtils.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 1000) {
                System.gc();
                throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(new ArrayList<>(mapScanCursor.getMap().entrySet()));
        } else {
            scanContext.setKeyList(new ArrayList<>(mapScanCursor.getMap().entrySet()));
        }

        scanHashContextMap.put(key, scanContext);

        hashTableModel = new HashTableModel(scanContext.getKeyList());
        dataSize = DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0));
        loadSize = String.valueOf(scanContext.getKeyList().size());
        finished = mapScanCursor.isFinished();
    }

    @Override
    public void loadData() {
        consumer.accept(new Turbo5<>(len, hashTableModel, dataSize, loadSize, finished));
    }

    public void reset() {
        scanHashContextMap.put(key, new RedisScanContext<>());
    }
}
