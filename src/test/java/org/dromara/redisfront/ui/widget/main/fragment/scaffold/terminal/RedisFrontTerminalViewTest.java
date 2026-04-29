package org.dromara.redisfront.ui.widget.main.fragment.scaffold.terminal;

import org.dromara.redisfront.commons.codec.ValueViewType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RedisFrontTerminalViewTest {

    @Test
    void resolveViewType_should_accept_synonyms() {
        Assertions.assertEquals(ValueViewType.AUTO, RedisFrontTerminal.resolveViewType("auto"));
        Assertions.assertEquals(ValueViewType.LATIN1, RedisFrontTerminal.resolveViewType("latin-1"));
        Assertions.assertEquals(ValueViewType.GB18030, RedisFrontTerminal.resolveViewType("gb18030"));
    }

    @Test
    void format_should_use_selected_view_type() {
        byte[] raw = new byte[]{0x00, 0x10, 0x2A, (byte) 0xFF};
        String text = RedisFrontTerminal.format(raw, "", ValueViewType.HEX);
        Assertions.assertEquals("00102aff", text);
    }

    @Test
    void format_should_apply_to_list_items() {
        byte[] raw = new byte[]{0x00, 0x01};
        String text = RedisFrontTerminal.format(List.of(raw), "", ValueViewType.HEX);
        Assertions.assertTrue(text.contains("0001"));
    }
}

