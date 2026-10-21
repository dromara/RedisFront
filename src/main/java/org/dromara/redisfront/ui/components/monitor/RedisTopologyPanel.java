package org.dromara.redisfront.ui.components.monitor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RedisTopologyPanel extends JPanel {
    private final Function<String, String> translate;
    private final JLabel summary = new JLabel();
    private final JLabel replication = new JLabel();
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private RedisTopologyMonitor.Snapshot snapshot;
    private boolean updating;

    public RedisTopologyPanel(Function<String, String> translate, Consumer<String> selectNode) {
        super(new BorderLayout(0, 10));
        this.translate = translate;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        var details = new JPanel(new GridLayout(0, 1, 0, 8));
        details.add(summary);
        details.add(replication);
        add(details, BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (updating || event.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            selectNode.accept(String.valueOf(model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0)));
        });
        var scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        add(scroll, BorderLayout.CENTER);
        relocalize();
    }

    private String tr(String key) { return translate.apply("ReportPageView." + key); }

    public void relocalize() {
        model.setColumnIdentifiers(new String[]{tr("node"), tr("role"), tr("state"), tr("upstream"), tr("slots")});
        if (snapshot != null) setSnapshot(snapshot);
    }

    public void setSnapshot(RedisTopologyMonitor.Snapshot snapshot) {
        this.snapshot = snapshot;
        updating = true;
        try {
            model.setRowCount(0);
            for (var node : snapshot.nodes()) {
                model.addRow(new Object[]{node.address(), role(node.role()), state(node.state()), node.upstream(), node.slots()});
            }
            var s = snapshot.status();
            if (s.containsKey("cluster_state")) {
                summary.setText(String.format(tr("clusterSummary"), s.get("cluster_state"),
                        s.getOrDefault("cluster_slots_ok", "-"), s.getOrDefault("cluster_slots_assigned", "-"),
                        s.getOrDefault("cluster_slots_fail", "-"), s.getOrDefault("cluster_known_nodes", "-"),
                        s.getOrDefault("cluster_size", "-")));
            } else if (s.containsKey("sentinel_master")) {
                summary.setText(String.format(tr("sentinelSummary"), s.get("sentinel_master"),
                        s.get("sentinel_known"), s.get("sentinel_quorum"), s.getOrDefault("sentinel_ckquorum", "-")));
            } else {
                summary.setText(s.containsKey("sentinel_unavailable") ? tr("sentinelUnavailable") : tr("replicationSummary"));
            }
            summary.setToolTipText(summary.getText());
            var info = snapshot.info();
            replication.setText(String.format(tr("selectedReplication"), snapshot.selected().address(),
                    role(Objects.toString(info.get("role"), snapshot.selected().role())),
                    info.getOrDefault("master_link_status", "-"),
                    info.getOrDefault("master_repl_offset", "-"), info.getOrDefault("slave_repl_offset", "-")));
            replication.setToolTipText(replication.getText());
        } finally { updating = false; }
    }

    public String role(String role) {
        return switch (role) { case "master" -> tr("master"); case "slave", "replica" -> tr("replica"); default -> role; };
    }

    private String state(String state) {
        return switch (state) {
            case "connected", "online", "up", "master", "slave" -> tr("online");
            case "disconnected", "down" -> tr("offline");
            default -> state;
        };
    }
}
