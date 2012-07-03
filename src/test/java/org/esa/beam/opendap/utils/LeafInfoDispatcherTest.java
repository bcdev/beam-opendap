package org.esa.beam.opendap.utils;

import org.esa.beam.opendap.ui.CatalogTree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LeafInfoDispatcherTest {

    @Test
    public void testThatNoDAPVariablesAreAddedWhenLeafHasNoVariables() {
        CatalogTree.OPeNDAP_Leaf leaf = new CatalogTree.OPeNDAP_Leaf("empty");
        LeafInfoDispatcher.dispatchLeafInfo(leaf);
        assertEquals(0, leaf.getDAPVariables().length);
    }

} 