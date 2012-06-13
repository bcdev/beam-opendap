package org.esa.beam.opendap.ui;

import org.junit.*;

import javax.swing.tree.DefaultMutableTreeNode;

import static org.junit.Assert.*;

public class CatalogTree_isDapNodeTest {

    @Test
    public void testThatNullIsResolvedToFalse() {
        final Object notADapNode = null;
        assertEquals(false, CatalogTree.isDapNode(notADapNode));
    }

    @Test
    public void testThatAUserObjectWhichIsNotAsStringIsResolvedToFalse() {
        final Integer userObject = new Integer(4);
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isDapNode(notADapNode));
    }

    @Test
    public void testThatAUserObjectIsAStringButDoesNotStartWithHttpIsResolvedToFalse() {
        final String userObject = "DoesNotStartWithHttp";
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isDapNode(notADapNode));
    }

    @Test
    public void testThatAUserObjectIsAStringAndStartsWithHttpIsResolvedToTrue() {
        final String userObject = "http://any.thing";
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(true, CatalogTree.isDapNode(notADapNode));
    }
}
