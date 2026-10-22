package org.dromara.redisfront.ui.components.connection;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.lettuce.ConnectionRecovery;
import org.dromara.redisfront.model.context.RedisConnectContext;

import javax.swing.*;
import java.awt.Component;
import java.util.function.Function;

/** Connection-specific settings, edited and saved with the connection profile. */
public final class ConnectionSafetySettingsPanel extends JPanel {
    private final JCheckBox readOnly;
    private final JButton sentinelOptions;
    private final Function<String, String> tr;
    private String sentinelEndpoints = "";
    private RedisMode detectedMode;

    public ConnectionSafetySettingsPanel(Function<String, String> tr) {
        super(new MigLayout("insets 0, fillx, wrap 1, hidemode 3", "[grow,fill]", "[]6[]"));
        this.tr = tr;
        readOnly = new JCheckBox(tr.apply("Safety.readOnly"));
        readOnly.setName("connection.readOnly");
        readOnly.setToolTipText(tr.apply("Safety.readOnlyHelp"));
        add(readOnly);

        sentinelOptions = new JButton(tr.apply("Safety.sentinelConfigure"));
        sentinelOptions.setName("connection.sentinelOptions");
        sentinelOptions.setToolTipText(tr.apply("Safety.sentinelEndpointsHelp"));
        sentinelOptions.addActionListener(e -> promptSentinelEndpoints(this));
        add(sentinelOptions, "alignx left, growx 0");
        setDetectedMode(null);
    }

    public void load(RedisConnectContext.SettingInfo setting) {
        readOnly.setSelected(setting != null && setting.isReadOnly());
        sentinelEndpoints = setting == null || setting.getSentinelEndpoints() == null ? "" : setting.getSentinelEndpoints();
        // Existing profiles with saved endpoints can edit them without testing again.
        setDetectedMode(sentinelEndpoints.isBlank() ? null : RedisMode.SENTINEL);
    }

    public void setDetectedMode(RedisMode mode) {
        detectedMode = mode;
        sentinelOptions.setVisible(mode == RedisMode.SENTINEL);
        revalidate();
        repaint();
    }

    private String validatedEndpoints(String value) {
        String endpoints = value.strip();
        try {
            ConnectionRecovery.parseEndpoints(endpoints);
        } catch (IllegalArgumentException error) {
            throw new RedisFrontException(tr.apply("Safety.invalidSentinelEndpoints"), sentinelOptions);
        }
        return endpoints;
    }

    void setSentinelEndpoints(String value) {
        sentinelEndpoints = validatedEndpoints(value);
    }

    public void promptSentinelEndpoints(Component parent) {
        if (detectedMode != RedisMode.SENTINEL) return;
        var input = new JTextField(sentinelEndpoints, 32);
        var prompt = createSentinelPrompt(input);
        Object[] options = {tr.apply("Safety.sentinelDone"), tr.apply("Safety.sentinelLater")};
        while (JOptionPane.showOptionDialog(parent, prompt, tr.apply("Safety.sentinelEndpoints"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]) == 0) {
            try {
                setSentinelEndpoints(input.getText());
                return;
            } catch (RedisFrontException error) {
                JOptionPane.showMessageDialog(parent, error.getMessage(), tr.apply("Safety.sentinelEndpoints"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JPanel createSentinelPrompt(JTextField input) {
        input.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "host:26380, host:26381");
        input.setToolTipText(tr.apply("Safety.sentinelEndpointsHelp"));
        var prompt = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow,fill]", "[]8[]4[]"));
        prompt.add(new JLabel(tr.apply("Safety.sentinelDetected")));
        var label = new JLabel(tr.apply("Safety.sentinelEndpoints"));
        label.setLabelFor(input);
        prompt.add(label);
        prompt.add(input);
        prompt.add(new JLabel(tr.apply("Safety.sentinelEntryHelp")));
        return prompt;
    }

    public void applyTo(RedisConnectContext.SettingInfo setting) {
        // Unknown mode preserves an existing profile; a confirmed non-Sentinel profile needs no endpoints.
        String endpoints = detectedMode == null || detectedMode == RedisMode.SENTINEL ? validatedEndpoints(sentinelEndpoints) : "";
        setting.setReadOnly(readOnly.isSelected());
        setting.setSentinelEndpoints(endpoints);
    }
}
