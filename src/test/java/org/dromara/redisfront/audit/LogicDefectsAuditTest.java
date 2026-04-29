package org.dromara.redisfront.audit;

import org.dromara.redisfront.ui.dialog.ImportConfigDialog;
import org.dromara.redisfront.ui.components.scanner.context.RedisScanContext;
import org.dromara.redisfront.ui.widget.main.fragment.MainTabView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class LogicDefectsAuditTest {

    @Test
    void redisScanContext_setKeyList_can_throw_when_previous_keys_is_immutable() {
        RedisScanContext<String> ctx = new RedisScanContext<>();
        ctx.setKeyList(List.of("a"));

        Assertions.assertDoesNotThrow(() -> ctx.setKeyList(List.of("b")));
        Assertions.assertEquals(2, ctx.getKeyList().size());
    }

    @Test
    void importConfigDialog_genConnectInfo_port_cast_can_throw_when_port_is_long() {
        Map<String, Object> raw = Map.of("port", 6379L);
        Assertions.assertEquals(6379, ImportConfigDialog.toInt(raw.get("port")));
    }

    @Test
    void mainTabView_id_compare_can_throw_npe_when_event_id_is_null_due_to_unboxing() {
        Assertions.assertFalse(MainTabView.matchEventId(1, null));
    }
}
