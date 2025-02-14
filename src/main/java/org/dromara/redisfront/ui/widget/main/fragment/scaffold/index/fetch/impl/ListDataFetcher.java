package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.fetch.impl;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import io.lettuce.core.ScanCursor;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.context.RedisScanContext;
import org.dromara.redisfront.model.table.ListTableModel;
import org.dromara.redisfront.model.turbo.Turbo5;
import org.dromara.redisfront.service.RedisListService;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.fetch.DataFetcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ListDataFetcher implements DataFetcher {
    private final RedisConnectContext redisConnectContext;
    private final Consumer<Turbo5<Long, ListTableModel, String, String, Boolean>> consumer;
    private final Map<String, RedisScanContext<String>> scanListContextMap;
    private final ResourceBundle tr;
    @Setter
    @Getter
    private String key;
    @Setter
    @Getter
    private String skey;

    private ListTableModel listTableModel;
    private Long len;
    private String dataSize;
    private String loadSize;
    private Boolean finished;

    public ListDataFetcher(RedisConnectContext redisConnectContext, String key, Consumer<Turbo5<Long, ListTableModel, String, String, Boolean>> consumer, ResourceBundle tr) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
        this.key = key;
        this.tr = tr;
        this.scanListContextMap = new LinkedHashMap<>();
    }

    @Override
    public void fetchData() {

        len = RedisListService.service.llen(redisConnectContext, key);

        var scanContext = scanListContextMap.getOrDefault(key, new RedisScanContext<>());

        var lastSearchKey = scanContext.getSearchKey();
        if (StrUtil.isNotBlank(skey)) {
            scanContext.setSearchKey(skey);
        } else {
            scanContext.setSearchKey("*");
        }
        scanContext.setLimit(500L);

        var start = Long.parseLong(scanContext.getScanCursor().getCursor());
        var stop = start + (scanContext.getLimit() - 1);
        var value = RedisListService.service.lrange(redisConnectContext, key, start, stop);

        var nextCursor = start + scanContext.getLimit();
        if (nextCursor >= len) {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(len), true));
        } else {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(nextCursor), false));
        }

        if (RedisFrontUtils.equal(scanContext.getSearchKey(), lastSearchKey) && RedisFrontUtils.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 2000) {
                System.gc();
                throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(value);
        } else {
            scanContext.setKeyList(value);
        }

        scanListContextMap.put(key, scanContext);

        if (RedisFrontUtils.isNotEmpty(skey)) {
            var findList = scanContext.getKeyList().stream().filter(s -> s.contains(skey)).collect(Collectors.toList());
            listTableModel = new ListTableModel(findList);
        } else {
            listTableModel = new ListTableModel(scanContext.getKeyList());
        }

        dataSize = DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0));
        loadSize = String.valueOf(scanContext.getKeyList().size());
        finished = scanContext.getScanCursor().isFinished();

    }

    @Override
    public void loadData() {
        consumer.accept(new Turbo5<>(len, listTableModel, dataSize, loadSize, finished));
    }

    public void reset() {
        scanListContextMap.put(key, new RedisScanContext<>());
    }
}
