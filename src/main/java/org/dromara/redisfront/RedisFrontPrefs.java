package org.dromara.redisfront;


import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.QSPrefs;

@Slf4j
public class RedisFrontPrefs extends QSPrefs {
    @Override
    protected String getKeyPrefix() {
        return "redis-front";
    }

    public RedisFrontPrefs(String rootPath) {
        super(rootPath);
    }


    @Override
    protected void initDefaults() {
        setDbFileName("redis-front.db");
    }

    public boolean getDBInitialized() {
        return getState().getBoolean("redis-front-databaseInitialized", false);
    }

    public void setDBInitialized(boolean databaseInitialized) {
        getState().putBoolean("redis-front-databaseInitialized", databaseInitialized);
    }


}
