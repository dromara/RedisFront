package org.dromara.redisfront.ui.scanner;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamMessage;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.scanner.context.RedisScanContext;
import org.dromara.redisfront.model.table.StreamTableModel;
import org.dromara.redisfront.model.turbo.Turbo5;
import org.dromara.redisfront.service.RedisStreamService;
import org.dromara.redisfront.ui.scanner.RedisDataScanner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class StreamRedisDataScanner implements RedisDataScanner {
    private final RedisConnectContext redisConnectContext;
    private final Consumer<Turbo5<Long, StreamTableModel, String, String, Boolean>> consumer;
    private final Map<String, RedisScanContext<StreamMessage<String, String>>> xRangeContextMap;
    private final ResourceBundle tr;
    @Setter
    @Getter
    private String key;
    @Setter
    @Getter
    private String skey;

    private StreamTableModel streamTableModel;
    private Long len;
    private String dataSize;
    private String loadSize;
    private Boolean finished;

    public StreamRedisDataScanner(RedisConnectContext redisConnectContext, String key, Consumer<Turbo5<Long, StreamTableModel, String, String, Boolean>> consumer, ResourceBundle tr) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
        this.key = key;
        this.tr = tr;
        this.xRangeContextMap = new LinkedHashMap<>();
    }

    @Override
    public void fetchData(String fetchKey) {

        len = RedisStreamService.service.xlen(redisConnectContext, key);

        var xRangeContext = xRangeContextMap.getOrDefault(key, new RedisScanContext<>());

        var lastSearchKey = xRangeContext.getSearchKey();
        if (StrUtil.isNotEmpty(skey)) {
            xRangeContext.setSearchKey(skey);
        } else {
            xRangeContext.setSearchKey("*");
        }
        xRangeContext.setLimit(500L);
        var start = Long.parseLong(xRangeContext.getScanCursor().getCursor());
        var stop = start + (xRangeContext.getLimit() - 1);
        var value = RedisStreamService.service.xrange(redisConnectContext, key, Range.unbounded(), Limit.create(start, stop));

        var nextCursor = start + xRangeContext.getLimit();
        if (nextCursor >= len) {
            xRangeContext.setScanCursor(new ScanCursor(String.valueOf(len), true));
        } else {
            xRangeContext.setScanCursor(new ScanCursor(String.valueOf(nextCursor), false));
        }

        if (RedisFrontUtils.equal(xRangeContext.getSearchKey(), lastSearchKey) && RedisFrontUtils.isNotEmpty(xRangeContext.getKeyList())) {
            if (xRangeContext.getKeyList().size() >= 2000) {
                System.gc();
                throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
            }
            xRangeContext.getKeyList().addAll(value);
        } else {
            xRangeContext.setKeyList(value);
        }

        xRangeContextMap.put(key, xRangeContext);

        streamTableModel = new StreamTableModel((List<StreamMessage<String, String>>) xRangeContext.getKeyList());

        dataSize = DataSizeUtil.format(xRangeContext.getKeyList().stream().map(e -> RedisFrontUtils.getByteSize(e.getBody())).reduce(Integer::sum).orElse(0));
        loadSize = String.valueOf(xRangeContext.getKeyList().size());
        finished = xRangeContext.getScanCursor().isFinished();
    }

    @Override
    public void refreshUI() {
        consumer.accept(new Turbo5<>(len, streamTableModel, dataSize, loadSize, finished));
    }

    public void reset() {
        xRangeContextMap.put(key, new RedisScanContext<>());
    }
}
