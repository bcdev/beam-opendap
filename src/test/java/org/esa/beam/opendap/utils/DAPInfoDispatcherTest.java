package org.esa.beam.opendap.utils;

import org.esa.beam.opendap.OpendapLeaf;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DAPInfoDispatcherTest {

    @Test
    public void testThatNoDAPVariablesAreAddedWhenLeafHasNoVariables() {
        OpendapLeaf leaf = new OpendapLeaf("empty");
        DAPInfoDispatcher.dispatchLeafInfo(leaf);
        assertEquals(0, leaf.getDAPVariables().length);
    }

} 