package com.redisfront.ui.dialog;


import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.StringUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.theme.RedisFrontDarkLaf;
import com.redisfront.commons.theme.RedisFrontLightLaf;
import com.redisfront.commons.ui.AbstractDialog;
import com.redisfront.commons.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;

public class SettingDialog extends AbstractDialog<Void> {

    private static final Logger log = LoggerFactory.getLogger(SettingDialog.class);
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel themePanel;
    private JPanel fontPanel;
    private JPanel redisPanel;
    private JComboBox<String> fontSizeComboBox;
    private JComboBox<String> fontNameComboBox;
    private JComboBox<ThemeUtils.ThemeInfo> themeNameComboBox;
    private JTextField keySeparator;
    private JTextField keyMaxLoadNum;
    private JLabel fontLabel;
    private JLabel fontSizeLabel;
    private JLabel themeLabel;
    private JLabel loadNumLabel;
    private JLabel keySeparatorLabel;
    private JPanel languagePanel;
    private JLabel languageLabel;
    private JComboBox<Map.Entry<String, String>> languageComboBox;

    private static final LocaleUtils.BundleInfo SETTING_DIALOG = LocaleUtils.get("SettingDialog.Window");
    private static final LocaleUtils.BundleInfo THEME_PANEL = LocaleUtils.get("SettingDialog.ThemePanel");

    private static final LocaleUtils.BundleInfo LANGUAGE_PANEL = LocaleUtils.get("SettingDialog.LanguagePanel");
    private static final LocaleUtils.BundleInfo FONT_PANEL = LocaleUtils.get("SettingDialog.FontPanel");
    private static final LocaleUtils.BundleInfo REDIS_PANEL = LocaleUtils.get("SettingDialog.RedisPanel");
    private static final LocaleUtils.BundleInfo FONT_LABEL = LocaleUtils.get("SettingDialog.FontLabel");

    private static final LocaleUtils.BundleInfo LANGUAGE_LABEL = LocaleUtils.get("SettingDialog.LanguageLabel");
    private static final LocaleUtils.BundleInfo FONT_SIZE_LABEL = LocaleUtils.get("SettingDialog.FontSizeLabel");
    private static final LocaleUtils.BundleInfo THEME_LABEL = LocaleUtils.get("SettingDialog.ThemeLabel");
    private static final LocaleUtils.BundleInfo LOAD_NUM_LABEL = LocaleUtils.get("SettingDialog.LoadNumLabel");
    private static final LocaleUtils.BundleInfo KEY_SEPARATOR_LABEL = LocaleUtils.get("SettingDialog.KeySeparatorLabel");

    public static void showSettingDialog() {
        var settingDialog = new SettingDialog(RedisFrontApplication.frame);
        settingDialog.setMinimumSize(new Dimension(550, 400));
        settingDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        settingDialog.pack();
        settingDialog.setVisible(true);
    }

