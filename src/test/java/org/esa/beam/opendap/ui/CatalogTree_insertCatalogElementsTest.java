package org.esa.beam.opendap.ui;

import org.junit.*;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class CatalogTree_insertCatalogElementsTest {

    private CatalogTree catalogTree;
    private DefaultMutableTreeNode parentNode;

    @Before
    public void setUp() throws Exception {
        final CatalogTree.ResponseDispatcher responseDispatcherNotNeededForThisTestCases = null;
        catalogTree = new CatalogTree(responseDispatcherNotNeededForThisTestCases);
        parentNode = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
    }

    @Test
    public void thestThatParentNodeHasNoChildrenAfterInitialisation() {
        assertEquals(0, parentNode.getChildCount());
    }

    @Test
    public void testThatOneCatalogReferenceNodeHasBeenAdded() throws URISyntaxException {
        //preparation
        final URI catalogBaseUri = new URI("http://sonst.wo.hin/catalog.xml");
        final InputStream catalogIS = getThreddsCatalogInputStreamWithOneChildCatalogReference();

        //execution
        catalogTree.insertCatalogElements(catalogIS, catalogBaseUri, parentNode);

        //verification
        assertEquals(1, parentNode.getChildCount());
        final DefaultMutableTreeNode catalogNameNode = (DefaultMutableTreeNode) parentNode.getChildAt(0);
        assertEquals("CatalogName/", catalogNameNode.getUserObject());
        final TreeNode catalogReferenceNode = catalogNameNode.getChildAt(0);
        assertEquals(true, CatalogTree.isCatalogReferenceNode(catalogReferenceNode));
    }

    @Test
    public void testThatTwoDapDatasetsHaveBeenAdded() throws URISyntaxException, IOException {
        //preparation
        final URI catalogBaseUri = new URI("http://sonst.wo.hin/child/catalog.xml");
        final InputStream catalogIS = getThreddsCatalogInputStreamWithTwoChildDapDatasets();

        //execution
        catalogTree.insertCatalogElements(catalogIS, catalogBaseUri, parentNode);

        //verification
        assertEquals(2, parentNode.getChildCount());
        assertEquals(true , CatalogTree.isDapNode(parentNode.getChildAt(0)));
        assertEquals(true , CatalogTree.isDapNode(parentNode.getChildAt(1)));
        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) parentNode.getChildAt(0);
        CatalogTree.OPeNDAP_Leaf firstLeaf = (CatalogTree.OPeNDAP_Leaf) firstChild.getUserObject();
        assertEquals("http://sonst.wo.hin/child/ProductName.N1.nc", firstLeaf.getFileUri());
        DefaultMutableTreeNode secondChild = (DefaultMutableTreeNode) parentNode.getChildAt(1);
        CatalogTree.OPeNDAP_Leaf secondLeaf = (CatalogTree.OPeNDAP_Leaf) secondChild.getUserObject();
        assertEquals("http://sonst.wo.hin/child/OtherProductName.N1.nc", secondLeaf.getFileUri());
    }

    private InputStream getThreddsCatalogInputStreamWithOneChildCatalogReference() {

        final String threddsCatalogWithOneChildCatalogReference =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "    <thredds:catalog xmlns:fn=\"http://www.w3.org/2005/02/xpath-functions\"\n" +
                    "                 xmlns:thredds=\"http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0\"\n" +
                    "                 xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                    "                 xmlns:bes=\"http://xml.opendap.org/ns/bes/1.0#\">\n" +
                    "    <thredds:service name=\"dap\" serviceType=\"OPeNDAP\" base=\"/opendap/hyrax\"/>\n" +
                    "    <thredds:service name=\"file\" serviceType=\"HTTPServer\" base=\"/opendap/hyrax\"/>\n" +
                    "        <thredds:dataset name=\"/data\" ID=\"/opendap/hyrax/data/\">\n" +
                    "            <thredds:catalogRef name=\"CatalogName\" xlink:href=\"CatalogName/catalog.xml\" xlink:title=\"CatalogName\"\n" +
                    "                          xlink:type=\"simple\"\n" +
                    "                          ID=\"/opendap/hyrax/data/child/\"/>\n" +
                    "        </thredds:dataset>\n" +
                    "    </thredds:catalog>";

        return new ByteArrayInputStream(threddsCatalogWithOneChildCatalogReference.getBytes());
    }

    private InputStream getThreddsCatalogInputStreamWithTwoChildDapDatasets() {

        final String threddsCatalogStringWithTwoChildDapDatasets =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<thredds:catalog xmlns:fn=\"http://www.w3.org/2005/02/xpath-functions\"\n" +
                    "   xmlns:thredds=\"http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0\"\n" +
                    "   xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                    "   xmlns:bes=\"http://xml.opendap.org/ns/bes/1.0#\">\n" +
                    "   <thredds:service name=\"dap\" serviceType=\"OPeNDAP\" base=\"/opendap/hyrax\"/>\n" +
                    "   <thredds:service name=\"file\" serviceType=\"HTTPServer\" base=\"/opendap/hyrax\"/>\n" +
                    "   <thredds:dataset name=\"/data/child/MERIS/2012\" ID=\"/opendap/hyrax/data/child/MERIS/2012/\">\n" +
                    "       <thredds:dataset name=\"ProductName.N1.nc\" ID=\"/opendap/hyrax/data/child/MERIS/2012/ProductName.N1.nc\">\n" +
                    "           <thredds:dataSize units=\"bytes\">22851448</thredds:dataSize>\n" +
                    "           <thredds:date type=\"modified\">2012-01-13T15:18:20</thredds:date>\n" +
                    "           <thredds:access serviceName=\"dap\" urlPath=\"/data/child/ProductName.N1.nc\"/>\n" +
                    "           <thredds:access serviceName=\"file\" urlPath=\"/data/child/MERIS/2012/ProductName.N1.nc\"/>\n" +
                    "       </thredds:dataset>\n" +
                    "       <thredds:dataset name=\"OtherProductName.N1.nc\" ID=\"/opendap/hyrax/data/child/OtherProductName.N1.nc\">\n" +
                    "           <thredds:dataSize units=\"bytes\">20268280</thredds:dataSize>\n" +
                    "           <thredds:date type=\"modified\">2012-01-13T17:03:54</thredds:date>\n" +
                    "           <thredds:access serviceName=\"dap\" urlPath=\"/data/child/OtherProductName.N1.nc\"/>\n" +
                    "           <thredds:access serviceName=\"file\" urlPath=\"/data/child/MERIS/2012/OtherProductName.N1.nc\"/>\n" +
                    "       </thredds:dataset>\n" +
                    "   </thredds:dataset>\n" +
                    "</thredds:catalog>";

        return new ByteArrayInputStream(threddsCatalogStringWithTwoChildDapDatasets.getBytes());
    }
}
