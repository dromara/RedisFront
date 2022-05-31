package cn.devcms.redisfront.ui.theme;

import cn.devcms.redisfront.utils.DialogUtil;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.util.LoggingFacade;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;

/**
 * ThemeUtil
 *
 * @author Jin
 */
public class ThemeUtil {

    public static final String THEMES_PACKAGE = "/com/formdev/flatlaf/intellijthemes/themes/";

    public static void setTheme(Component c, ThemeInfo themeInfo) {
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
                DialogUtil.showInformationDialog(c, "Failed to create '" + themeInfo.lafClassName() + "'.", ex);
            }
        } else if (themeInfo.themeFile() != null) {
            FlatAnimatedLafChange.showSnapshot();

            try {
                if (themeInfo.themeFile().getName().endsWith(".properties")) {
                    FlatLaf.setup(new FlatPropertiesLaf(themeInfo.name(), themeInfo.themeFile()));
                } else
                    FlatLaf.setup(IntelliJTheme.createLaf(new FileInputStream(themeInfo.themeFile())));

//                DemoPrefs.getState().put( DemoPrefs.KEY_LAF_THEME, DemoPrefs.FILE_PREFIX + themeInfo.themeFile );
            } catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
                DialogUtil.showInformationDialog(c, "Failed to load '" + themeInfo.themeFile() + "'.", ex);
            }
        } else {
            FlatAnimatedLafChange.showSnapshot();

            IntelliJTheme.setup(ThemeUtil.class.getResourceAsStream(THEMES_PACKAGE + themeInfo.resourceName()));
//            DemoPrefs.getState().put( DemoPrefs.KEY_LAF_THEME, DemoPrefs.RESOURCE_PREFIX + themeInfo.resourceName );
        }
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

}
