package org.dromara.redisfront.commons.scanner.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.StreamMessage;
import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Setter
@Getter
public class RedisScanContext<T> {
    private static final long DEFAULT_SCAN_LIMIT = 100L;

    private ScanCursor scanCursor;
    private Long limit = DEFAULT_SCAN_LIMIT;
    private String searchKey;
    private Collection<T> keys = Collections.emptyList();

    public void setScanCursor(ScanCursor scanCursor) {
        if (this.scanCursor == null) {
            this.scanCursor = new ScanCursor(scanCursor.getCursor(), scanCursor.isFinished());
        }
        this.scanCursor.setCursor(scanCursor.getCursor());
        this.scanCursor.setFinished(scanCursor.isFinished());
    }

    public ScanCursor getScanCursor() {
        return Optional.ofNullable(scanCursor).orElse(ScanCursor.INITIAL);
    }

    public ScanArgs getScanArgs() {
        return new ScanArgs()
                .match(getSearchKey())
                .limit(limit);
    }

    public String getSearchKey() {
        return RedisFrontUtils.isNotEmpty(searchKey) ? searchKey : "*";
    }

    public String getLoadSize() {
        return String.valueOf(keys.size());
    }

    public String getDataLength() {
        Integer sum = keys.stream()
                .map(e -> switch (e) {
                    case String s -> s.getBytes().length;
                    case Map.Entry<?, ?> entry -> RedisFrontUtils.getByteSize(entry.getValue());
                    case StreamMessage<?, ?> message -> RedisFrontUtils.getByteSize(message.getBody());
                    case ScoredValue<?> scoredValue -> RedisFrontUtils.getByteSize(scoredValue.getValue());
                    case null, default -> 0;
                })
                .reduce(Integer::sum)
                .orElse(0);
        return DataSizeUtil.format(sum);
    }

    public Collection<T> getKeyList() {
        return keys;
    }

    public void setKeyList(Collection<T> keys) {
        if (CollUtil.isNotEmpty(keys)) {
            if (CollUtil.isNotEmpty(this.keys)) {
                this.keys.addAll(keys);
            } else {
                this.keys = keys;
            }
        } else {
            this.keys = Collections.emptyList();
        }
    }

}