    public SettingDialog(Frame owner) {
        super(owner);
        $$$setupUI$$$();
        setTitle(SETTING_DIALOG.title());
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        initThemeNameComboBox();
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
                    String languageTag = PrefUtils.getState().get(Const.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
                    return Fn.equal(e.getValue(), languageTag);
                }).findAny().orElseThrow());
    }

    private void initLabelText() {
        fontLabel.setText(FONT_LABEL.title());
        fontSizeLabel.setText(FONT_SIZE_LABEL.title());
        themeLabel.setText(THEME_LABEL.title());
        languageLabel.setText(LANGUAGE_LABEL.title());

        keySeparatorLabel.setText(KEY_SEPARATOR_LABEL.title());
        keySeparator.setText(PrefUtils.getState().get(Const.KEY_KEY_SEPARATOR, ":"));

        loadNumLabel.setText(LOAD_NUM_LABEL.title());
        keyMaxLoadNum.setText(PrefUtils.getState().get(Const.KEY_KEY_MAX_LOAD_NUM, "5000"));


    }

    private void initFontSizeComboBox() {
        var fontSizes = new ArrayList<>(Arrays.asList(
                "10", "11", "12", "13", "14", "15", "16", "17", "18"));
        for (String fontSize : fontSizes) {
            fontSizeComboBox.addItem(fontSize);
        }
        fontSizeComboBox.addActionListener(e -> {
            String fontSizeStr = (String) fontSizeComboBox.getSelectedItem();
            if (Fn.equal(fontSizeStr, PrefUtils.getState().get(Const.KEY_FONT_SIZE, getDefaultFontSize()))) {
                return;
            }
            this.updateFontSizeHandler(fontSizeStr);
        });
        fontSizeComboBox.setSelectedItem(PrefUtils.getState().get(Const.KEY_FONT_SIZE, getDefaultFontSize()));
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
            if (Fn.equal(fontFamily, PrefUtils.getState().get(Const.KEY_FONT_NAME, getDefaultFontFamily()))) {
                return;
            }
            this.updateFontNameHandler(fontFamily);
        });
        fontNameComboBox.setSelectedItem(PrefUtils.getState().get(Const.KEY_FONT_NAME, getDefaultFontFamily()));
    }

    //获取默认字体大小
    private String getDefaultFontFamily() {
        return String.valueOf(UIManager.getFont("defaultFont").getFontName());
    }


    private void initThemeNameComboBox() {
        themeNameComboBox.addItem(new ThemeUtils.ThemeInfo(RedisFrontLightLaf.NAME, null, true, null, null, null, null, null, RedisFrontLightLaf.class.getName()));
        themeNameComboBox.addItem(new ThemeUtils.ThemeInfo(RedisFrontDarkLaf.NAME, null, true, null, null, null, null, null, RedisFrontDarkLaf.class.getName()));

        themeNameComboBox.addActionListener(e -> {
            JComboBox<?> selected = (JComboBox<?>) e.getSource();
            ThemeUtils.ThemeInfo themeInfo = (ThemeUtils.ThemeInfo) selected.getSelectedItem();
            ThemeUtils.changeTheme(themeInfo);
        });

        themeNameComboBox.setSelectedIndex(Integer.parseInt(PrefUtils.getState().get(Const.KEY_THEME_SELECT_INDEX, "0")));
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

        PrefUtils.getState().put(Const.KEY_KEY_SEPARATOR, keySeparator.getText());
        PrefUtils.getState().put(Const.KEY_KEY_MAX_LOAD_NUM, keyMaxLoadNum.getText());
        //风格
        ThemeUtils.ThemeInfo themeInfo = (ThemeUtils.ThemeInfo) themeNameComboBox.getSelectedItem();
        String themeName = StringUtils.isEmpty(Objects.requireNonNull(themeInfo).lafClassName()) ? "R_" + themeInfo.resourceName() : themeInfo.lafClassName();
        PrefUtils.getState().put(Const.KEY_THEME, themeName);
        PrefUtils.getState().put(Const.KEY_THEME_SELECT_INDEX, String.valueOf(themeNameComboBox.getSelectedIndex()));
        //字体名称
        String fontFamily = (String) fontNameComboBox.getSelectedItem();
        PrefUtils.getState().put(Const.KEY_FONT_NAME, fontFamily);
        //字体大小
        String fontSizeStr = (String) fontSizeComboBox.getSelectedItem();
        PrefUtils.getState().put(Const.KEY_FONT_SIZE, fontSizeStr);
        //语言
        var newLanguage = (Map.Entry<?, ?>) languageComboBox.getSelectedItem();
        var oldLanguage = PrefUtils.getState().get(Const.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
        assert newLanguage != null;
        if (Fn.notEqual(newLanguage.getValue(), oldLanguage)) {
            Locale.setDefault(Locale.forLanguageTag((String) newLanguage.getValue()));
            PrefUtils.getState().put(Const.KEY_LANGUAGE, (String) newLanguage.getValue());
            var res = AlertUtils.showConfirmDialog("语言已变更，重启后生效！\n 是否立即重启？", JOptionPane.YES_NO_OPTION);
            if (res == 0) {
                RedisFrontApplication.frame.dispose();
                System.exit(0);
            }
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        themePanel = new JPanel();
        themePanel.setBorder(new TitledBorder(THEME_PANEL.title()));
        languagePanel = new JPanel();
        languagePanel.setBorder(new TitledBorder(LANGUAGE_PANEL.title()));
        fontPanel = new JPanel();
        fontPanel.setBorder(new TitledBorder(FONT_PANEL.title()));
        redisPanel = new JPanel();
        redisPanel.setBorder(new TitledBorder(REDIS_PANEL.title()));
        themeNameComboBox = new JComboBox<>() {
            @Override
            public void setSelectedItem(Object item) {
                var themeInfo = (ThemeUtils.ThemeInfo) item;
                if (themeInfo == null) {
                    return;
                }
                //忽略分类符号
                if (themeInfo.name().startsWith("**")) {
                    return;
                }
                super.setSelectedItem(item);
            }

            {
                setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        var themeInfo = (ThemeUtils.ThemeInfo) value;
                        if (themeInfo.name().startsWith("Material Theme UI Lite /")) {
                            setText(themeInfo.name().replace("Material Theme UI Lite /", ""));
                        }
                        if (themeInfo.name().startsWith("**")) {
                            setEnabled(false);
                        }
                        return this;
                    }
                });

            }
        };

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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
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
        panel3.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        themePanel.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(themePanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        themeLabel = new JLabel();
        themeLabel.setText("风格");
        themePanel.add(themeLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        themePanel.add(themeNameComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(380, -1), null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        themePanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        themePanel.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        redisPanel.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(redisPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        keySeparatorLabel = new JLabel();
        keySeparatorLabel.setText("分隔符");
        redisPanel.add(keySeparatorLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keySeparator = new JTextField();
        redisPanel.add(keySeparator, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        loadNumLabel = new JLabel();
        loadNumLabel.setText("key加载数");
        redisPanel.add(loadNumLabel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keyMaxLoadNum = new JTextField();
        redisPanel.add(keyMaxLoadNum, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer4 = new Spacer();
        redisPanel.add(spacer4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        redisPanel.add(spacer5, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        fontPanel.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(fontPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        fontLabel = new JLabel();
        fontLabel.setText("字体");
        fontPanel.add(fontLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fontNameComboBox = new JComboBox();
        fontPanel.add(fontNameComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(180, -1), null, null, 0, false));
        fontSizeLabel = new JLabel();
        fontSizeLabel.setText("字号");
        fontPanel.add(fontSizeLabel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fontSizeComboBox = new JComboBox();
        fontPanel.add(fontSizeComboBox, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(180, -1), null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        fontPanel.add(spacer6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        fontPanel.add(spacer7, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        languagePanel.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(languagePanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        languageLabel = new JLabel();
        languageLabel.setText("语言");
        languagePanel.add(languageLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        languageComboBox = new JComboBox();
        languagePanel.add(languageComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(380, -1), null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        languagePanel.add(spacer8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        languagePanel.add(spacer9, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
