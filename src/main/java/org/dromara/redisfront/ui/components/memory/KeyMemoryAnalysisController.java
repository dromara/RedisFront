package org.dromara.redisfront.ui.components.memory;

import com.formdev.flatlaf.FlatClientProperties;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.model.MemoryUsageResult;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.dromara.redisfront.service.RedisMemoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

import static org.dromara.redisfront.model.MemoryUsageResult.Status.*;

/** One cancellable analysis bound to a tree model and database snapshot. UI updates run on the EDT. */
public final class KeyMemoryAnalysisController implements AutoCloseable {
    @FunctionalInterface interface Analyzer {
        RedisMemoryService.Summary analyze(RedisConnectContext context, Collection<String> keys,
                                           BooleanSupplier cancelled, Consumer<RedisMemoryService.Update> updates);
    }
    private final JTree tree;
    private final Supplier<RedisConnectContext> context;
    private final Function<String, String> tr;
    private final Analyzer analyzer;
    private final JPanel footer = new JPanel(new BorderLayout(6, 0));
    private final JLabel status = new JLabel();
    private final JButton cancel = new JButton();
    private final PropertyChangeListener modelListener = event -> reset();
    private MemoryTask active;
    private boolean closed;

    public KeyMemoryAnalysisController(JTree tree, Supplier<RedisConnectContext> context, Function<String, String> tr) {
        this(tree, context, tr, RedisMemoryService::analyze);
    }

