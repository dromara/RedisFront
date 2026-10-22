package org.dromara.redisfront.ui.components.memory;

import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.model.MemoryUsageResult;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.dromara.redisfront.service.RedisMemoryService;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class KeyMemoryAnalysisControllerTest {
    private final ResourceBundle tr = ResourceBundle.getBundle(Constants.APP_RESOURCE_BUNDLE, Locale.SIMPLIFIED_CHINESE);
    private static TreeNodeInfo root() {
        var root = new TreeNodeInfo("group", "group", false);
        root.add(new TreeNodeInfo("a", "a", true));root.add(new TreeNodeInfo("b", "b", true));return root;
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        var ready = new AtomicBoolean();
        do { SwingUtilities.invokeAndWait(() -> ready.set(condition.getAsBoolean()));if (ready.get()) return;Thread.sleep(10); } while (System.nanoTime() < deadline);
        fail("UI task did not finish");
    }
    private static void waitFor(CountDownLatch latch) {
        try { assertTrue(latch.await(5, TimeUnit.SECONDS)); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    @Test void finalMeasurementsAreAppliedEvenWhenPublishIsStillBuffered() throws Exception {
        var root = root();var context = new RedisConnectContext();
        var controller = new AtomicReference<KeyMemoryAnalysisController>();
        SwingUtilities.invokeAndWait(() -> {
            var tree = new JTree(new DefaultTreeModel(root));
            var c = new KeyMemoryAnalysisController(tree, () -> context, tr::getString, (snapshot, keys, cancelled, updates) -> {
                var first = new RedisMemoryService.Summary(2, 1, 1, 0, 0, 0, 3_000_000_000L, false, null);
                var done = new RedisMemoryService.Summary(2, 2, 2, 0, 0, 0, 3_000_000_000L, false, null);
                updates.accept(new RedisMemoryService.Update("a", MemoryUsageResult.measured(3_000_000_000L), first));
                updates.accept(new RedisMemoryService.Update("b", MemoryUsageResult.measured(0), done));return done;
            });controller.set(c);c.start(root);
        });
        await(() -> !controller.get().isRunning());
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(3_000_000_000L, root.memoryStats().bytes());assertEquals(2, root.memoryStats().measured());
            controller.get().close();
        });
    }

    @Test void repeatedStartDoesNotCreateASecondTaskAndCancellationClearsPendingState() throws Exception {
        var root = root();var context = new RedisConnectContext();var entered = new CountDownLatch(1);var release = new CountDownLatch(1);
        var calls = new AtomicInteger();var controller = new AtomicReference<KeyMemoryAnalysisController>();
        SwingUtilities.invokeAndWait(() -> {
            var c = new KeyMemoryAnalysisController(new JTree(new DefaultTreeModel(root)), () -> context, tr::getString, (snapshot, keys, cancelled, updates) -> {
                calls.incrementAndGet();entered.countDown();waitFor(release);return new RedisMemoryService.Summary(2, 0, 0, 0, 0, 0, 0, cancelled.getAsBoolean(), null);
            });controller.set(c);c.start(root);c.start(root);
        });
        try { waitFor(entered);SwingUtilities.invokeAndWait(() -> controller.get().cancel()); } finally { release.countDown(); }
        await(() -> !controller.get().isRunning());
        SwingUtilities.invokeAndWait(() -> { assertEquals(1, calls.get());assertEquals(2, root.memoryStats().cancelled());assertEquals(0, root.memoryStats().pending());controller.get().close(); });
    }

    @Test void treeReplacementCancelsOldWorkAndNeverUpdatesTheReplacementModel() throws Exception {
        var old = root();var replacement = root();var context = new RedisConnectContext();var entered = new CountDownLatch(1);var release = new CountDownLatch(1);var finished = new CountDownLatch(1);
        var controller = new AtomicReference<KeyMemoryAnalysisController>();var tree = new AtomicReference<JTree>();
        SwingUtilities.invokeAndWait(() -> {
            tree.set(new JTree(new DefaultTreeModel(old)));
            var c = new KeyMemoryAnalysisController(tree.get(), () -> context, tr::getString, (snapshot, keys, cancelled, updates) -> {
                entered.countDown();waitFor(release);
                var done = new RedisMemoryService.Summary(2, 1, 1, 0, 0, 0, 99, true, null);
                updates.accept(new RedisMemoryService.Update("a", MemoryUsageResult.measured(99), done));finished.countDown();return done;
            });controller.set(c);c.start(old);
        });
        try { waitFor(entered);SwingUtilities.invokeAndWait(() -> tree.get().setModel(new DefaultTreeModel(replacement))); } finally { release.countDown(); }
        waitFor(finished);
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(controller.get().isRunning());assertFalse(controller.get().footer().isVisible());
            assertEquals(0, replacement.memoryStats().measured());assertEquals(0, old.memoryStats().pending());controller.get().close();
        });
    }

    @Test void backgroundCommandsKeepTheOriginalDatabaseAndLateResultsAreDiscarded() throws Exception {
        var root = root();var context = new RedisConnectContext();context.setDatabase(3);
        var entered = new CountDownLatch(1);var release = new CountDownLatch(1);var database = new AtomicInteger();var controller = new AtomicReference<KeyMemoryAnalysisController>();
        SwingUtilities.invokeAndWait(() -> {
            var c = new KeyMemoryAnalysisController(new JTree(new DefaultTreeModel(root)), () -> context, tr::getString, (snapshot, keys, cancelled, updates) -> {
                entered.countDown();waitFor(release);database.set(snapshot.getDatabase());return new RedisMemoryService.Summary(2, 0, 0, 0, 0, 0, 0, true, null);
            });controller.set(c);c.start(root);
        });
        try { waitFor(entered);SwingUtilities.invokeAndWait(() -> context.setDatabase(9)); } finally { release.countDown(); }
        await(() -> !controller.get().isRunning());
        SwingUtilities.invokeAndWait(() -> { assertEquals(3, database.get());assertEquals(0, root.memoryStats().pending());assertFalse(controller.get().footer().isVisible());controller.get().close(); });
    }
}
