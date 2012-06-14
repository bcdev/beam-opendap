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
    private InvCatalogImpl catalog;
    private DefaultMutableTreeNode parentNode;

    @Before
    public void setUp() throws Exception {
        datasets = new ArrayList<InvDataset>();
        catalog = new InvCatalogImpl("catalogName", "1.0", new URI("http://x.y"));
        parentNode = new DefaultMutableTreeNode();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAppendADapNode() throws URISyntaxException {
        // preparation
        final InvDatasetImpl dapDataset = createDapTreeNode(catalog, "first");
        datasets.add(dapDataset);

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
        datasets.add(createDapTreeNode(catalog, "Name_1"));
        final InvDatasetImpl dapTreeNode = createDapTreeNode(catalog, "Name_2");

        datasets.add(dapTreeNode);
        datasets.add(createDapTreeNode(catalog, "Name_3"));

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

    @Test
    public void testAppendAFileNode() throws URISyntaxException {
        //preparation
        final InvDatasetImpl fileDataset = new InvDatasetImpl(null, "fileName", FeatureType.NONE, "file", "http://sonstwohin.bc");
        fileDataset.setCatalog(catalog);
        final InvService dapService = new InvService("file", "unwichtig", "unwichtig", "unwichtig", "unwichtig");
        fileDataset.addAccess(new InvAccessImpl(fileDataset, "http://y.z", dapService));
        fileDataset.finish();
        datasets.add(fileDataset);

        //execution
        CatalogTree.appendToNode(new JTree(), datasets, parentNode);

        //verification
        assertEquals(1, parentNode.getChildCount());
        assertEquals(true, parentNode.getChildAt(0).isLeaf());
        assertEquals(false, CatalogTree.isDapNode(parentNode.getChildAt(0)));
    }

    @Test
    public void testAppendACatalogNode() throws URISyntaxException {
        //preparation
        final InvCatalogRef catalogRef = new InvCatalogRef(null, "catalogName", "unwichtig");
        catalogRef.setCatalog(catalog);
        datasets.add(catalogRef);

        //execution
        CatalogTree.appendToNode(new JTree(), datasets, parentNode);

        //verification
        assertEquals(1, parentNode.getChildCount());
        assertEquals(1, parentNode.getChildAt(0).getChildCount());
        assertEquals(true, parentNode.getChildAt(0).getChildAt(0).isLeaf());
        assertEquals(false, CatalogTree.isDapNode(parentNode.getChildAt(0)));
        assertEquals(false, CatalogTree.isDapNode(parentNode.getChildAt(0).getChildAt(0)));
        assertEquals(true, CatalogTree.isCatalogReferenceNode(parentNode.getChildAt(0).getChildAt(0)));
    }

    @Test
    public void testAppendingVariousDatasets() {
        //preparation
        final InvDatasetImpl dapDataset = createDapTreeNode(catalog, "dapName");
        datasets.add(dapDataset);

        final InvDatasetImpl fileDataset = new InvDatasetImpl(null, "fileName", FeatureType.NONE, "file", "http://sonstwohin.bc");
        fileDataset.setCatalog(catalog);
        final InvService dapService = new InvService("file", "unwichtig", "unwichtig", "unwichtig", "unwichtig");
        fileDataset.addAccess(new InvAccessImpl(fileDataset, "http://y.z", dapService));
        fileDataset.finish();
        datasets.add(fileDataset);

        final InvCatalogRef catalogRef = new InvCatalogRef(null, "catalogName", "unwichtig");
        catalogRef.setCatalog(catalog);
        datasets.add(catalogRef);

        //execution
        CatalogTree.appendToNode(new JTree(), datasets, parentNode);

        //verification
        assertEquals(3, parentNode.getChildCount());
        assertEquals(true, CatalogTree.isDapNode(parentNode.getChildAt(0)));
        assertEquals(false, CatalogTree.isDapNode(parentNode.getChildAt(1)));
        assertEquals(true, CatalogTree.isCatalogReferenceNode(parentNode.getChildAt(2).getChildAt(0)));
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