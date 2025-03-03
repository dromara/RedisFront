package org.dromara.redisfront.ui.widget.main.about;

import cn.hutool.core.swing.DesktopUtil;
import com.formdev.flatlaf.FlatClientProperties;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.commons.resources.Icons;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainAboutPanel extends JPanel {

    private static final String PROJECT_URL = "https://redisfront.dromara.org";
    private static final String DROMARA_URL = "https://www.dromara.org";
    private static final String TITLE = "RedisFront";
    private static final String SUBTITLE = "Cross-platform Redis GUI Client";

    public MainAboutPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 0)); // 增加水平间距
        setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // 左侧图标
        add(createIconPanel(), BorderLayout.WEST);

        // 右侧信息面板
        add(createInfoPanel(), BorderLayout.CENTER);
    }

    private JComponent createIconPanel() {
        JLabel iconLabel = new JLabel(Icons.REDIS_ICON);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 20)); // 右侧留白
        return iconLabel;
    }

    private JComponent createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // 标题区域
        panel.add(createTitlePanel(), BorderLayout.NORTH);

        // 信息区域
        panel.add(createContentPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JComponent createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel(TITLE);
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel subtitleLabel = new JLabel(SUBTITLE);
        panel.add(subtitleLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JComponent createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // 版本信息
        JLabel versionLabel = createInfoLabel("Version " + Constants.APP_VERSION);
        panel.add(versionLabel);

        // 项目链接
        panel.add(createLinkLabel(PROJECT_URL, PROJECT_URL));

        // Dromara 链接
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLinkLabel("Dromara Foundation", DROMARA_URL));

        return panel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(new EmptyBorder(2, 0, 2, 0));
        return label;
    }

    private JLabel createLinkLabel(String text, String url) {
        JLabel linkLabel = new JLabel("<html><a href='#'>" + text + "</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                    DesktopUtil.browse(url);

            }
        });
        return linkLabel;
    }
}
