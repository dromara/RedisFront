package cn.devcms.redisfront.ui.theme;

public record ColorInfo(String name, String key) {

    @Override
    public String toString() {
        return name;
    }
}
