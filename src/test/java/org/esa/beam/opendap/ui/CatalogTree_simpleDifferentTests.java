package org.esa.beam.opendap.ui;

import org.junit.*;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.Component;

import static org.junit.Assert.*;

public class CatalogTree_simpleDifferentTests {

    @Test
    public void testThatGetComponentGetsAWellDefinedJTreeComponent() {
        final CatalogTree catalogTree = new CatalogTree();
        final Component component = catalogTree.getComponent();

        assertNotNull(component);
        assertEquals(true, component instanceof JTree);
        final JTree tree = (JTree) component;
        assertEquals(false, tree.isRootVisible());
        assertNotNull(tree.getModel());
        assertEquals(true, tree.getModel() instanceof DefaultTreeModel);

        final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        assertNotNull(model.getRoot());
        assertEquals(true, model.getRoot() instanceof DefaultMutableTreeNode);
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        assertNotNull(rootNode.getUserObject());
        assertEquals(true, rootNode.getUserObject() instanceof String);
        assertEquals("root", rootNode.getUserObject().toString());
    }

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
