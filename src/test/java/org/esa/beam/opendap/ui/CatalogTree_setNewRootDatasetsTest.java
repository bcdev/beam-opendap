package org.esa.beam.opendap.ui;

import org.junit.*;
import thredds.catalog.*;
import ucar.nc2.constants.FeatureType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URI;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CatalogTree_setNewRootDatasetsTest {

    private ArrayList<InvDataset> datasets;
    private InvCatalogImpl catalog;
    private InvDatasetImpl dapDataset;
    private CatalogTree catalogTree;

    @Before
    public void setUp() throws Exception {
        datasets = new ArrayList<InvDataset>();
        catalog = new InvCatalogImpl("catalogName", "1.0", new URI("http://x.y"));
        dapDataset = createDataset(catalog, "first", "dap");
        datasets.add(dapDataset);
        catalogTree = new CatalogTree(null);
    }

    @Test
    public void testAddingADapDataset() {
        //execution
        catalogTree.setNewRootDatasets(datasets);

        //verification
        assertEquals(true, ((JTree) catalogTree.getComponent()).getModel().getRoot() instanceof DefaultMutableTreeNode);
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
        assertEquals(1, root.getChildCount());
        assertEquals(true, CatalogTree.isDapNode(root.getChildAt(0)));
        assertEquals("first", ((DefaultMutableTreeNode) root.getChildAt(0)).getUserObject().toString());
    }

    @Test
    public void testAddingTwoDifferentDatasets() {
        //preparation
        final InvDatasetImpl fileDataset = createDataset(catalog, "second", "file");
        datasets.add(fileDataset);

        //execution
        catalogTree.setNewRootDatasets(datasets);

        //verification
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
        assertEquals(2, root.getChildCount());
        assertEquals(true, CatalogTree.isDapNode(root.getChildAt(0)));
        assertEquals(false, CatalogTree.isDapNode(root.getChildAt(1)));
        assertEquals(true, ((DefaultMutableTreeNode)root.getChildAt(1)).getUserObject() instanceof CatalogTree.OPeNDAP_Leaf);
        final CatalogTree.OPeNDAP_Leaf leaf = (CatalogTree.OPeNDAP_Leaf) ((DefaultMutableTreeNode) root.getChildAt(1)).getUserObject();
        assertEquals(true, leaf.isFileAccess());
    }

    @Test
    public void testWhetherRootNodeHasBeenExchanged() {
        //preparation
        final InvDatasetImpl fileDataset = createDataset(catalog, "second", "file");
        final ArrayList<InvDataset> otherDatasets = new ArrayList<InvDataset>();
        otherDatasets.add(fileDataset);

        //execution
        catalogTree.setNewRootDatasets(datasets);

        //verification
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
        catalogTree.setNewRootDatasets(otherDatasets);
        final DefaultMutableTreeNode otherRoot = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
        assertEquals(false, root.equals(otherRoot));
    }

    @Test
    public void testThatPreviousDatasetsHaveBeenRemoved() {
        //preparation
        final InvDatasetImpl fileDataset = createDataset(catalog, "second", "file");
        final ArrayList<InvDataset> otherDatasets = new ArrayList<InvDataset>();
        otherDatasets.add(fileDataset);

        //execution
        catalogTree.setNewRootDatasets(datasets);

        //verification
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
        final DefaultMutableTreeNode child = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getChild(root, 0);

        catalogTree.setNewRootDatasets(otherDatasets);
        final DefaultMutableTreeNode otherRoot = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
        for(int i=0; i<otherRoot.getChildCount(); i++){
            DefaultMutableTreeNode otherChild = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getChild(otherRoot, i);
            assertEquals(true, ((CatalogTree.OPeNDAP_Leaf)otherChild.getUserObject()).isFileAccess());
            assertEquals(false, child.equals(otherChild));
        }
    }

    private InvDatasetImpl createDataset(InvCatalogImpl catalog, String datasetName, final String serviceName) {
        final InvDatasetImpl dapDataset =
                new InvDatasetImpl(null, datasetName, FeatureType.NONE, serviceName, "http://sonstwohin.bc");
        dapDataset.setCatalog(catalog);
        final InvService dapService = new InvService(serviceName, "unwichtig", "unwichtig", "unwichtig", "unwichtig");
        dapDataset.addAccess(new InvAccessImpl(dapDataset, "http://y.z", dapService));
        dapDataset.finish();
        return dapDataset;
    }

} 