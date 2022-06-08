package cn.devcms.redisfront.common.util;

import cn.devcms.redisfront.RedisFrontApplication;
import cn.devcms.redisfront.common.constant.Constant;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.util.LoggingFacade;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ThemeUtil
 *
 * @author Jin
 */
public class ThemeUtil {

    public static final String THEMES_PACKAGE = "/com/formdev/flatlaf/intellijthemes/themes/";

    public static final List<ThemeInfo> bundledThemes = new ArrayList<>();

    static {
        loadBundledThemes();
    }

    public static void changeTheme(Component c, ThemeInfo themeInfo) {
        if (themeInfo == null)
            return;
        if (themeInfo.lafClassName() != null) {
            if (themeInfo.lafClassName().equals(UIManager.getLookAndFeel().getClass().getName()))
                return;

            FlatAnimatedLafChange.showSnapshot();

            try {
                UIManager.setLookAndFeel(themeInfo.lafClassName());
            } catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
                MsgUtil.showInformationDialog(c, "Failed to create '" + themeInfo.lafClassName() + "'.", ex);
            }
        } else if (themeInfo.themeFile() != null) {
            FlatAnimatedLafChange.showSnapshot();
            try {
                if (themeInfo.themeFile().getName().endsWith(".properties")) {
                    FlatLaf.setup(new FlatPropertiesLaf(themeInfo.name(), themeInfo.themeFile()));
                } else
                    FlatLaf.setup(IntelliJTheme.createLaf(new FileInputStream(themeInfo.themeFile())));

            } catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
                MsgUtil.showInformationDialog(c, "Failed to load '" + themeInfo.themeFile() + "'.", ex);
            }
        } else {
            FlatAnimatedLafChange.showSnapshot();
            IntelliJTheme.setup(ThemeUtil.class.getResourceAsStream(THEMES_PACKAGE + themeInfo.resourceName()));
        }
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    public static void setupTheme(String[] args) {
        try {
            if (args.length > 0) {
                UIManager.setLookAndFeel(args[0]);
            } else {
                String theme = PrefUtil.getState().get(Constant.KEY_THEME, FlatDarculaLaf.class.getName());
                if (theme.startsWith("R_")) {
                    IntelliJTheme.setup(ThemeUtil.class.getResourceAsStream(THEMES_PACKAGE + theme.replace("R_", "")));
                } else {
                    UIManager.setLookAndFeel(theme);
                }
            }
        } catch (Throwable ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
            FlatLightLaf.setup();
        }
    }


    public record ThemeInfo(String name, String resourceName, boolean dark, String license, String licenseFile,
                            String sourceCodeUrl, String sourceCodePath, File themeFile, String lafClassName) {
        @Override
        public String toString() {
            return name;
        }
    }

    @SuppressWarnings("unchecked")
    static void loadBundledThemes() {
        Map<String, Object> json;
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(RedisFrontApplication.class.getResourceAsStream("themes.json")), StandardCharsets.UTF_8)) {
            json = (Map<String, Object>) Json.parse(reader);
        } catch (IOException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
            return;
        }

        for (Map.Entry<String, Object> e : json.entrySet()) {
            String resourceName = e.getKey();
            Map<String, String> value = (Map<String, String>) e.getValue();
            String name = value.get("name");
            boolean dark = Boolean.parseBoolean(value.get("dark"));
            String license = value.get("license");
            String licenseFile = value.get("licenseFile");
            String sourceCodeUrl = value.get("sourceCodeUrl");
            String sourceCodePath = value.get("sourceCodePath");
            bundledThemes.add(new ThemeInfo(name, resourceName, dark, license, licenseFile, sourceCodeUrl, sourceCodePath, null, null));
        }
    }

}
