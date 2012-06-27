package org.esa.beam.opendap.ui;

import org.junit.*;
import thredds.catalog.*;
import ucar.nc2.constants.FeatureType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

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

    @Test
    public void testAppendADapNode() throws URISyntaxException {
        // preparation
        datasets.add(createDataset(catalog, "first", "dap"));

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
        datasets.add(createDataset(catalog, "Name_1", "dap"));
        datasets.add(createDataset(catalog, "Name_2", "dap"));
        datasets.add(createDataset(catalog, "Name_3", "dap"));

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
        datasets.add(createDataset(catalog, "fileName", "file"));

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
        datasets.add(createCatalogRefDataset());

        //execution
        CatalogTree.appendToNode(new JTree(), datasets, parentNode);

        //verification
        assertEquals(1, parentNode.getChildCount());
        assertEquals(1, parentNode.getChildAt(0).getChildCount());
        assertEquals(true, parentNode.getChildAt(0).getChildAt(0).isLeaf());
        assertEquals(false, CatalogTree.isDapNode(parentNode.getChildAt(0)));
        assertEquals(false, CatalogTree.isCatalogReferenceNode(parentNode.getChildAt(0)));
        assertEquals(false, CatalogTree.isDapNode(parentNode.getChildAt(0).getChildAt(0)));
        assertEquals(true, CatalogTree.isCatalogReferenceNode(parentNode.getChildAt(0).getChildAt(0)));
    }

    @Test
    public void testAppendingVariousDatasets() {
        //preparation
        datasets.add(createDataset(catalog, "dapName", "dap"));
        datasets.add(createDataset(catalog,"fileName", "file"));
        datasets.add(createCatalogRefDataset());

        //execution
        CatalogTree.appendToNode(new JTree(), datasets, parentNode);

        //verification
        assertEquals(3, parentNode.getChildCount());
        assertEquals(true, CatalogTree.isDapNode(parentNode.getChildAt(0)));
        assertEquals(false, CatalogTree.isDapNode(parentNode.getChildAt(1)));
        assertEquals(false, CatalogTree.isCatalogReferenceNode(parentNode.getChildAt(1)));
        assertEquals(true, CatalogTree.isCatalogReferenceNode(parentNode.getChildAt(2).getChildAt(0)));
    }

    private InvDatasetImpl createDataset(InvCatalogImpl catalog, String datasetName, final String serviceName) {
        final InvDatasetImpl dapDataset = new InvDatasetImpl(null, datasetName, FeatureType.NONE, serviceName, "http://wherever.you.want.bc");
        dapDataset.setCatalog(catalog);
        final InvService dapService = new InvService(serviceName, "nonrelevant", "nonrelevant", "nonrelevant", "nonrelevant");
        dapDataset.addAccess(new InvAccessImpl(dapDataset, "http://y.z", dapService));
        dapDataset.finish();
        return dapDataset;
    }

    private InvCatalogRef createCatalogRefDataset() {
        final InvCatalogRef catalogRef = new InvCatalogRef(null, "catalogName", "nonrelevant");
        catalogRef.setCatalog(catalog);
        return catalogRef;
    }

}