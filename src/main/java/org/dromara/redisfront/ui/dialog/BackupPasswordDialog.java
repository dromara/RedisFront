package org.dromara.redisfront.ui.dialog;

import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public final class BackupPasswordDialog {
    private BackupPasswordDialog() { }
    public static char[] ask(RedisFrontWidget owner, boolean confirmation) {
        var first = new JPasswordField(24);var second = new JPasswordField(24);
        var panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel(owner.$tr("Safety.backupPassword")));panel.add(first);
        if (confirmation) { panel.add(new JLabel(owner.$tr("Safety.repeatPassword")));panel.add(second); }
        if (JOptionPane.showConfirmDialog(owner, panel, owner.$tr("Safety.encryptedBackup"), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return null;
        char[] password = first.getPassword(), repeated = second.getPassword();
        try {
            if (confirmation && (password.length < 8 || !Arrays.equals(password, repeated))) {
                Arrays.fill(password, '\0');
                JOptionPane.showMessageDialog(owner, owner.$tr("Safety.passwordMismatch"));return null;
            }
            return password;
        } finally { Arrays.fill(repeated, '\0');first.setText("");second.setText(""); }
    }
}