    KeyMemoryAnalysisController(JTree tree, Supplier<RedisConnectContext> context, Function<String, String> tr, Analyzer analyzer) {
        this.tree = tree;
        this.context = context;
        this.tr = tr;
        this.analyzer = analyzer;
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(2, 1, 0, 0));
        status.setName("memory.status");
        cancel.setName("memory.cancel");
        cancel.setText(tr.apply("Memory.cancel"));
        cancel.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        cancel.putClientProperty(FlatClientProperties.STYLE, "margin: 1,5,1,5");
        cancel.addActionListener(event -> cancel());
        footer.add(status, BorderLayout.CENTER);
        footer.add(cancel, BorderLayout.EAST);
        footer.setVisible(false);
        tree.addPropertyChangeListener("model", modelListener);
    }

    public JComponent footer() { return footer; }
    public boolean isRunning() { return active != null; }

    public void start(TreeNodeInfo selected) {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> start(selected));return; }
        if (closed || active != null || !(tree.getModel() instanceof DefaultTreeModel model)
                || selected.getRoot() != model.getRoot()) return;
        var targets = new LinkedHashMap<String, TreeNodeInfo>();
        var nodes = selected.depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            var node = (TreeNodeInfo) nodes.nextElement();
            if (Boolean.TRUE.equals(node.getIsLeafNode())) targets.putIfAbsent(node.key(), node);
        }
        if (targets.isEmpty()) return;
        var snapshot = context.get().clone();
        for (var node : targets.values()) node.setMemoryUsage(MemoryUsageResult.state(ANALYZING));
        active = new MemoryTask(model, targets, snapshot);
        refresh(model, targets.values());
        showProgress("running", active.lastProgress);
        footer.setVisible(true);
        cancel.setEnabled(true);
        cancel.setVisible(true);
        footer.revalidate();
        active.execute();
    }

    public void cancel() {
        if (active == null) return;
        active.cancelled.set(true);
        cancel.setEnabled(false);
        status.setText(tr.apply("Memory.cancelling"));
    }

    public void reset() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::reset);return; }
        if (active != null) {
            active.cancelled.set(true);
            var previous = active;
            active = null;
            previous.finishPending(CANCELLED);
        }
        footer.setVisible(false);
    }

    @Override public void close() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::close);return; }
        closed = true;
        reset();
        tree.removePropertyChangeListener("model", modelListener);
    }

    private void refresh(DefaultTreeModel model, Collection<TreeNodeInfo> changed) {
        if (tree.getModel() != model) return;
        Set<TreeNodeInfo> affected = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var changedNode : changed) {
            for (TreeNodeInfo node = changedNode; node != null; node = node.getParent() instanceof TreeNodeInfo parent ? parent : null) affected.add(node);
        }
        for (var node : affected) model.nodeChanged(node);
    }

    private void showProgress(String state, RedisMemoryService.Summary progress) {
        status.setText(String.format(tr.apply("Memory." + state), progress.processed(), progress.total(),
                progress.measured() == 0 ? "—" : TreeNodeInfo.formatMemorySize(progress.bytes())));
        String detail = tr.apply("Memory.scope").formatted(progress.total()) + "\n"
                + tr.apply("Memory.counts").formatted(progress.measured(), progress.missing(), progress.failed(), progress.unsupported(), progress.total() - progress.processed())
                + "\n" + tr.apply("Memory.metric");
        if (progress.stopReason() != null) detail += "\n" + (progress.unsupported() > 0 ? tr.apply("Memory.unsupportedHelp")
                : ExceptionMessageUtils.userMessage(new RuntimeException(progress.stopReason()), tr));
        status.setToolTipText(html(detail));
        footer.setToolTipText(status.getToolTipText());
    }

    private static String html(String text) {
        return "<html><div style='width:320px'>" + text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("\n", "<br>") + "</div></html>";
    }

    private record Batch(List<RedisMemoryService.Update> results, RedisMemoryService.Summary progress) { }

    private final class MemoryTask extends SwingWorker<RedisMemoryService.Summary, Batch> {
        private final DefaultTreeModel model;
        private final LinkedHashMap<String, TreeNodeInfo> targets;
        private final RedisConnectContext snapshot;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final Map<String, MemoryUsageResult> results = new LinkedHashMap<>();
        private RedisMemoryService.Summary lastProgress;

        private MemoryTask(DefaultTreeModel model, LinkedHashMap<String, TreeNodeInfo> targets, RedisConnectContext snapshot) {
            this.model = model;this.targets = targets;this.snapshot = snapshot;
            lastProgress = new RedisMemoryService.Summary(targets.size(), 0, 0, 0, 0, 0, 0, false, null);
        }

        private boolean current() {
            return !closed && active == this && tree.getModel() == model && Objects.equals(context.get().getDatabase(), snapshot.getDatabase());
        }

        @Override protected RedisMemoryService.Summary doInBackground() {
            var pending = new ArrayList<RedisMemoryService.Update>();
            long[] lastPublish = {System.nanoTime()};
            var summary = analyzer.analyze(snapshot, targets.keySet(), cancelled::get, update -> {
                results.put(update.key(), update.result());
                pending.add(update);
                long now = System.nanoTime();
                if (pending.size() >= 64 || now - lastPublish[0] >= 100_000_000L) {
                    publish(new Batch(List.copyOf(pending), update.progress()));
                    pending.clear();lastPublish[0] = now;
                }
            });
            if (!pending.isEmpty()) publish(new Batch(List.copyOf(pending), summary));
            return summary;
        }

        @Override protected void process(List<Batch> chunks) {
            if (!current()) return;
            var changed = new ArrayList<TreeNodeInfo>();
            for (var chunk : chunks) {
                for (var update : chunk.results()) {
                    var node = targets.get(update.key());
                    node.setMemoryUsage(update.result());changed.add(node);
                }
                lastProgress = chunk.progress();
            }
            refresh(model, changed);
            if (!cancelled.get()) showProgress("running", lastProgress);
        }

        private void finishPending(MemoryUsageResult.Status state) {
            var changed = new ArrayList<TreeNodeInfo>();
            for (var node : targets.values()) if (node.getMemoryUsage().status() == ANALYZING) {
                node.setMemoryUsage(MemoryUsageResult.state(state));changed.add(node);
            }
            refresh(model, changed);
        }

        @Override protected void done() {
            if (!current()) {
                if (active == this) reset();
                return;
            }
            try {
                var summary = get();
                var changed = new ArrayList<TreeNodeInfo>();
                for (var result : results.entrySet()) {
                    var node = targets.get(result.getKey());
                    if (node.getMemoryUsage() != result.getValue()) { node.setMemoryUsage(result.getValue());changed.add(node); }
                }
                refresh(model, changed);
                finishPending(summary.cancelled() ? CANCELLED : UNANALYZED);
                showProgress(summary.cancelled() ? "cancelled" : summary.stopReason() != null || summary.failed() + summary.unsupported() > 0 ? "stopped" : "complete", summary);
            } catch (Exception error) {
                finishPending(UNANALYZED);
                showProgress("stopped", lastProgress);
                status.setToolTipText(html(ExceptionMessageUtils.userMessage(error, tr)));
            } finally {
                active = null;
                cancel.setVisible(false);
                footer.revalidate();
            }
        }
    }
}
