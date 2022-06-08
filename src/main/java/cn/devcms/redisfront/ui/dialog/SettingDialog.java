package cn.devcms.redisfront.ui.dialog;


import cn.devcms.redisfront.common.base.BaseDialog;
import cn.devcms.redisfront.common.constant.Constant;
import cn.devcms.redisfront.common.util.PrefUtil;
import cn.devcms.redisfront.common.util.ThemeUtil;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.StringUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingDialog extends BaseDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel themePanel;
    private JPanel fontPanel;
    private JPanel redisPanel;
    private JComboBox<String> fontSizeComboBox;
    private JComboBox<String> fontNameComboBox;
    private JComboBox<ThemeUtil.ThemeInfo> themeNameComboBox;
    private JTextField textField1;
    private JTextField textField2;


    public SettingDialog(Frame owner) {
        super(owner);
        setTitle("设置");
        setContentPane(contentPane);
        initThemeNameComboBox();
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
    }


    private void onOK() {
        // 在此处添加您的代码
        dispose();
    }

    private void onCancel() {
        // 必要时在此处添加您的代码
        dispose();
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
            EventQueue.invokeLater(() -> {
                ThemeUtil.ThemeInfo themeInfo = (ThemeUtil.ThemeInfo) selected.getSelectedItem();
                assert themeInfo != null;
                ThemeUtil.changeTheme(this, themeInfo);
                PrefUtil.getState().put(Constant.KEY_THEME, StringUtils.isEmpty(themeInfo.lafClassName()) ? "R_" + themeInfo.resourceName() : themeInfo.lafClassName());
                PrefUtil.getState().put(Constant.KEY_THEME_SELECT_INDEX, String.valueOf(themeNameComboBox.getSelectedIndex()));
            });
        });

        themeNameComboBox.setSelectedIndex(Integer.parseInt(PrefUtil.getState().get(Constant.KEY_THEME_SELECT_INDEX, "0")));
    }


    private void createUIComponents() {
        themePanel = new JPanel();
        themePanel.setBorder(new TitledBorder("主题设置"));
        fontPanel = new JPanel();
        fontPanel.setBorder(new TitledBorder("字体设置"));
        redisPanel = new JPanel();
        redisPanel.setBorder(new TitledBorder("加载设置"));
        themeNameComboBox = new JComboBox<>() {
            @Override
            public void setSelectedItem(Object item) {
                ThemeUtil.ThemeInfo themeInfo = (ThemeUtil.ThemeInfo) item;
                if (themeInfo == null) {
                    return;
                }
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

}
