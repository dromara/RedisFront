package org.dromara.redisfront.ui.components.connection;

import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionSafetySettingsPanelTest {
    private static ConnectionSafetySettingsPanel panel() {
        var bundle = ResourceBundle.getBundle(Constants.APP_RESOURCE_BUNDLE, Locale.SIMPLIFIED_CHINESE);
        return new ConnectionSafetySettingsPanel(bundle::getString);
    }

    private static JCheckBox readOnly(ConnectionSafetySettingsPanel panel) {
        return (JCheckBox) Arrays.stream(panel.getComponents())
                .filter(c -> "connection.readOnly".equals(c.getName())).findFirst().orElseThrow();
    }

    private static JButton sentinelOptions(ConnectionSafetySettingsPanel panel) {
        return (JButton) Arrays.stream(panel.getComponents())
                .filter(c -> "connection.sentinelOptions".equals(c.getName())).findFirst().orElseThrow();
    }

    private static String endpoints(ConnectionSafetySettingsPanel panel) {
        var setting = new RedisConnectContext.SettingInfo();
        panel.applyTo(setting);
        return setting.getSentinelEndpoints();
    }

    @Test void savedConnectionRestoresOptionsAndCanReturnToWritable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var panel = panel();
            var context = new RedisConnectContext();
            context.setConnectTypeMode(ConnectType.NORMAL);
            context.setSetting(new RedisConnectContext.SettingInfo(1500, ":", 2000, 3000, 2));
            readOnly(panel).setSelected(true);
            panel.setDetectedMode(RedisMode.SENTINEL);
            panel.setSentinelEndpoints("  sentinel.internal:26380, [::1]:26381  ");
            panel.applyTo(context.getSetting());

            var entity = context.toEntity();
            entity.setId(1);
            var restored = entity.getConnectContext().getSetting();
            var editor = panel();
            editor.load(restored);
            assertTrue(readOnly(editor).isSelected());
            assertTrue(sentinelOptions(editor).isVisible());
            assertEquals("sentinel.internal:26380, [::1]:26381", endpoints(editor));
            assertEquals(1500, restored.getLoadKeyNum());
            assertEquals(2, restored.getDefaultDatabase());

            readOnly(editor).setSelected(false);
            editor.setSentinelEndpoints("");
            editor.applyTo(restored);
            context.setSetting(restored);
            var updated = context.toEntity();
            updated.setId(1);
            assertFalse(updated.getConnectContext().getSetting().isReadOnly());
            assertEquals("", updated.getConnectContext().getSetting().getSentinelEndpoints());
        });
    }

    @Test void olderProfilesWithoutNewOptionsRemainWritable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var context = new RedisConnectContext();
            context.setConnectTypeMode(ConnectType.NORMAL);
            var entity = context.toEntity();
            entity.setId(1);
            entity.setSetting("{\"loadKeyNum\":5000,\"keySeparator\":\":\",\"redisTimeout\":1000,\"sshTimeout\":1000}");
            var editor = panel();
            readOnly(editor).setSelected(true);
            editor.setSentinelEndpoints("old:26379");
            editor.load(entity.getConnectContext().getSetting());
            assertFalse(readOnly(editor).isSelected());
            assertEquals("", endpoints(editor));
            assertFalse(sentinelOptions(editor).isVisible());
        });
    }

    @Test void invalidSupplementLeavesThePreviousEndpointsIntact() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = panel();
            var setting = new RedisConnectContext.SettingInfo(5000, ":", 1000, 1000);
            setting.setSentinelEndpoints("valid:26379");
            editor.load(setting);
            readOnly(editor).setSelected(true);
            var error = assertThrows(RedisFrontException.class, () -> editor.setSentinelEndpoints("missing-port"));
            assertSame(sentinelOptions(editor), error.getComponent());
            assertTrue(error.getMessage().contains("host:port"));
            assertFalse(setting.isReadOnly());
            assertEquals("valid:26379", setting.getSentinelEndpoints());
            assertEquals("valid:26379", endpoints(editor));
        });
    }

    @Test void onlyADetectedSentinelOffersSupplementaryConfiguration() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = panel();
            assertFalse(sentinelOptions(editor).isVisible());
            editor.promptSentinelEndpoints(null); // Unknown mode must not create a dialog.
            for (var mode : new RedisMode[]{RedisMode.STANDALONE, RedisMode.CLUSTER}) {
                editor.setDetectedMode(RedisMode.SENTINEL);
                assertTrue(sentinelOptions(editor).isVisible());
                editor.setDetectedMode(mode);
                assertFalse(sentinelOptions(editor).isVisible());
                editor.promptSentinelEndpoints(null);
            }
        });
    }

    @Test void addressChangesHideOptionsUntilRetestedAndPreserveSavedEndpoints() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = panel();
            var setting = new RedisConnectContext.SettingInfo();
            setting.setSentinelEndpoints("sentinel.internal:26380");
            editor.load(setting);
            editor.setDetectedMode(null);
            assertFalse(sentinelOptions(editor).isVisible());
            assertEquals("sentinel.internal:26380", endpoints(editor));
            editor.setDetectedMode(RedisMode.SENTINEL);
            assertTrue(sentinelOptions(editor).isVisible());
            assertEquals("sentinel.internal:26380", endpoints(editor));
        });
    }

    @Test void confirmedStandaloneAndClusterProfilesDoNotSaveSentinelEndpoints() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (var mode : new RedisMode[]{RedisMode.STANDALONE, RedisMode.CLUSTER}) {
                var editor = panel();
                var setting = new RedisConnectContext.SettingInfo();
                setting.setSentinelEndpoints("old-sentinel:26380");
                editor.load(setting);
                editor.setDetectedMode(mode);
                editor.applyTo(setting);
                assertEquals("", setting.getSentinelEndpoints());
                assertFalse(sentinelOptions(editor).isVisible());
            }
        });
    }
}
