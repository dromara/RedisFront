package cn.devcms.redisfront.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class ConfigInfo {

    private ThemeConfig themeConfig;
    private FontConfig fontConfig;
    private LanguageConfig languageConfig;
    private List<ConnectInfo> connectInfoList;
    @Data
    public class ThemeConfig {
        private String themeName;
        private String themeColor;
    }
    @Data
    public class FontConfig {
        private String fontName;
        private String fontSize;
    }
    @Data
    public class LanguageConfig {
        private String language;
    }
}
