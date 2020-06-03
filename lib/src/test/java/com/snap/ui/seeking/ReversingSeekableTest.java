package com.snap.ui.seeking;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReversingSeekableTest {

    Seekable<Integer> seekable;

    @Before
    public void setup() {
        seekable = new ListSeekable<>(Lists.newArrayList(1, 2, 3));
    }

    @Test
    public void basic_test() throws Exception {
        Seekable<Integer> reversed = Seekables.reverse(seekable);
        List<Integer> list = Lists.newArrayList(reversed);
        assertEquals(Lists.newArrayList(3, 2, 1), list);

        Seekable<Integer> forward = Seekables.reverse(list);
        List<Integer> list2 = Lists.newArrayList(forward);
        assertEquals(Lists.newArrayList(1, 2, 3), list2);
    }
}