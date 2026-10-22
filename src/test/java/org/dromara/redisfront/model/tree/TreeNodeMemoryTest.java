package org.dromara.redisfront.model.tree;

import org.dromara.redisfront.model.MemoryUsageResult;
import org.junit.jupiter.api.Test;
import static org.dromara.redisfront.model.MemoryUsageResult.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class TreeNodeMemoryTest {
    private TreeNodeInfo node(String key, boolean leaf) { return new TreeNodeInfo(key, key, leaf); }

    @Test void directorySumsUseLongAndZeroRemainsAValidMeasurement() {
        var parent = node("group", false);var a = node("a", true);var b = node("b", true);var empty = node("empty", true);
        parent.add(a);parent.add(b);parent.add(empty);
        a.setMemorySize(Integer.MAX_VALUE);b.setMemorySize(Integer.MAX_VALUE);empty.setMemorySize(0);
        assertEquals(4294967294L, parent.memoryStats().bytes());
        assertEquals(3, parent.memoryStats().measured());
        assertTrue(parent.toString().contains("GB"));
        assertTrue(empty.toString().contains("0"));
        assertFalse(node("new", true).toString().contains("["));
    }

    @Test void aggregatesRefreshAfterValueAndTreeChanges() {
        var root = node("root", false);var group = node("group", false);var a = node("a", true);
        root.add(group);group.add(a);a.setMemorySize(10);
        assertEquals(10, root.memoryStats().bytes());
        a.setMemorySize(20);assertEquals(20, root.memoryStats().bytes());
        var b = node("b", true);b.setMemorySize(30);group.add(b);assertEquals(50, root.memoryStats().bytes());
        group.remove(a);assertEquals(30, root.memoryStats().bytes());
        group.removeAllChildren();assertEquals(0, root.memoryStats().keys());assertEquals(0, root.memoryStats().bytes());
    }

    @Test void allDirectoryLevelsCountKeysAndPartialResultsDoNotPretendToBeComplete() {
        var root = node("root", false);var nested = node("nested", false);root.add(nested);
        for (int i = 0; i < 3; i++) nested.add(node("key" + i, true));
        ((TreeNodeInfo) nested.getChildAt(0)).setMemorySize(123);
        ((TreeNodeInfo) nested.getChildAt(1)).setMemoryUsage(MemoryUsageResult.state(MISSING));
        assertEquals(3, root.memoryStats().keys());assertEquals(3, nested.memoryStats().keys());
        assertEquals(1, root.memoryStats().missing());assertEquals(1, root.memoryStats().unfinished());
        assertTrue(root.toString().contains("1/3"));assertTrue(nested.toString().contains("(3)"));
    }

    @Test void retryClearsOldBytesAndTracksEveryOutcomeSeparately() {
        var parent = node("parent", false);
        for (var state : new MemoryUsageResult.Status[]{ANALYZING, MISSING, FAILED, UNSUPPORTED, CANCELLED}) {
            var child = node(state.name(), true);parent.add(child);child.setMemorySize(99);
            child.setMemoryUsage(MemoryUsageResult.state(state));
        }
        var stats = parent.memoryStats();
        assertEquals(0, stats.bytes());assertEquals(0, stats.measured());
        assertEquals(1, stats.pending());assertEquals(1, stats.missing());assertEquals(1, stats.failed());
        assertEquals(1, stats.unsupported());assertEquals(1, stats.cancelled());assertEquals(2, stats.unfinished());
    }
}
