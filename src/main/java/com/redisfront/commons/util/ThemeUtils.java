package com.redisfront.commons.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.theme.RedisFrontLightLaf;

import javax.swing.*;
import javax.swing.text.StyleContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class ThemeUtils {

    public static final String THEMES_PACKAGE = "/com/formdev/flatlaf/intellijthemes/themes/";

    public static final List<ThemeInfo> bundledThemes = new ArrayList<>();

    static {
        loadBundledThemes();
    }

    public static void changeTheme(ThemeInfo themeInfo) {
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
                AlertUtils.showInformationDialog("Failed to create '" + themeInfo.lafClassName() + "'.", ex);
            }
        } else if (themeInfo.themeFile() != null) {
            FlatAnimatedLafChange.showSnapshot();
            try {
                if (themeInfo.themeFile().getName().endsWith(".properties")) {
                    FlatLaf.setup(new FlatPropertiesLaf(themeInfo.name(), themeInfo.themeFile()));
                } else
                    FlatLaf.setup(IntelliJTheme.createLaf(new FileInputStream(themeInfo.themeFile())));

            } catch (Exception ex) {
                AlertUtils.showInformationDialog("Failed to load '" + themeInfo.themeFile() + "'.", ex);
            }
        } else {
            FlatAnimatedLafChange.showSnapshot();
            IntelliJTheme.setup(ThemeUtils.class.getResourceAsStream(THEMES_PACKAGE + themeInfo.resourceName()));
        }
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    public static void fontInit() {
        var font = UIManager.getFont("defaultFont");
        var fontSizeStr = PrefUtils.getState().get(Const.KEY_FONT_SIZE, String.valueOf(font.getSize()));
        var fontNameStr = PrefUtils.getState().get(Const.KEY_FONT_NAME, font.getFontName());
        var newFont = StyleContext.getDefaultStyleContext().getFont(fontNameStr, font.getStyle(), Integer.parseInt(fontSizeStr));
        UIManager.put("defaultFont", FlatUIUtils.nonUIResource(newFont));
    }

    public static void setupTheme(String[] args) {
        try {
            if (args.length > 0) {
                UIManager.setLookAndFeel(args[0]);
            } else {
                String theme = PrefUtils.getState().get(Const.KEY_THEME, RedisFrontLightLaf.class.getName());
                if (theme.startsWith("R_")) {
                    IntelliJTheme.setup(ThemeUtils.class.getResourceAsStream(THEMES_PACKAGE + theme.replace("R_", "")));
                } else {
                    UIManager.setLookAndFeel(theme);
                }
            }
            fontInit();
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

    static void loadBundledThemes() {
        Map<String, Object> json;
        try (var reader = new InputStreamReader(Objects.requireNonNull(RedisFrontApplication.class.getResourceAsStream("themes.json")), StandardCharsets.UTF_8)) {
            json = (Map<String, Object>) Json.parse(reader);
        } catch (IOException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
            return;
        }

        for (Map.Entry<String, Object> e : json.entrySet()) {
            var resourceName = e.getKey();
            var value = (Map<String, String>) e.getValue();
            var name = value.get("name");
            var dark = Boolean.parseBoolean(value.get("dark"));
            var license = value.get("license");
            var licenseFile = value.get("licenseFile");
            var sourceCodeUrl = value.get("sourceCodeUrl");
            var sourceCodePath = value.get("sourceCodePath");
            bundledThemes.add(new ThemeInfo(name, resourceName, dark, license, licenseFile, sourceCodeUrl, sourceCodePath, null, null));
        }
    }

}
