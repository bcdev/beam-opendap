package org.esa.beam.opendap.utils;

import org.esa.beam.opendap.ui.CatalogTree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DAPInfoDispatcherTest {

    @Test
    public void testThatNoDAPVariablesAreAddedWhenLeafHasNoVariables() {
        CatalogTree.OPeNDAP_Leaf leaf = new CatalogTree.OPeNDAP_Leaf("empty");
        DAPInfoDispatcher.dispatchLeafInfo(leaf);
        assertEquals(0, leaf.getDAPVariables().length);
    }

} 