package org.dromara.redisfront.commons.scanner.core;

import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamMessage;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.scanner.AbstractRedisDataScanner;
import org.dromara.redisfront.commons.scanner.handler.ScanDataRefreshHandler;
import org.dromara.redisfront.commons.scanner.model.ScanDataResult;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.StreamTableModel;
import org.dromara.redisfront.service.RedisStreamService;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

@Setter
@Getter
public class StreamRedisDataScanner extends AbstractRedisDataScanner<StreamMessage<String, String>, StreamTableModel> {

    private String key;
    private StreamTableModel streamTableModel;
    private Long len;
    private String dataSize;
    private String loadSize;
    private Boolean finished;

    public StreamRedisDataScanner(RedisConnectContext redisConnectContext, String key, ScanDataRefreshHandler<ScanDataResult<StreamTableModel>> scanDataRefreshHandler, ResourceBundle tr) {
        super(redisConnectContext, tr, scanDataRefreshHandler);
        this.key = key;
    }

    @Override
    public void fetchData(String fetchKey) {

        var scanContext = getContextByKey(key);


        if (RedisFrontUtils.isNotEmpty(scanContext.getKeyList()) && scanContext.getKeyList().size() >= 1000) {
            throw new RedisFrontException(tr.getString("DataViewForm.redisFrontException.message"));
        }

        var start = Long.parseLong(scanContext.getScanCursor().getCursor());
        var stop = start + (scanContext.getLimit() - 1);

        var value = RedisStreamService.service.xrange(redisConnectContext, key, Range.unbounded(), Limit.create(start, stop));

        var nextCursor = start + scanContext.getLimit();

        if (nextCursor >= len) {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(len), true));
        } else {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(nextCursor), false));
        }

        scanContext.setKeyList(value);

        updateState(scanContext);
    }

    @Override
    protected StreamTableModel createModel(Collection<StreamMessage<String, String>> data) {
        return new StreamTableModel((List<StreamMessage<String, String>>) data);
    }

    @Override
    protected Long getLen() {
        return RedisStreamService.service.xlen(redisConnectContext, key);
    }

}
