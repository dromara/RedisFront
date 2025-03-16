package org.dromara.redisfront.ui.widget.main.fragment.scaffold.pubsub;

import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubListener;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.FutureUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisPubSubService;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import raven.toast.Notifications;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * PubSubForm
 *
 * @author Jin
 */
public class PubSubPageView extends QSPageItem<RedisFrontWidget> implements RedisPubSubListener<String, String>, RedisClusterPubSubListener<String, String> {

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
    private final RedisConnectContext redisConnectContext;
    private final RedisFrontWidget owner;
    private Integer messageCount = 0;

    public PubSubPageView(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        this.owner = owner;
        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        this.redisConnectContext = redisConnectContext;
        channelField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                return RedisFrontUtils.isNotEmpty(jTextField.getText());
            }
        });
        numLabel.setText(String.format(owner.$tr("PubSubPageView.numLabel.messageCount"), "0"));
        subscribeChannel.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, owner.$tr("PubSubPageView.subscribeChannel.message"));
        channelField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, owner.$tr("PubSubPageView.channelField.message"));
        messageField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, owner.$tr("PubSubPageView.messageField.message"));
        infoLabel.setText(owner.$tr("PubSubPageView.infoLabel.normalTitle"));
        infoLabel.setIcon(Icons.STATUS_ERROR);
        enableSubscribe.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                var ret = RedisFrontUtils.isNotEmpty(jTextField.getText());
                enableSubscribe.setEnabled(ret);
                return ret;
            }
        });
        enableSubscribe.addItemListener(_ -> {
            var channel = subscribeChannel.getText();
            if (!enableSubscribe.isSelected()) {
                subscribeChannel.setFocusable(true);
                pubsub.unsubscribe(channel);
                infoLabel.setText(owner.$tr("PubSubPageView.infoLabel.stopTitle"));
                infoLabel.setIcon(Icons.STATUS_ERROR);
                enableSubscribe.setText(owner.$tr("PubSubPageView.enableSubscribe.enableTitle"));
                enableSubscribe.setToolTipText(owner.$tr("PubSubPageView.enableSubscribe.enableMessage"));
            } else {
                subscribeChannel.setFocusable(false);
                if (RedisFrontUtils.isEmpty(lastSubscribeChanel)) {
                    lastSubscribeChanel = channel;
                } else if (RedisFrontUtils.endsWith(lastSubscribeChanel, channel)) {
                    SwingUtilities.invokeLater(() -> numLabel.setText(String.format(owner.$tr("PubSubPageView.numLabel.messageCount"), "0")));
                }
                pubsub.subscribe(channel);
                infoLabel.setText(owner.$tr("PubSubPageView.infoLabel.startTitle"));
                infoLabel.setIcon(Icons.STATUS_OK);
                enableSubscribe.setText(owner.$tr("PubSubPageView.enableSubscribe.disableTitle"));
                enableSubscribe.setToolTipText(owner.$tr("PubSubPageView.enableSubscribe.disableMessage"));
            }
        });
        publishBtn.addActionListener(_ -> FutureUtils.runAsync(() -> {
            RedisPubSubService.service.publish(redisConnectContext, channelField.getText(), messageField.getText());
            SwingUtilities.invokeLater(() -> {
                messageField.setText("");
                Notifications.getInstance().show(Notifications.Type.SUCCESS, owner.$tr("PubSubPageView.publishBtn.sendMessageSuccess"));
            });
        }));
    }

    @Override
    public void onChange() {
        openConnection();
    }

    public void openConnection() {
        if (pubsub != null) {
            return;
        }
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            FutureUtils.runAsync(() -> {
                var connection = RedisConnectionPoolManager.getClusterConnectPubSub(redisConnectContext);
                pubsub = connection.async();
            }).thenRun(() -> pubsub.getStatefulConnection().addListener(this));
        } else {
            FutureUtils.runAsync(() -> {
                var connection = RedisConnectionPoolManager.getConnectPubSub(redisConnectContext);
                pubsub = connection.async();
            }).thenRun(() -> pubsub.getStatefulConnection().addListener(this));
        }
    }

    public void disConnection() {
        enableSubscribe.setSelected(false);
        if (RedisFrontUtils.isNotNull(pubsub)) {
            pubsub.getStatefulConnection().closeAsync().thenRun(() -> redisClient.shutdownAsync().thenRun(() -> {
                pubsub = null;
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
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, BorderLayout.NORTH);
        panel1.add(numLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(subscribeChannel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("");
        panel1.add(infoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout(0, 0));
        rootPanel.add(listPanel, BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        listPanel.add(scrollPane1, BorderLayout.CENTER);
        messageList = new JTextArea();
        scrollPane1.setViewportView(messageList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, BorderLayout.SOUTH);
        channelField = new JTextField();
        panel2.add(channelField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "PubSubPageView.channelLabel.text"));
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "PubSubPageView.messageLabel.text"));
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        messageField = new JTextField();
        panel2.add(messageField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        this.$$$loadButtonText$$$(publishBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "PubSubPageView.publishBtn.text"));
        panel2.add(publishBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
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
        enableSubscribe.setText(owner.$tr("PubSubPageView.enableSubscribe.enableTitle") + " ");
        enableSubscribe.setIcon(Icons.SUBSCRIBE_ICON);
        enableSubscribe.setSelectedIcon(Icons.UNSUBSCRIBE_ICON);

        publishBtn = new JButton();
        publishBtn.setIcon(Icons.PUBLISH_ICON);
        publishBtn.setFocusable(false);

        numLabel = new JLabel();
        numLabel.setOpaque(true);
        numLabel.setForeground(Color.WHITE);
        numLabel.setBackground(KeyTypeEnum.ZSET.color());
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
                owner.$tr("PubSubPageView.message.time") + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + "\n" + owner.$tr("PubSubPageView.message.channel") + channel + " " + "\n" +
                owner.$tr("PubSubPageView.message.body") + message + "\n" + "\n";
        messageCount++;
        SwingUtilities.invokeLater(() -> {
            numLabel.setText(String.format(owner.$tr("PubSubPageView.numLabel.messageCount"), messageCount));
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

    @Override
    public RedisFrontWidget getApp() {
        return null;
    }

    @Override
    protected JComponent getContentPanel() {
        return rootPanel;
    }

}
