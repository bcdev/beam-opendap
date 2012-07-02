package org.esa.beam.opendap.ui;

import org.junit.*;

import javax.swing.tree.DefaultMutableTreeNode;

import static org.junit.Assert.*;

public class CatalogTree_isCatalogReferenceNodeTest {

    @Test
    public void testThatNullIsResolvedToFalse() {
        final Object notADapNode = null;
        assertEquals(false, CatalogTree.isCatalogReferenceNode(notADapNode));
    }

    @Test
    public void testThatAUserObjectWhichIsNotAStringIsResolvedToFalse() {
        final Integer userObject = new Integer(4);
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isCatalogReferenceNode(notADapNode));
    }

    @Test
    public void testThatAOPeNDAP_LeafWhichIsNotACatalogRefIsResolvedToFalse() {
        final Object userObject = new CatalogTree.OPeNDAP_Leaf("any");
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isCatalogReferenceNode(notADapNode));
    }

    @Test
    public void testThatAOPeNDAP_LeafWhichIsACatalogRefIsResolvedToTrue() {
        final CatalogTree.OPeNDAP_Leaf oPeNDAP_leaf = new CatalogTree.OPeNDAP_Leaf("any");
        oPeNDAP_leaf.setCatalogReference(true);
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(oPeNDAP_leaf);
        assertEquals(true, CatalogTree.isCatalogReferenceNode(notADapNode));
    }
}
