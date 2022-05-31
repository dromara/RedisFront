package cn.devcms.redisfront.model;


import java.io.File;

public record ThemeInfo(String name, String resourceName, boolean dark, String license, String licenseFile,
                        String sourceCodeUrl, String sourceCodePath, File themeFile, String lafClassName) {

    @Override
    public String toString() {
        return name;
    }
}
