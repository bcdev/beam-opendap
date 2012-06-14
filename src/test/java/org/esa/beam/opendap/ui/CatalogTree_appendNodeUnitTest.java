package org.esa.beam.opendap.ui;

import org.junit.*;
import thredds.catalog.InvAccessImpl;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.InvService;
import ucar.nc2.constants.FeatureType;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CatalogTree_appendNodeUnitTest {

    private ArrayList<InvDataset> datasets;
    private DefaultMutableTreeNode parentNode;

    @Before
    public void setUp() throws Exception {
        parentNode = new DefaultMutableTreeNode();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAppendADapNode_ServiceName_dap() throws URISyntaxException {
        // preparation
        final String serviceName = "dap";
        final InvDatasetImpl dapDataset = createADataset(serviceName);

        // execution
        CatalogTree.appendDataNodeToParent(parentNode, (DefaultTreeModel) new JTree().getModel(), dapDataset);

        // verification
        testThatChildIsADapNode(parentNode);
    }

    @Test
    public void testAppendADapNode_ServiceName_odap() throws URISyntaxException {
        // preparation
        final String serviceName = "odap";
        final InvDatasetImpl dapDataset = createADataset(serviceName);

        // execution
        CatalogTree.appendDataNodeToParent(parentNode, (DefaultTreeModel) new JTree().getModel(), dapDataset);

        // verification
        testThatChildIsADapNode(parentNode);
    }

    @Test
    public void testAppendAFileNode_ServiceName_file() throws URISyntaxException {
        // preparation
        final String serviceName = "file";
        final InvDatasetImpl dapDataset = createADataset(serviceName);

        // execution
        CatalogTree.appendDataNodeToParent(parentNode, (DefaultTreeModel) new JTree().getModel(), dapDataset);

        // verification
        testThatChildIsAFileNode(parentNode);
    }

    @Test
    public void testAppendAFileNode_ServiceName_http() throws URISyntaxException {
        // preparation
        final String serviceName = "http";
        final InvDatasetImpl dapDataset = createADataset(serviceName);

        // execution
        CatalogTree.appendDataNodeToParent(parentNode, (DefaultTreeModel) new JTree().getModel(), dapDataset);

        // verification
        testThatChildIsAFileNode(parentNode);
    }

    private void testThatChildIsADapNode(DefaultMutableTreeNode parentNode) {
        assertEquals(1, parentNode.getChildCount());
        assertEquals(true, parentNode.getChildAt(0).isLeaf());
        assertEquals(true, CatalogTree.isDapNode(parentNode.getChildAt(0)));
    }

    private void testThatChildIsAFileNode(DefaultMutableTreeNode parentNode) {
        assertEquals(1, parentNode.getChildCount());
        final DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(0);
        assertEquals(true, child.isLeaf());
        assertEquals(false, CatalogTree.isDapNode(child));
        assertEquals(true, child.getUserObject() instanceof CatalogTree.OPeNDAP_Leaf);
        final CatalogTree.OPeNDAP_Leaf leafObject = (CatalogTree.OPeNDAP_Leaf) child.getUserObject();
        assertEquals(true, leafObject.isFileAccess());
    }

    private InvDatasetImpl createADataset(String serviceName) throws URISyntaxException {
        final InvDatasetImpl dapDataset = new InvDatasetImpl(null, "first", FeatureType.NONE, "dap", "http://sonstwohin.bc");

        final InvCatalogImpl catalog = new InvCatalogImpl("catalogName", "1.0", new URI("http://x.y"));
        dapDataset.setCatalog(catalog);

        final InvService dapService = new InvService(serviceName, "unwichtig", "unwichtig", "unwichtig", "unwichtig");
        final InvAccessImpl invAccess = new InvAccessImpl(dapDataset, "http://y.z", dapService);
        dapDataset.addAccess(invAccess);

        dapDataset.finish();
        return dapDataset;
    }
}