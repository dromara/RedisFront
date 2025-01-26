package org.dromara.redisfront.model;

import lombok.Setter;
import org.dromara.redisfront.commons.func.Fn;

/**
 * DbInfo
 *
 * @author Jin
 */
public class DbInfo {
    private String dbName;
    private Integer dbIndex;
    @Setter
    private Long dbSize;

    public DbInfo(String dbName, Integer dbIndex) {
        this.dbName = dbName;
        this.dbIndex = dbIndex;
    }

    public String dbName() {
        return dbName;
    }

    public DbInfo setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public Integer dbIndex() {
        return dbIndex;
    }

    public DbInfo setDbIndex(Integer dbIndex) {
        this.dbIndex = dbIndex;
        return this;
    }

    public Long dbSize() {
        return dbSize;
    }

    @Override
    public String toString() {
        return Fn.isNotEmpty(dbSize) ? dbName + " [" + dbSize + "]" : dbName;
    }
}
