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
    public void testThatAUserObjectWhichIsNotAOPeNDAP_LeafIsResolvedToFalse() {
        final Integer userObject = new Integer(4);
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isDapNode(notADapNode));
    }

    @Test
    public void testThatAOPeNDAP_LeafWhichHasNoDapServiceSetIsResolvedToFalse() {
        final CatalogTree.OPeNDAP_Leaf userObject = new CatalogTree.OPeNDAP_Leaf("name");
        userObject.setDapAccess(false);
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(false, CatalogTree.isDapNode(notADapNode));
    }

    @Test
    public void testThatAOPeNDAP_LeafWhichHasADapServiceSetIsResolvedToTrue_ServiceName_dap() {
        final CatalogTree.OPeNDAP_Leaf userObject = new CatalogTree.OPeNDAP_Leaf("name");
        userObject.setDapAccess(true);
        final DefaultMutableTreeNode notADapNode = new DefaultMutableTreeNode(userObject);
        assertEquals(true, CatalogTree.isDapNode(notADapNode));
    }
}
