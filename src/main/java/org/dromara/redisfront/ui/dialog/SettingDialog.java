package org.dromara.redisfront.ui.dialog;


import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.quickswing.ui.app.QSDialog;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

public class SettingDialog extends QSDialog<RedisFrontWidget> {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel fontPanel;
    private JComboBox<String> fontSizeComboBox;
    private JComboBox<String> fontNameComboBox;
    private JLabel fontLabel;
    private JLabel fontSizeLabel;
    private JPanel languagePanel;
    private JLabel languageLabel;
    private JComboBox<Map.Entry<String, String>> languageComboBox;
    private final Preferences preferences;


    public static void showSettingDialog(RedisFrontWidget owner) {
        var settingDialog = new SettingDialog(owner);
        settingDialog.setMinimumSize(new Dimension(400, 400));
        settingDialog.setLocationRelativeTo(owner);
        settingDialog.pack();
        settingDialog.setVisible(true);
    }

    public SettingDialog(RedisFrontWidget owner) {
        super(owner, owner.$tr("SettingDialog.Window.Title"), true);
        preferences = getOwner().getPrefs().getState();
        $$$setupUI$$$();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(_ -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        initFontComboBox();
        initFontSizeComboBox();
        initLanguageComboBox();
        initLabelText();
    }

    private void initLanguageComboBox() {
        Map<String, String> languages = new HashMap<>() {
            {
                put("简体中文", Locale.SIMPLIFIED_CHINESE.toLanguageTag());
                put("English", Locale.ENGLISH.toLanguageTag());
            }
        };
        languages.entrySet().forEach(languageComboBox::addItem);
        languageComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((Map.Entry<?, ?>) value).getKey(), index, isSelected, cellHasFocus);
            }
        });
        languageComboBox.setSelectedItem(languages
                .entrySet()
                .stream()
                .filter(e -> {
                    String languageTag = preferences.get(Constants.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
                    return RedisFrontUtils.equal(e.getValue(), languageTag);
                }).findAny().orElseThrow());
    }

    private void initLabelText() {
        fontLabel.setText(getOwner().$tr("SettingDialog.FontLabel.Title"));
        fontSizeLabel.setText(getOwner().$tr("SettingDialog.FontSizeLabel.Title"));
        languageLabel.setText(getOwner().$tr("SettingDialog.LanguageLabel.Title"));
    }

    private void initFontSizeComboBox() {
        var fontSizes = new ArrayList<>(Arrays.asList(
                "10", "11", "12", "13", "14", "15", "16", "17", "18"));
        for (String fontSize : fontSizes) {
            fontSizeComboBox.addItem(fontSize);
        }
        fontSizeComboBox.addActionListener(e -> {
            String fontSizeStr = (String) fontSizeComboBox.getSelectedItem();
            if (RedisFrontUtils.equal(fontSizeStr, preferences.get(Constants.KEY_FONT_SIZE, getDefaultFontSize()))) {
                return;
            }
            this.updateFontSizeHandler(fontSizeStr);
        });
        fontSizeComboBox.setSelectedItem(preferences.get(Constants.KEY_FONT_SIZE, getDefaultFontSize()));
    }

    //获取默认字体大小
    private String getDefaultFontSize() {
        return String.valueOf(UIManager.getFont("defaultFont").getSize());
    }

    private void initFontComboBox() {
        //获取系统全部字体
        var graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        var availableFontFamilyNames = List.of(graphicsEnvironment.getAvailableFontFamilyNames());
        var families = new HashSet<>(Arrays.asList(
                "Arial", "Cantarell", "Comic Sans MS", "Courier New", "DejaVu Sans",
                "Dialog", "Liberation Sans", "Monospaced", "Microsoft YaHei UI", "Noto Sans", "Roboto",
                "SansSerif", "Segoe UI", "Serif", "Tahoma", "Ubuntu", "Verdana"));
        families.add(UIManager.getFont("defaultFont").getFontName());
        //移除列表中不存在的字体
        families.removeIf(f -> !availableFontFamilyNames.contains(f));
        families.forEach(fontNameComboBox::addItem);
        //列表文字样式渲染
        fontNameComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Font font = UIManager.getFont("defaultFont");
                Font newFont = StyleContext.getDefaultStyleContext().getFont((String) value, font.getStyle(), font.getSize());
                newFont = FlatUIUtils.nonUIResource(newFont);
                setFont(newFont);
                return this;
            }
        });
        fontNameComboBox.addActionListener(e -> {
            String fontFamily = (String) fontNameComboBox.getSelectedItem();
            if (RedisFrontUtils.equal(fontFamily, preferences.get(Constants.KEY_FONT_NAME, getDefaultFontFamily()))) {
                return;
            }
            this.updateFontNameHandler(fontFamily);
        });
        fontNameComboBox.setSelectedItem(preferences.get(Constants.KEY_FONT_NAME, getDefaultFontFamily()));
    }

    //获取默认字体大小
    private String getDefaultFontFamily() {
        return String.valueOf(UIManager.getFont("defaultFont").getFontName());
    }


    private void updateFontSizeHandler(String fontSize) {
        var font = UIManager.getFont("defaultFont");
        var newFont = font.deriveFont(Float.parseFloat(fontSize));
        UIManager.put("defaultFont", newFont);
        FlatLaf.updateUI();
    }

    private void updateFontNameHandler(String fontFamily) {
        FlatAnimatedLafChange.showSnapshot();
        var font = UIManager.getFont("defaultFont");
        var newFont = StyleContext.getDefaultStyleContext().getFont(fontFamily, font.getStyle(), font.getSize());
        newFont = FlatUIUtils.nonUIResource(newFont);
        UIManager.put("defaultFont", newFont);
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private void onOK() {
        //字体名称
        String fontFamily = (String) fontNameComboBox.getSelectedItem();
        preferences.put(Constants.KEY_FONT_NAME, fontFamily);
        //字体大小
        String fontSizeStr = (String) fontSizeComboBox.getSelectedItem();
        preferences.put(Constants.KEY_FONT_SIZE, fontSizeStr);

        //语言
        var newLanguage = (Map.Entry<?, ?>) languageComboBox.getSelectedItem();
        var oldLanguage = preferences.get(Constants.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
        assert newLanguage != null;
        if (RedisFrontUtils.notEqual(newLanguage.getValue(), oldLanguage)) {
            Locale.setDefault(Locale.forLanguageTag((String) newLanguage.getValue()));
            preferences.put(Constants.KEY_LANGUAGE, (String) newLanguage.getValue());
            FlatLaf.updateUI();
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        languagePanel = new JPanel();
        languagePanel.setBorder(new TitledBorder(getOwner().$tr("SettingDialog.LanguagePanel.Title")));
        fontPanel = new JPanel();
        fontPanel.setBorder(new TitledBorder(getOwner().$tr("SettingDialog.FontPanel.Title")));
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("确认");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("取消");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        fontPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(fontPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        fontPanel.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        fontSizeComboBox = new JComboBox();
        Font fontSizeComboBoxFont = this.$$$getFont$$$(null, -1, 14, fontSizeComboBox.getFont());
        if (fontSizeComboBoxFont != null) fontSizeComboBox.setFont(fontSizeComboBoxFont);
        panel4.add(fontSizeComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(180, -1), null, null, 0, false));
        fontSizeLabel = new JLabel();
        Font fontSizeLabelFont = this.$$$getFont$$$(null, -1, 14, fontSizeLabel.getFont());
        if (fontSizeLabelFont != null) fontSizeLabel.setFont(fontSizeLabelFont);
        fontSizeLabel.setText("字号");
        panel4.add(fontSizeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        fontLabel = new JLabel();
        Font fontLabelFont = this.$$$getFont$$$(null, -1, 14, fontLabel.getFont());
        if (fontLabelFont != null) fontLabel.setFont(fontLabelFont);
        fontLabel.setText("字体");
        panel4.add(fontLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        fontNameComboBox = new JComboBox();
        Font fontNameComboBoxFont = this.$$$getFont$$$(null, -1, 14, fontNameComboBox.getFont());
        if (fontNameComboBoxFont != null) fontNameComboBox.setFont(fontNameComboBoxFont);
        panel4.add(fontNameComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(180, -1), null, null, 0, false));
        languagePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(languagePanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(5, 5, 5, 5), -1, -1));
        languagePanel.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        languageComboBox = new JComboBox();
        Font languageComboBoxFont = this.$$$getFont$$$(null, -1, 14, languageComboBox.getFont());
        if (languageComboBoxFont != null) languageComboBox.setFont(languageComboBoxFont);
        panel5.add(languageComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(380, -1), null, null, 0, false));
        languageLabel = new JLabel();
        Font languageLabelFont = this.$$$getFont$$$(null, -1, 14, languageLabel.getFont());
        if (languageLabelFont != null) languageLabel.setFont(languageLabelFont);
        languageLabel.setText("语言");
        panel5.add(languageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final Spacer spacer2 = new Spacer();
        contentPane.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
