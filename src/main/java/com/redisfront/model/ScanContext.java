package com.redisfront.model;

import com.redisfront.commons.func.Fn;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;

import java.util.List;

public class ScanContext<T> {
    private ScanCursor scanCursor;
    private Long limit;
    private String searchKey;
    private List<T> keys;

    public ScanCursor getScanCursor() {
        if (Fn.isNull(scanCursor)) {
            return ScanCursor.INITIAL;
        }
        return scanCursor;
    }

    public void setScanCursor(ScanCursor scanCursor) {
        this.scanCursor = scanCursor;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public ScanArgs getScanArgs() {
        return ScanArgs.Builder.matches(getSearchKey()).limit(getLimit());
    }


    public List<T> getKeyList() {
        return keys;
    }

    public void setKeyList(List<T> keys) {
        this.keys = keys;
    }
}
