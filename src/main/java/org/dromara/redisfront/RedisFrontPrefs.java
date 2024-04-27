package org.dromara.redisfront;


import org.dromara.quickswing.ui.app.AppPrefs;

public class RedisFrontPrefs extends AppPrefs {
    public RedisFrontPrefs(String rootPath) {
        super(rootPath);
    }


    @Override
    protected void initDefaults() {

    }

    public Boolean isRememberMe() {
        return getState().getBoolean("rememberMe", false);
    }

    public void setRememberMe(Boolean rememberMe) {
        getState().putBoolean("rememberMe", rememberMe);
    }

    public String getUsername() {
        return getState().get("username","");
    }

    public void setUsername(String username) {
        getState().put("username",username);
    }

    public String getPassword() {
        return getState().get("password","");
    }

    public void setPassword(String password) {
        getState().put("password",password);
    }
}