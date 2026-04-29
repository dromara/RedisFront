package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LeftSearchFragmentTest {

    @Test
    void listPageRanges_should_generate_one_page_for_single_item() {
        List<long[]> ranges = LeftSearchFragment.listPageRanges(1, 1000);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertArrayEquals(new long[]{0, 0}, ranges.getFirst());
    }

    @Test
    void listPageRanges_should_generate_two_pages_for_1001_items() {
        List<long[]> ranges = LeftSearchFragment.listPageRanges(1001, 1000);
        Assertions.assertEquals(2, ranges.size());
        Assertions.assertArrayEquals(new long[]{0, 999}, ranges.get(0));
        Assertions.assertArrayEquals(new long[]{1000, 1000}, ranges.get(1));
    }
}

