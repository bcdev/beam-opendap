package org.esa.beam.opendap.ui;

import com.jidesoft.tree.StyledTreeCellRenderer;
import org.junit.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import static org.junit.Assert.*;

public class CatalogTree_simpleDifferentTests {

    @Test
    public void testThatGetComponentGetsAWellDefinedJTreeComponent() {
        final CatalogTree catalogTree = new CatalogTree(null);
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

    @Test
    public void testThatCellRendererIsSet() {
        final JTree jTree = new JTree();
        final TreeCellRenderer renderer1 = jTree.getCellRenderer();
        assertNotNull(renderer1);
        assertEquals(true, renderer1 instanceof DefaultTreeCellRenderer);

        CatalogTree.addCellRenderer(jTree);

        final TreeCellRenderer renderer2 = jTree.getCellRenderer();
        assertNotNull(renderer2);
        assertEquals(true, renderer2 instanceof DefaultTreeCellRenderer);
        assertNotSame(renderer1, renderer2);
    }

    @Test
    public void testThatRendererRendersDifferentTypes() {
        final JTree jTree = new JTree();
        CatalogTree.addCellRenderer(jTree);
        final TreeCellRenderer dapCellRenderer = jTree.getCellRenderer();

        final CatalogTree.OPeNDAP_Leaf dap_oPeNDAP_leaf = new CatalogTree.OPeNDAP_Leaf("This is A dap Node", "uri");
        dap_oPeNDAP_leaf.setService("dap");
        final Object dapNode = new DefaultMutableTreeNode(dap_oPeNDAP_leaf);
        final Object noDapNode = new DefaultMutableTreeNode("noDap");

        final Component component = dapCellRenderer.getTreeCellRendererComponent(jTree, noDapNode, false, false, true, 0, false);

        assertEquals(true, component instanceof DefaultTreeCellRenderer);
        final DefaultTreeCellRenderer tcr1 = (DefaultTreeCellRenderer) component;
        assertEquals("noDap", tcr1.getText());
        assertEquals(true, tcr1.getIcon() instanceof ImageIcon);
        final ImageIcon icon1 = (ImageIcon) tcr1.getIcon();
        // todo change the expected icon to a realistic icon
        assertEquals("/FRsProduct16.png", icon1.getDescription().substring(icon1.getDescription().length()-17));

        final Color foreground1 = tcr1.getForeground();
        final Color background1 = tcr1.getBackground();
        final Font font1 = tcr1.getFont();

        final Component component2 = dapCellRenderer.getTreeCellRendererComponent(jTree, dapNode, false, false, true, 0, false);

        assertSame(component, component2);

        assertEquals(true, component2 instanceof DefaultTreeCellRenderer);
        final DefaultTreeCellRenderer tcr2 = (DefaultTreeCellRenderer) component2;
        assertEquals("This is A dap Node", tcr2.getText());
        assertEquals(true, tcr2.getIcon() instanceof ImageIcon);
        final ImageIcon icon2 = (ImageIcon) tcr2.getIcon();
        // todo change the expected icon to a realistic icon
        assertEquals("/DRsProduct16.png", icon2.getDescription().substring(icon2.getDescription().length()-17));

        assertEquals(foreground1, tcr2.getForeground());
        assertEquals(background1, tcr2.getBackground());
        assertEquals(font1, tcr2.getFont());
    }
}
