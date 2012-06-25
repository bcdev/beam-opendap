package org.esa.beam.opendap.ui;

import org.junit.Before;
import org.junit.Test;
import thredds.catalog.InvAccessImpl;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.InvService;
import ucar.nc2.constants.FeatureType;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URI;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class OPeNDAP_Leaf_GetterTest {

    private DefaultMutableTreeNode child;

    @Before
    public void setUp() throws Exception {
        ArrayList<InvDataset> datasets = new ArrayList<InvDataset>();
        InvCatalogImpl catalog = new InvCatalogImpl("catalogName", "1.0", new URI("http://x.y"));
        InvDatasetImpl dapDataset = createDataset(catalog, "first", "dap");
        datasets.add(dapDataset);
        CatalogTree catalogTree = new CatalogTree(null);
        catalogTree.setNewRootDatasets(datasets);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
        child = (DefaultMutableTreeNode)root.getChildAt(0);
    }

    @Test
    public void testThatChildObjectIsAInstanceOfOPeNDAP_Leaf() {
        assertEquals(true, child.getUserObject() instanceof CatalogTree.OPeNDAP_Leaf);
    }

    @Test
    public void testGetDasURI() {
        assertEquals("http://first.das", ((CatalogTree.OPeNDAP_Leaf)child.getUserObject()).getDasUri());
    }

    @Test
    public void testGetDdsURI() {
        assertEquals("http://first.dds", ((CatalogTree.OPeNDAP_Leaf)child.getUserObject()).getDdsUri());
    }

    @Test
    public void testGetDdxURI() {
        assertEquals("http://first.ddx", ((CatalogTree.OPeNDAP_Leaf)child.getUserObject()).getDdxUri());
    }

    @Test
    public void testGetDodsURI() {
        assertEquals("http://first", ((CatalogTree.OPeNDAP_Leaf)child.getUserObject()).getDodsUri());
    }

    @Test
    public void testGetFileURI() {
        assertEquals("http://first", ((CatalogTree.OPeNDAP_Leaf)child.getUserObject()).getFileUri());
    }

    private InvDatasetImpl createDataset(InvCatalogImpl catalog, String datasetName, final String serviceName) {
        final InvDatasetImpl dataset =
                new InvDatasetImpl(null, datasetName, FeatureType.NONE, serviceName, "http://sonstwohin.bc");
        dataset.setCatalog(catalog);
        final InvService dapService = new InvService(serviceName, "unwichtig", "unwichtig", "unwichtig", "unwichtig");
        dataset.addAccess(new InvAccessImpl(dataset, "http://y.z", dapService));
        dataset.finish();
        return dataset;
    }

}