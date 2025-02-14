package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.fetch.impl;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.context.RedisScanContext;
import org.dromara.redisfront.model.table.SetTableModel;
import org.dromara.redisfront.model.turbo.Turbo5;
import org.dromara.redisfront.service.RedisSetService;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.fetch.DataFetcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SetDataFetcher implements DataFetcher {
    private final RedisConnectContext redisConnectContext;
    private final Consumer<Turbo5<Long, SetTableModel, String, String, Boolean>> consumer;
    private final Map<String, RedisScanContext<String>> scanSetContextMap;
    private final ResourceBundle tr;
    @Setter
    @Getter
    private String key;
    @Setter
    @Getter
    private String skey;

    private SetTableModel setTableModel;
    private Long len;
    private String dataSize;
    private String loadSize;
    private Boolean finished;

    public SetDataFetcher(RedisConnectContext redisConnectContext, String key, Consumer<Turbo5<Long, SetTableModel, String, String, Boolean>> consumer, ResourceBundle tr) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
        this.key = key;
        this.tr = tr;
        this.scanSetContextMap = new LinkedHashMap<>();
    }

    @Override
    public void fetchData() {
        len = RedisSetService.service.scard(redisConnectContext, key);
        var scanContext = scanSetContextMap.getOrDefault(key, new RedisScanContext<>());

        var lastSearchKey = scanContext.getSearchKey();
        if (StrUtil.isNotEmpty(skey)) {
            scanContext.setSearchKey(skey);
        } else {
            scanContext.setSearchKey("*");
        }
        scanContext.setLimit(500L);

        var valueScanCursor = RedisSetService.service.sscan(redisConnectContext, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(valueScanCursor);

        if (RedisFrontUtils.equal(scanContext.getSearchKey(), lastSearchKey) && RedisFrontUtils.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 2000) {
                System.gc();
                throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(valueScanCursor.getValues());
        } else {
            scanContext.setKeyList(valueScanCursor.getValues());
        }

        scanSetContextMap.put(key, scanContext);
        setTableModel = new SetTableModel(scanContext.getKeyList());
        dataSize = DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0));
        loadSize = String.valueOf(scanContext.getKeyList().size());
        finished = valueScanCursor.isFinished();

    }

    @Override
    public void loadData() {
        consumer.accept(new Turbo5<>(len, setTableModel, dataSize, loadSize, finished));
    }

    public void reset() {
        scanSetContextMap.put(key, new RedisScanContext<>());
    }
}
