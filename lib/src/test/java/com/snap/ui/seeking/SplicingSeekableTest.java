package com.snap.ui.seeking;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SplicingSeekableTest {

    Seekable<Integer> seekable1;
    Seekable<Integer> seekable2;

    @Before
    public void setup() {
        seekable1 = new ListSeekable<>(Lists.newArrayList(1, 2, 3, 4, 5));
        seekable2 = new ListSeekable<>(Lists.newArrayList(10, 11, 12));
    }

    @Test
    public void basic_splice() throws Exception {
        Seekable<Integer> combined = new SplicingSeekable<>(seekable1, seekable2, 0);
        List<Integer> list = Lists.newArrayList(combined);
        assertEquals(Lists.newArrayList(10, 11, 12, 1, 2, 3, 4, 5), list);


        combined = new SplicingSeekable<>(seekable1, seekable2, 3);
        list = Lists.newArrayList(combined);
        assertEquals(Lists.newArrayList(1, 2, 3, 10, 11, 12, 4, 5), list);


        combined = new SplicingSeekable<>(seekable1, seekable2, 8);
        list = Lists.newArrayList(combined);
        assertEquals(Lists.newArrayList(1, 2, 3, 4, 5, 10, 11, 12), list);

        combined = new SplicingSeekable<>(seekable1, Seekables.<Integer>empty(), 3);
        list = Lists.newArrayList(combined);
        assertEquals(Lists.newArrayList(1, 2, 3, 4, 5), list);

        combined = Seekables.splice(Seekables.<Integer>empty(), seekable1, 3);
        list = Lists.newArrayList(combined);
        assertEquals(Lists.newArrayList(1, 2, 3, 4, 5), list);
    }
}