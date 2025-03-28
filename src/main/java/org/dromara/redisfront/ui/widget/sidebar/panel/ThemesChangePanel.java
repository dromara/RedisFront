package org.dromara.redisfront.ui.widget.sidebar.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.jthemedetecor.OsThemeDetector;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;


public class ThemesChangePanel extends JPanel {
    public final static OsThemeDetector DETECTOR = OsThemeDetector.getDetector();
    public ThemesChangePanel() {
        initializeUI();
        ThemesChangePanel.changeMode(ThemesChangePanel.isDark());
        ThemesChangePanel.DETECTOR.registerListener(isDark -> SwingUtilities.invokeLater(() -> changeMode(isDark)));
    }



    private Icon createIcon(String path) {
        FlatSVGIcon icon = new FlatSVGIcon(path, 0.7f);
        FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
        colorFilter.add(Color.decode("#969696"), Color.decode("#FAFAFA"), Color.decode("#969696"));
        icon.setColorFilter(colorFilter);
        return icon;
    }

    private void initializeUI() {
        putClientProperty(FlatClientProperties.STYLE,  "background:null");
        setLayout(new MigLayout("al center", "[fill,200]", "fill"));
        JPanel panel = new JPanel(new MigLayout("fill", "[fill]10[fill]", "fill"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:999;"
                + "background:darken($RedisFront.main.background,5%)");
        JButton buttonLight = new JButton(createIcon("svg/light.svg"));
        JButton buttonDark = new JButton(createIcon("svg/dark.svg"));
        buttonLight.addActionListener(_ -> changeMode(false));
        buttonDark.addActionListener(_ -> changeMode(true));
        buttonLight.putClientProperty(FlatClientProperties.STYLE, "arc:999;"
                + "[dark]background:null;"
                + "[light]background:$RedisFront.main.background;"
                + "borderWidth:0;"
                + "focusWidth:0;"
                + "innerFocusWidth:0;"
                + "margin:3,5,3,5");

        buttonDark.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;"
                + "[dark]background:$RedisFront.main.background;"
                + "[light]background:null;"
                + "borderWidth:0;"
                + "focusWidth:0;"
                + "innerFocusWidth:0;"
                + "margin:3,5,3,5");
        panel.add(buttonDark);
        panel.add(buttonLight);
        add(panel);
    }

    public static boolean isDark(){
        return DETECTOR.isDark();
    }

    public static void changeMode(boolean dark) {
        if (dark != FlatLaf.isLafDark()) {
            if (dark) {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacDarkLaf.setup();
                    FlatLaf.updateUI();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                });
            } else {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacLightLaf.setup();
                    FlatLaf.updateUI();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                });
            }
        }
    }
}
