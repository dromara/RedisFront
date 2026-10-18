package org.dromara.redisfront.i18n;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * 双语资源包的一致性守卫。
 * <p>
 * 直接读 properties 文件而不是 ResourceBundle：getBundle 会沿 locale 回退链把
 * 父包的 key 也算进来，用它做对齐检查永远通过，测不出真实差异。
 */
public class ResourceBundleConsistencyTest {

    private static final String BASE = "/org/dromara/redisfront/RedisFront.properties";
    private static final String ZH_CN = "/org/dromara/redisfront/RedisFront_zh_CN.properties";

    private static Properties load(String path) throws IOException {
        try (InputStream stream = ResourceBundleConsistencyTest.class.getResourceAsStream(path)) {
            Assertions.assertNotNull(stream, "资源包不存在: " + path);
            Properties properties = new Properties();
            // 用 UTF-8 Reader：文件里的中文是 \\uXXXX 转义，load 内部会自行反转义
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        }
    }

    @Test
    void zh_CN_should_define_every_key_the_default_bundle_defines() throws IOException {
        Properties english = load(BASE);
        Properties chinese = load(ZH_CN);

        List<String> missing = english.stringPropertyNames().stream()
                .filter(key -> !chinese.containsKey(key))
                .toList();

        Assertions.assertTrue(missing.isEmpty(), "中文包缺少以下 key: " + missing);
    }

    @Test
    void default_bundle_should_define_every_key_zh_CN_defines() throws IOException {
        Properties english = load(BASE);
        Properties chinese = load(ZH_CN);

        List<String> extra = chinese.stringPropertyNames().stream()
                .filter(key -> !english.containsKey(key))
                .toList();

        Assertions.assertTrue(extra.isEmpty(), "中文包多出以下 key（默认包没有）: " + extra);
    }

    /**
     * 中文包大部分用 \\uXXXX 转义存储，个别新增条目是原生 UTF-8。
     * 这里验证两种形式都能正确还原成中文，防止某次编辑把文件写成其它编码导致界面乱码。
     */
    @Test
    void escaped_chinese_values_should_decode_correctly() throws IOException {
        Properties chinese = load(ZH_CN);

        // 转义形式
        Assertions.assertEquals("设置", chinese.getProperty("SettingDialog.Window.Title"));
        Assertions.assertEquals("证书", chinese.getProperty("AddConnectDialog.sslCertLabel.Title"));
        Assertions.assertEquals("key不能以 '%s' 开头或结尾！",
                chinese.getProperty("AddKeyDialog.requireSeparator.text"));
        // 原生 UTF-8 形式
        Assertions.assertEquals("更多信息", chinese.getProperty("MainTabView.moreInfo.tooltip.text"));
        Assertions.assertEquals("加载信息中...", chinese.getProperty("RedisInfoView.loading.message"));
    }

    /**
     * 代码里用 String.format 拼这些文案，占位符丢了会直接抛
     * MissingFormatArgumentException 或把 null 拼进提示。
     */
    @Test
    void format_placeholders_should_survive_in_both_bundles() throws IOException {
        Properties english = load(BASE);
        Properties chinese = load(ZH_CN);

        List<String> formattedKeys = List.of(
                "AddKeyDialog.redisVersion.text",
                "AddKeyDialog.requireSeparator.text",
                "AddConnectDialog.title0",
                "AddConnectDialog.title1",
                "PubSubPageView.numLabel.messageCount"
        );

        for (String key : formattedKeys) {
            String en = english.getProperty(key);
            String zh = chinese.getProperty(key);
            Assertions.assertNotNull(en, "默认包缺少 " + key);
            Assertions.assertNotNull(zh, "中文包缺少 " + key);
            Assertions.assertTrue(en.contains("%"), key + " 的英文占位符丢了");
            Assertions.assertTrue(zh.contains("%"), key + " 的中文占位符丢了");
        }
    }
}
