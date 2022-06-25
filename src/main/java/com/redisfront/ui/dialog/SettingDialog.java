package com.redisfront.ui.dialog;


import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.StringUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.RedisFrontApplication;
import com.redisfront.constant.Constant;
import com.redisfront.ui.component.AbstractDialog;
import com.redisfront.util.*;
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
    private JComboBox<ThemeUtil.ThemeInfo> themeNameComboBox;
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

    private static final LocaleUtil.BundleInfo SETTING_DIALOG = LocaleUtil.get("SettingDialog.Window");
    private static final LocaleUtil.BundleInfo THEME_PANEL = LocaleUtil.get("SettingDialog.ThemePanel");

    private static final LocaleUtil.BundleInfo LANGUAGE_PANEL = LocaleUtil.get("SettingDialog.LanguagePanel");
    private static final LocaleUtil.BundleInfo FONT_PANEL = LocaleUtil.get("SettingDialog.FontPanel");
    private static final LocaleUtil.BundleInfo REDIS_PANEL = LocaleUtil.get("SettingDialog.RedisPanel");
    private static final LocaleUtil.BundleInfo FONT_LABEL = LocaleUtil.get("SettingDialog.FontLabel");

    private static final LocaleUtil.BundleInfo LANGUAGE_LABEL = LocaleUtil.get("SettingDialog.LanguageLabel");
    private static final LocaleUtil.BundleInfo FONT_SIZE_LABEL = LocaleUtil.get("SettingDialog.FontSizeLabel");
    private static final LocaleUtil.BundleInfo THEME_LABEL = LocaleUtil.get("SettingDialog.ThemeLabel");
    private static final LocaleUtil.BundleInfo LOAD_NUM_LABEL = LocaleUtil.get("SettingDialog.LoadNumLabel");
    private static final LocaleUtil.BundleInfo KEY_SEPARATOR_LABEL = LocaleUtil.get("SettingDialog.KeySeparatorLabel");

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
                    String languageTag = PrefUtil.getState().get(Constant.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
                    return Fn.equal(e.getValue(), languageTag);
                }).findAny().orElseThrow());
    }

    private void initLabelText() {
        fontLabel.setText(FONT_LABEL.title());
        fontSizeLabel.setText(FONT_SIZE_LABEL.title());
        themeLabel.setText(THEME_LABEL.title());
        languageLabel.setText(LANGUAGE_LABEL.title());

        keySeparatorLabel.setText(KEY_SEPARATOR_LABEL.title());
        keySeparator.setText(PrefUtil.getState().get(Constant.KEY_KEY_SEPARATOR, ":"));

        loadNumLabel.setText(LOAD_NUM_LABEL.title());
        keyMaxLoadNum.setText(PrefUtil.getState().get(Constant.KEY_KEY_MAX_LOAD_NUM, "5000"));


    }

    private void initFontSizeComboBox() {
        ArrayList<String> fontSizes = new ArrayList<>(Arrays.asList(
                "10", "11", "12", "13", "14", "15", "16", "17", "18"));
        for (String fontSize : fontSizes) {
            fontSizeComboBox.addItem(fontSize);
        }
        fontSizeComboBox.addActionListener(e -> {
            String fontSizeStr = (String) fontSizeComboBox.getSelectedItem();
            if (Fn.equal(fontSizeStr, PrefUtil.getState().get(Constant.KEY_FONT_SIZE, getDefaultFontSize()))) {
                return;
            }
            this.updateFontSizeHandler(fontSizeStr);
        });
        fontSizeComboBox.setSelectedItem(PrefUtil.getState().get(Constant.KEY_FONT_SIZE, getDefaultFontSize()));
    }

    //获取默认字体大小
    private String getDefaultFontSize() {
        return String.valueOf(UIManager.getFont("defaultFont").getSize());
    }

    private void initFontComboBox() {
        //获取系统全部字体
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<String> availableFontFamilyNames = List.of(graphicsEnvironment.getAvailableFontFamilyNames());
        Set<String> families = new HashSet<>(Arrays.asList(
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
            if (Fn.equal(fontFamily, PrefUtil.getState().get(Constant.KEY_FONT_NAME, getDefaultFontFamily()))) {
                return;
            }
            this.updateFontNameHandler(fontFamily);
        });
        fontNameComboBox.setSelectedItem(PrefUtil.getState().get(Constant.KEY_FONT_NAME, getDefaultFontFamily()));
    }

    //获取默认字体大小
    private String getDefaultFontFamily() {
        return String.valueOf(UIManager.getFont("defaultFont").getFontName());
    }


    private void initThemeNameComboBox() {
        themeNameComboBox.addItem(new ThemeUtil.ThemeInfo(FlatLightLaf.NAME, null, false, null, null, null, null, null, FlatLightLaf.class.getName()));
        themeNameComboBox.addItem(new ThemeUtil.ThemeInfo(FlatDarkLaf.NAME, null, true, null, null, null, null, null, FlatDarkLaf.class.getName()));
        themeNameComboBox.addItem(new ThemeUtil.ThemeInfo(FlatIntelliJLaf.NAME, null, true, null, null, null, null, null, FlatIntelliJLaf.class.getName()));
        themeNameComboBox.addItem(new ThemeUtil.ThemeInfo(FlatDarculaLaf.NAME, null, true, null, null, null, null, null, FlatDarculaLaf.class.getName()));

        boolean addMaterialTag = true;
        for (int i = 0; i < ThemeUtil.bundledThemes.size(); i++) {
            if (i == 0) {
                themeNameComboBox.addItem(new ThemeUtil.ThemeInfo("****** IntelliJ Theme ******", null, false, null, null, null, null, null, null));
            }
            ThemeUtil.ThemeInfo themeInfo = ThemeUtil.bundledThemes.get(i);

            if (themeInfo.name().startsWith("Material") && addMaterialTag) {
                themeNameComboBox.addItem(new ThemeUtil.ThemeInfo("****** Material Theme ******", null, false, null, null, null, null, null, null));
                addMaterialTag = false;
            }
            themeNameComboBox.addItem(themeInfo);
        }

        themeNameComboBox.addActionListener(e -> {
            JComboBox<?> selected = (JComboBox<?>) e.getSource();
            ThemeUtil.ThemeInfo themeInfo = (ThemeUtil.ThemeInfo) selected.getSelectedItem();
            String themeName = StringUtils.isEmpty(Objects.requireNonNull(themeInfo).lafClassName()) ? "R_" + themeInfo.resourceName() : themeInfo.lafClassName();
            if (Fn.equal(themeName, PrefUtil.getState().get(Constant.KEY_THEME, FlatDarculaLaf.class.getName()))) {
                return;
            }
            ThemeUtil.changeTheme(themeInfo);
        });

        themeNameComboBox.setSelectedIndex(Integer.parseInt(PrefUtil.getState().get(Constant.KEY_THEME_SELECT_INDEX, "0")));
    }

    private void updateFontSizeHandler(String fontSize) {
        Font font = UIManager.getFont("defaultFont");
        Font newFont = font.deriveFont(Float.parseFloat(fontSize));
        UIManager.put("defaultFont", newFont);
        FlatLaf.updateUI();
    }

    private void updateFontNameHandler(String fontFamily) {
        FlatAnimatedLafChange.showSnapshot();
        Font font = UIManager.getFont("defaultFont");
        Font newFont = StyleContext.getDefaultStyleContext().getFont(fontFamily, font.getStyle(), font.getSize());
        newFont = FlatUIUtils.nonUIResource(newFont);
        UIManager.put("defaultFont", newFont);
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private void onOK() {

        PrefUtil.getState().put(Constant.KEY_KEY_SEPARATOR, keySeparator.getText());
        PrefUtil.getState().put(Constant.KEY_KEY_MAX_LOAD_NUM, keyMaxLoadNum.getText());
        //风格
        ThemeUtil.ThemeInfo themeInfo = (ThemeUtil.ThemeInfo) themeNameComboBox.getSelectedItem();
        String themeName = StringUtils.isEmpty(Objects.requireNonNull(themeInfo).lafClassName()) ? "R_" + themeInfo.resourceName() : themeInfo.lafClassName();
        PrefUtil.getState().put(Constant.KEY_THEME, themeName);
        PrefUtil.getState().put(Constant.KEY_THEME_SELECT_INDEX, String.valueOf(themeNameComboBox.getSelectedIndex()));
        //字体名称
        String fontFamily = (String) fontNameComboBox.getSelectedItem();
        PrefUtil.getState().put(Constant.KEY_FONT_NAME, fontFamily);
        //字体大小
        String fontSizeStr = (String) fontSizeComboBox.getSelectedItem();
        PrefUtil.getState().put(Constant.KEY_FONT_SIZE, fontSizeStr);
        //语言
        Map.Entry<?, ?> newLanguage = (Map.Entry<?, ?>) languageComboBox.getSelectedItem();
        String oldLanguage = PrefUtil.getState().get(Constant.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
        assert newLanguage != null;
        if (Fn.notEqual(newLanguage.getValue(), oldLanguage)) {
            Locale.setDefault(Locale.forLanguageTag((String) newLanguage.getValue()));
            PrefUtil.getState().put(Constant.KEY_LANGUAGE, (String) newLanguage.getValue());
            var res = MsgUtil.showConfirmDialog("语言已变更，重启后生效！\n 是否立即重启？", JOptionPane.YES_NO_OPTION);
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
                ThemeUtil.ThemeInfo themeInfo = (ThemeUtil.ThemeInfo) item;
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
                        ThemeUtil.ThemeInfo themeInfo = (ThemeUtil.ThemeInfo) value;
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
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
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
