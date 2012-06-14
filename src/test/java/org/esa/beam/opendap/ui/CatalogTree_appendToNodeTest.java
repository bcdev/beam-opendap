package org.esa.beam.opendap.ui;

import org.hsqldb.lib.StringInputStream;
import org.junit.*;
import thredds.catalog.*;
import ucar.nc2.constants.FeatureType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CatalogTree_appendToNodeTest {

    private ArrayList<InvDataset> datasets;

    @Before
    public void setUp() throws Exception {
        datasets = new ArrayList<InvDataset>();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAppendADapNode() throws URISyntaxException {
        // preparation
        final InvCatalogImpl catalog = new InvCatalogImpl("catalogName", "1.0", new URI("http://x.y"));
        final InvDatasetImpl dapDataset = createDapTreeNode(catalog, "first");
        datasets.add(dapDataset);
        final DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode();

        // execution
        CatalogTree.appendToNode(new JTree(), datasets, parentNode);

        // verification
        assertEquals(1, parentNode.getChildCount());
        assertEquals(true, parentNode.getChildAt(0).isLeaf());
        assertEquals(true, CatalogTree.isDapNode(parentNode.getChildAt(0)));
    }

    @Test
    public void testAppendThreeDapNodes() throws URISyntaxException {
        //preparation
        final InvCatalogImpl catalog = new InvCatalogImpl("catalogName", "1.0", new URI("http://x.y"));
        datasets.add(createDapTreeNode(catalog, "Name_1"));
        final InvDatasetImpl dapTreeNode = createDapTreeNode(catalog, "Name_2");

        datasets.add(dapTreeNode);
        datasets.add(createDapTreeNode(catalog, "Name_3"));

        final DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode();

        //execution
        CatalogTree.appendToNode(new JTree(), datasets, parentNode);

        //verification
        assertEquals(3, parentNode.getChildCount());
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            final DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) parentNode.getChildAt(i);
            final String indexMessage = "Index = " + i;
            assertEquals(indexMessage, true, childAt.isLeaf());
            assertEquals(indexMessage, true, CatalogTree.isDapNode(childAt));
            assertEquals(indexMessage, "Name_" + (i + 1), childAt.getUserObject().toString());
        }
    }

    private InvDatasetImpl createDapTreeNode(InvCatalogImpl catalog, String nodeName) {
        final InvDatasetImpl dapDataset = new InvDatasetImpl(null, nodeName, FeatureType.NONE, "dap", "http://sonstwohin.bc");
        dapDataset.setCatalog(catalog);
        final InvService dapService = new InvService("dap", "unwichtig", "unwichtig", "unwichtig", "unwichtig");
        dapDataset.addAccess(new InvAccessImpl(dapDataset, "http://y.z", dapService));
        dapDataset.finish();
        return dapDataset;
    }

}