package cn.devcms.redisfront.ui.dialog;

import cn.devcms.redisfront.ui.theme.ThemeInfo;
import cn.devcms.redisfront.ui.theme.ThemeUtil;
import cn.devcms.redisfront.ui.theme.ThemesManager;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel themePanel;
    private JPanel fontPanel;
    private JPanel redisPanel;
    private JComboBox<String> fontSizeComboBox;
    private JComboBox<String> fontNameComboBox;
    private JComboBox<ThemeInfo> themeNameComboBox;
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
        themeNameComboBox.addItem(new ThemeInfo("FlatLaf Light", null, false, null, null, null, null, null, FlatLightLaf.class.getName()));
        themeNameComboBox.addItem(new ThemeInfo("FlatLaf Dark", null, true, null, null, null, null, null, FlatDarkLaf.class.getName()));
        boolean addMaterialTag = true;
        for (int i = 0; i < ThemesManager.bundledThemes.size(); i++) {
            if (i == 0) {
                themeNameComboBox.addItem(new ThemeInfo("****** IntelliJ Theme ******", null, false, null, null, null, null, null, null));
            }

            ThemeInfo themeInfo = ThemesManager.bundledThemes.get(i);

            if (themeInfo.name().startsWith("Material") && addMaterialTag) {
                themeNameComboBox.addItem(new ThemeInfo("****** Material Theme ******", null, false, null, null, null, null, null, null));
                addMaterialTag = false;
            }
            themeNameComboBox.addItem(themeInfo);
        }
        themeNameComboBox.addActionListener(e -> {
            JComboBox<?> selected = (JComboBox<?>) e.getSource();
            EventQueue.invokeLater(() -> ThemeUtil.setTheme(this, (ThemeInfo) selected.getSelectedItem()));
        });

    }


    private void createUIComponents() {
        themePanel = new JPanel();
        themePanel.setBorder(new TitledBorder("主题设置"));
        fontPanel = new JPanel();
        fontPanel.setBorder(new TitledBorder("字体设置"));
        redisPanel = new JPanel();
        redisPanel.setBorder(new TitledBorder("其他设置"));
        themeNameComboBox = new JComboBox<>() {
            @Override
            public void setSelectedItem(Object item) {
                ThemeInfo themeInfo = (ThemeInfo) item;
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
                        ThemeInfo themeInfo = (ThemeInfo) value;
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
