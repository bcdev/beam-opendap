package org.esa.beam.opendap.ui;

import org.junit.*;

import javax.swing.tree.DefaultMutableTreeNode;

import static org.junit.Assert.*;

public class CatalogTree_createRootNode {

    @Test
    public void testThatAWellDefinedRootNodeIsCreated() {
        final DefaultMutableTreeNode rootNode = CatalogTree.createRootNode();
        assertNotNull(rootNode);
        assertEquals(true, rootNode instanceof DefaultMutableTreeNode);

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode;
        final Object userObject = node.getUserObject();
        assertNotNull(userObject);
        assertEquals(true, userObject instanceof String);
        assertEquals("root", userObject.toString());
    }
}
