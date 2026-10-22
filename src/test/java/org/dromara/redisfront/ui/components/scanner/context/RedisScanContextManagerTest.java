package org.dromara.redisfront.ui.components.scanner.context;

import io.lettuce.core.ScanCursor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedisScanContextManagerTest {
    @Test void renameContinuesPaginationWithTheLoadedRowsAndCursor() {
        var manager = new RedisScanContextManager<String>();
        var loaded = manager.getContext("old");
        loaded.setKeyList(List.of("first", "second"));
        loaded.setScanCursor(new ScanCursor("42", false));
        loaded.setSearchKey("field:*");
        loaded.setLimit(100L);
        manager.getContext("new").setKeyList(List.of("stale destination"));

        manager.rename("old", "new");

        var nextPage = manager.getContext("new");
        assertSame(loaded, nextPage);
        assertEquals(List.of("first", "second"), nextPage.getKeyList());
        assertEquals("42", nextPage.getScanCursor().getCursor());
        assertEquals("field:*", nextPage.getSearchKey());
        assertEquals(100L, nextPage.getLimit());
        assertTrue(manager.getContext("old").getKeyList().isEmpty());
    }

    @Test void ttlOnlyUpdateKeepsTheCurrentScanState() {
        var manager = new RedisScanContextManager<String>();
        var loaded = manager.getContext("same");
        loaded.setKeyList(List.of("existing"));
        manager.rename("same", "same");
        assertSame(loaded, manager.getContext("same"));
        assertEquals(List.of("existing"), manager.getContext("same").getKeyList());
    }
}
