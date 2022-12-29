package com.redisfront.ui.form.fragment;

import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.JschUtils;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisPubSubService;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubListener;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * PubSubForm
 *
 * @author Jin
 */
public class PubSubForm extends JPanel implements RedisPubSubListener<String, String>, RedisClusterPubSubListener<String, String> {

    private RedisPubSubAsyncCommands<String, String> pubsub;
    private AbstractRedisClient redisClient;
    private JPanel rootPanel;
    private JToggleButton enableSubscribe;
    private JTextField channelField;
    private JTextField messageField;
    private JButton publishBtn;
    private JPanel listPanel;
    private JTextField subscribeChannel;
    private JTextArea messageList;
    private JLabel numLabel;
    private JLabel infoLabel;
    private String lastSubscribeChanel;
    private final ConnectInfo connectInfo;

    public static PubSubForm newInstance(ConnectInfo connectInfo) {
        return new PubSubForm(connectInfo);
    }

    public PubSubForm(ConnectInfo connectInfo) {
        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        this.connectInfo = connectInfo;
        channelField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                return Fn.isNotEmpty(jTextField.getText());
            }
        });
        subscribeChannel.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入需要监听的通道名称！");
        channelField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入通道名称！");
        messageField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入消息内容！");

        enableSubscribe.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                var ret = Fn.isNotEmpty(jTextField.getText());
                enableSubscribe.setEnabled(ret);
                return ret;
            }
        });
        enableSubscribe.addItemListener(e -> {
            var channel = subscribeChannel.getText();
            if (!enableSubscribe.isSelected()) {
                subscribeChannel.setFocusable(true);
                pubsub.unsubscribe(channel);
                infoLabel.setText("  监听已关闭 ");
                enableSubscribe.setText("开启监听 ");
                enableSubscribe.setToolTipText("点击开始监听！");
            } else {
                subscribeChannel.setFocusable(false);
                if (Fn.isEmpty(lastSubscribeChanel)) {
                    lastSubscribeChanel = channel;
                } else if (Fn.endsWith(lastSubscribeChanel, channel)) {
                    SwingUtilities.invokeLater(() -> numLabel.setText("消息数量: 0 "));
                }
                pubsub.subscribe(channel);
                infoLabel.setText("  消息监听中....  ");
                enableSubscribe.setText("停止监听 ");
                enableSubscribe.setToolTipText("点击停止监听！");
            }
        });
        publishBtn.addActionListener(e -> FutureUtils.runAsync(() -> {
            var count = RedisPubSubService.service.publish(connectInfo, channelField.getText(), messageField.getText());
            SwingUtilities.invokeLater(() -> {
                messageField.setText("");
                AlertUtils.showInformationDialog("成功发布 " + count + " 条消息！");
            });
        }));
    }

    public void openConnection() {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            FutureUtils.runAsync(() -> {
                var redisUrl = LettuceUtils.getRedisURI(connectInfo);
                redisClient = LettuceUtils.getRedisClusterClient(redisUrl, connectInfo);
                var connection = ((RedisClusterClient) redisClient).connectPubSub();
                JschUtils.openSession(connectInfo, (RedisClusterClient) redisClient);
                pubsub = connection.async();
            }).thenRun(() -> pubsub.getStatefulConnection().addListener(this));
        } else {
            FutureUtils.runAsync(() -> {
                redisClient = LettuceUtils.getRedisClient(connectInfo);
                JschUtils.openSession(connectInfo);
                var connection = (((RedisClient) redisClient).connectPubSub());
                pubsub = connection.async();
            }).thenRun(() -> pubsub.getStatefulConnection().addListener(this));
        }
    }

    public void disConnection() {
        enableSubscribe.setSelected(false);
        if (Fn.isNotNull(pubsub)) {
            pubsub.getStatefulConnection().closeAsync().thenRun(() -> redisClient.shutdownAsync().thenRun(() -> {
                if (connectInfo.getSshConfig() != null) {
                    JschUtils.closeSession();
                }
            }));
        }
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
        rootPanel.setLayout(new BorderLayout(0, 0));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, BorderLayout.NORTH);
        numLabel.setText("消息数量: 0 ");
        panel1.add(numLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(subscribeChannel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("");
        panel1.add(infoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout(0, 0));
        rootPanel.add(listPanel, BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        listPanel.add(scrollPane1, BorderLayout.CENTER);
        messageList = new JTextArea();
        scrollPane1.setViewportView(messageList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, BorderLayout.SOUTH);
        channelField = new JTextField();
        panel2.add(channelField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("通道");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("消息");
        panel2.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        messageField = new JTextField();
        panel2.add(messageField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        publishBtn.setText("发布消息");
        panel2.add(publishBtn, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

    private void createUIComponents() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        enableSubscribe = new JToggleButton();
        enableSubscribe.setFocusable(false);
        enableSubscribe.setText("开启监听 ");
        enableSubscribe.setIcon(UI.SUBSCRIBE_ICON);
        enableSubscribe.setSelectedIcon(UI.UNSUBSCRIBE_ICON);

        publishBtn = new JButton();
        publishBtn.setIcon(UI.PUBLISH_ICON);
        publishBtn.setFocusable(false);

        numLabel = new JLabel();
        numLabel.setOpaque(true);
        numLabel.setForeground(Color.WHITE);
        numLabel.setBackground(Enum.KeyTypeEnum.ZSET.color());
        numLabel.setBorder(new EmptyBorder(2, 3, 2, 3));

        subscribeChannel = new JTextField();
        subscribeChannel.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, enableSubscribe);
        subscribeChannel.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        subscribeChannel.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) textField -> {

        });
    }


    @Override
    public void message(String channel, String message) {
        String sb = "----------------------------------" + "\n" +
                "时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + "\n" + "通道: " + channel + " " + "\n" +
                "消息: " + message + "\n" + "\n";
        SwingUtilities.invokeLater(() -> {
            var tmp = numLabel.getText().split(":");
            numLabel.setText(tmp[0] + ":" + (Integer.parseInt(tmp[1].replace(" ", "")) + 1));
            messageList.append(sb);
        });
    }

    @Override
    public void message(String pattern, String channel, String message) {
        System.out.println("channel:" + channel + "  " + message);
    }

    @Override
    public void subscribed(String channel, long count) {
        System.out.println("channel:" + channel + "  " + count);
    }

    @Override
    public void psubscribed(String pattern, long count) {
        System.out.println("channel:" + pattern + "  " + count);
    }

    @Override
    public void unsubscribed(String channel, long count) {
        System.out.println("channel:" + channel + "  " + count);
    }

    @Override
    public void punsubscribed(String pattern, long count) {
        System.out.println("channel:" + pattern + "  " + count);
    }

    @Override
    public void message(RedisClusterNode node, String channel, String message) {
        System.out.println("channel:" + channel + "  " + message);
    }

    @Override
    public void message(RedisClusterNode node, String pattern, String channel, String message) {
        System.out.println("channel:" + channel + "  " + message);
    }

    @Override
    public void subscribed(RedisClusterNode node, String channel, long count) {

    }

    @Override
    public void psubscribed(RedisClusterNode node, String pattern, long count) {

    }

    @Override
    public void unsubscribed(RedisClusterNode node, String channel, long count) {

    }

    @Override
    public void punsubscribed(RedisClusterNode node, String pattern, long count) {

    }
}
