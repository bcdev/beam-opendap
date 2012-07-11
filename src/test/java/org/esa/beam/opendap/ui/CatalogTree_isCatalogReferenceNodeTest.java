package org.esa.beam.opendap.ui;

import org.esa.beam.opendap.OpendapLeaf;
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
    public void testThatUserObjectWhichIsNoStringIsResolvedToFalse() {
        final Integer userObject = 4;
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isCatalogReferenceNode(notADapNode));
    }

    @Test
    public void testThatOpendapLeafWhichIsNoCatalogRefIsResolvedToFalse() {
        final Object userObject = new OpendapLeaf("any");
        final DefaultMutableTreeNode noDapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isCatalogReferenceNode(noDapNode));
    }

    @Test
    public void testThatOpendapLeafWhichIsCatalogRefIsResolvedToTrue() {
        final OpendapLeaf opendapLeaf = new OpendapLeaf("any");
        opendapLeaf.setCatalogReference(true);
        final DefaultMutableTreeNode notDapNode = new DefaultMutableTreeNode(opendapLeaf);
        assertEquals(true, CatalogTree.isCatalogReferenceNode(notDapNode));
    }
}
