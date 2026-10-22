package org.dromara.redisfront.ui.components.diagnostics;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisDiagnosticsService;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** On-demand bounded diagnostics; no automatic scans on production servers. */
public final class RedisDiagnosticsPanel extends JPanel implements AutoCloseable {
    private final RedisConnectContext context;
    private final RedisFrontWidget owner;
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return getRowCount() == 0 || getValueAt(0, column) == null ? Object.class : getValueAt(0, column).getClass();
        }
    };
    private final JComboBox<String> mode;
    private final JTextField pattern = new JTextField("*", 20);
    private final JButton run, batch, cancel;
    private final JLabel status = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar(0, 1000);
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private SwingWorker<?, ?> worker;
    private boolean closed;
    public RedisDiagnosticsPanel(RedisConnectContext context, RedisFrontWidget owner) {
        super(new BorderLayout(8, 8));this.context = context;this.owner = owner;
        mode = new JComboBox<>(new String[]{tr("slowlog"), tr("commandStats"), tr("largeKeys")});
        run = new JButton(tr("run"));batch = new JButton(tr("batchDelete"));cancel = new JButton(tr("cancel"));
        var toolbar = new JPanel(new BorderLayout(8, 0));
        var query = new JPanel(new BorderLayout(8, 0));query.add(mode, BorderLayout.WEST);query.add(pattern, BorderLayout.CENTER);
        var actions = new JPanel(new FlowLayout(FlowLayout.TRAILING, 4, 0));actions.add(run);actions.add(batch);actions.add(cancel);
        toolbar.add(query, BorderLayout.CENTER);toolbar.add(actions, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);var table = new JTable(model);table.setAutoCreateRowSorter(true);add(new JScrollPane(table), BorderLayout.CENTER);
        var footer = new JPanel(new BorderLayout(8, 0));footer.add(status);footer.add(progress, BorderLayout.EAST);add(footer, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pattern.setToolTipText(tr("sampleHelp"));cancel.setEnabled(false);
        run.addActionListener(e -> runDiagnostics());batch.addActionListener(e -> previewBatch());cancel.addActionListener(e -> cancelled.set(true));
        mode.addActionListener(e -> { model.setColumnIdentifiers(columns(mode.getSelectedIndex()));status.setText(tr("scopeHelp")); });
        model.setColumnIdentifiers(columns(0));status.setText(tr("scopeHelp"));
    }
    private String tr(String key) { return owner.$tr("Diagnostics." + key); }
    private String[] columns(int selected) {
        return selected == 0 ? new String[]{"ID", tr("timestamp"), "μs", tr("command")}
                : selected == 1 ? new String[]{tr("command"), tr("calls"), tr("totalMicros"), tr("averageMicros")}
                : new String[]{"Key", tr("type"), tr("bytes"), "TTL"};
    }
    private void busy(boolean running) {
        run.setEnabled(!running);batch.setEnabled(!running);mode.setEnabled(!running);pattern.setEnabled(!running);cancel.setEnabled(running);
        if (running) { cancelled.set(false);progress.setValue(0);status.setText(tr("running")); }
    }
    private void progress(int value) { SwingUtilities.invokeLater(() -> { if (!closed) progress.setValue(value); }); }
    private void runDiagnostics() {
        int selected = mode.getSelectedIndex();String match = pattern.getText();busy(true);
        worker = new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                return switch (selected) {
                    case 0 -> RedisDiagnosticsService.slowlog(context).stream().map(r -> new Object[]{r.id(), r.timestamp(), r.micros(), r.command()}).toList();
                    case 1 -> RedisDiagnosticsService.commandStats(context).stream().map(r -> new Object[]{r.command(), r.calls(), r.totalMicros(), r.averageMicros()}).toList();
                    default -> RedisDiagnosticsService.largeKeys(context, match, cancelled::get, RedisDiagnosticsPanel.this::progress).stream()
                            .map(r -> new Object[]{r.key(), r.type(), r.bytes(), r.ttl()}).toList();
                };
            }
            @Override protected void done() {
                if (closed) return;
                try {
                    model.setDataVector(get().toArray(Object[][]::new), columns(selected));
                    status.setText((cancelled.get() ? tr("cancelled") : tr("complete")) + " · " + model.getRowCount());
                } catch (Exception e) { status.setText(ExceptionMessageUtils.bestMessage(e)); }
                finally { busy(false); }
            }
        };worker.execute();
    }
    private void previewBatch() {
        if (org.dromara.redisfront.commons.lettuce.ReadOnlyCommands.isReadOnly(context)) {
            JOptionPane.showMessageDialog(owner, owner.$tr("Safety.readOnlyBlocked"));return;
        }
        String match = pattern.getText();busy(true);
        worker = new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() { return RedisDiagnosticsService.sampleKeys(context, match, 1000, cancelled::get); }
            @Override protected void done() {
                if (closed) return;
                busy(false);
                try {
                    List<String> keys = get();if (cancelled.get() || keys.isEmpty()) { status.setText(tr("cancelled"));return; }
                    String preview = String.format(tr("deleteConfirm"), context.getHost(), context.getPort(), context.getDatabase(), keys.size())
                            + "\n" + String.join("\n", keys.subList(0, Math.min(keys.size(), 10)));
                    if (JOptionPane.showConfirmDialog(owner, preview, tr("batchDelete"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) executeBatch(keys);
                } catch (Exception e) { status.setText(ExceptionMessageUtils.bestMessage(e)); }
            }
        };worker.execute();
    }
    private void executeBatch(List<String> keys) {
        busy(true);progress.setMaximum(keys.size());
        worker = new SwingWorker<RedisDiagnosticsService.BatchResult, Void>() {
            @Override protected RedisDiagnosticsService.BatchResult doInBackground() {
                return RedisDiagnosticsService.unlink(context, keys, cancelled::get, RedisDiagnosticsPanel.this::progress);
            }
            @Override protected void done() {
                if (closed) return;
                try { var result = get();status.setText(String.format(tr("batchResult"), result.attempted(), result.removed(), result.cancelled())); }
                catch (Exception e) { status.setText(ExceptionMessageUtils.bestMessage(e)); }
                finally { busy(false);progress.setMaximum(1000); }
            }
        };worker.execute();
    }
    @Override public void close() { closed = true;cancelled.set(true);if (worker != null) worker.cancel(true); }
}
