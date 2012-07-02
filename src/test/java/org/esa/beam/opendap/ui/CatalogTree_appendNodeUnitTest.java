package org.esa.beam.opendap.ui;

import org.junit.*;
import thredds.catalog.InvAccessImpl;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.InvService;
import ucar.nc2.constants.FeatureType;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class CatalogTree_appendNodeUnitTest {

    private DefaultMutableTreeNode parentNode;

    @Before
    public void setUp() throws Exception {
        parentNode = new DefaultMutableTreeNode();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAppendA_OPeNDAP_Node() throws URISyntaxException {
        // preparation
        final String serviceType = "OPENDAP";
        final InvDatasetImpl dapDataset = createADataset(new String[]{serviceType});

        // execution
        CatalogTree.appendDataNodeToParent(parentNode, getDefaultTreeModel(), dapDataset);

        // verification
        testThatChildIsADapNode(parentNode);
    }

    @Test
    public void testAppendA_FILE_Node() throws URISyntaxException {
        // preparation
        final String serviceName = "FILE";
        final InvDatasetImpl dapDataset = createADataset(new String[]{serviceName});

        // execution
        CatalogTree.appendDataNodeToParent(parentNode, getDefaultTreeModel(), dapDataset);

        // verification
        testThatChildIsAFileNodeAndMaybeADapNodeToo(parentNode, true);
    }

    @Test
    public void testAppendANodeWhichHasDapAccessAndAlsoFileAccess_ServiceNames_odapAndHttp() throws URISyntaxException {
        // preparation
        final String dapServiceName = "OPENDAP";
        final String fileServiceName = "FILE";
        final InvDatasetImpl dapDataset = createADataset(new String[]{fileServiceName, dapServiceName});

        // execution
        CatalogTree.appendDataNodeToParent(parentNode, getDefaultTreeModel(), dapDataset);

        // verification
        testThatChildIsAFileNodeAndMaybeADapNodeToo(parentNode, false);
    }

    @Test
    public void testAppendCatalogNodeToParent() throws URISyntaxException {
        //preparation
        final URI whatever = null;
        final InvCatalogRef catalogReference = new InvCatalogRef(null, "catalogRefName", "http://a.b");
        catalogReference.setCatalog(new InvCatalogImpl("whatever", "1.0", whatever));

        //execution
        CatalogTree.appendCatalogNodeToParent(parentNode, getDefaultTreeModel(), catalogReference);

        //verification
        assertEquals(1, parentNode.getChildCount());
        assertEquals(1, parentNode.getChildAt(0).getChildCount());

        final DefaultMutableTreeNode child1 = (DefaultMutableTreeNode) parentNode.getChildAt(0);
        assertEquals(true, child1.getUserObject() instanceof String);
        assertEquals("catalogRefName/", child1.getUserObject());

        final DefaultMutableTreeNode child2 = (DefaultMutableTreeNode) parentNode.getChildAt(0).getChildAt(0);
        assertEquals(true, child2.getUserObject() instanceof CatalogTree.OPeNDAP_Leaf);
        final CatalogTree.OPeNDAP_Leaf oPeNDAP_leaf = (CatalogTree.OPeNDAP_Leaf) child2.getUserObject();
        assertEquals("http://a.b", oPeNDAP_leaf.getCatalogUri());
        assertEquals(true, oPeNDAP_leaf.isCatalogReference());
        assertEquals(false, oPeNDAP_leaf.isFileAccess());
        assertEquals(false, oPeNDAP_leaf.isDapAccess());
    }

    private void testThatChildIsADapNode(DefaultMutableTreeNode parentNode) {
        assertEquals(1, parentNode.getChildCount());
        assertEquals(true, parentNode.getChildAt(0).isLeaf());
        assertEquals(true, CatalogTree.isDapNode(parentNode.getChildAt(0)));
    }

    private void testThatChildIsAFileNodeAndMaybeADapNodeToo(DefaultMutableTreeNode parentNode, boolean exclusivAFileNode) {
        assertEquals(1, parentNode.getChildCount());
        final DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(0);
        assertEquals(true, child.isLeaf());
        assertEquals(true, child.getUserObject() instanceof CatalogTree.OPeNDAP_Leaf);
        final CatalogTree.OPeNDAP_Leaf leafObject = (CatalogTree.OPeNDAP_Leaf) child.getUserObject();
        assertEquals(true, leafObject.isFileAccess());
        if (exclusivAFileNode) {
            assertEquals(false, leafObject.isDapAccess());
        } else {
            assertEquals(true, leafObject.isDapAccess());
        }
        assertEquals(false, leafObject.isCatalogReference());
    }

    private InvDatasetImpl createADataset(String[] serviceTypeNames) throws URISyntaxException {
        final InvDatasetImpl dapDataset = new InvDatasetImpl(null, "datasetName", FeatureType.NONE, serviceTypeNames[0], "http://wherever.you.want.bc");

        final InvCatalogImpl catalog = new InvCatalogImpl("catalogName", "1.0", new URI("http://x.y"));
        dapDataset.setCatalog(catalog);

        for (String serviceName : serviceTypeNames) {
            final InvService dapService = new InvService(serviceName, serviceName, "nonrelevant", "nonrelevant", "nonrelevant");
            final InvAccessImpl invAccess = new InvAccessImpl(dapDataset, "http://y.z", dapService);
            dapDataset.addAccess(invAccess);
        }

        dapDataset.finish();
        return dapDataset;
    }

    private DefaultTreeModel getDefaultTreeModel() {
        return (DefaultTreeModel) new JTree().getModel();
    }
}