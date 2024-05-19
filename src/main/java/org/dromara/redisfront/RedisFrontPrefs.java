package org.dromara.redisfront;


import org.dromara.quickswing.ui.app.QSPrefs;

public class RedisFrontPrefs extends QSPrefs {
    public RedisFrontPrefs(String rootPath) {
        super(rootPath);
    }


    @Override
    protected void initDefaults() {

    }

    public boolean getDBInitialized() {
        return getState().getBoolean("databaseInitialized", false);
    }

    public void setDBInitialized(boolean databaseInitialized) {
        getState().putBoolean("databaseInitialized", databaseInitialized);
    }
}