package org.dromara.redisfront.model.context;

import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.Fn;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;

import java.util.List;

public class ScanContext<T> {
    @Setter
    private ScanCursor scanCursor;
    @Setter
    @Getter
    private Long limit;
    @Setter
    private String searchKey;
    private List<T> keys;

    public ScanCursor getScanCursor() {
        if (Fn.isNull(scanCursor)) {
            return ScanCursor.INITIAL;
        }
        return scanCursor;
    }

    public String getSearchKey() {
        return Fn.isNotEmpty(searchKey) ? searchKey : "*";
    }

    public ScanArgs getScanArgs() {
        return MyScanArgs.Builder.matches(getSearchKey()).limit(getLimit());
    }

    public static class MyScanArgs extends ScanArgs {
        private Long count;
        private String match;

        public static class Builder {

            private Builder() {
            }

            public static ScanArgs limit(long count) {
                return new MyScanArgs().limit(count);
            }

            public static ScanArgs matches(String matches) {
                return new MyScanArgs().match(matches);
            }

            public static ScanArgs matches(byte[] matches) {
                return new MyScanArgs().match(matches);
            }

        }
        @Override
        public ScanArgs match(String match) {
            this.match = match;
            return super.match(match);
        }

        @Override
        public ScanArgs limit(long count) {
            this.count = count;
            return super.limit(count);
        }

        public String getCommandStr() {
            return " MATCH ".concat(match).concat(" ").concat("COUNT ").concat(count.toString());
        }
    }


    public List<T> getKeyList() {
        return keys;
    }

    public void setKeyList(List<T> keys) {
        this.keys = keys;
    }
}
