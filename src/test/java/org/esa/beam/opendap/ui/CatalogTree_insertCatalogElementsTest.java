package org.esa.beam.opendap.ui;

import org.junit.Before;
import org.junit.Test;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class CatalogTree_insertCatalogElementsTest {

    private CatalogTree catalogTree;
    private DefaultMutableTreeNode root;

    @Before
    public void setUp() throws Exception {
        catalogTree = new CatalogTree(null);
        root = (DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot();
    }

    @Test
    public void testThatNumberOfChildrenHasChanged() throws URISyntaxException {
        //preparation
        final String parentCatalogUrl = "http://sonst.wo.hin/catalog.xml";
        final URI parentUri = new URI(parentCatalogUrl);
        final InputStream parentInputStream = new ByteArrayInputStream(getCatalogXMLAsString().getBytes());
        int oldChildrenNumber = root.getChildCount();

        //execution
        catalogTree.insertCatalogElements(parentInputStream, parentUri, root);

        //verification
        assertEquals(false, root.getChildCount() == oldChildrenNumber);
    }

    @Test
    public void testInsertionOfCatalogRef() throws URISyntaxException, IOException {
        final String parentCatalogUrl = "http://sonst.wo.hin/catalog.xml";
        final URI parentUri = new URI(parentCatalogUrl);
        final InputStream parentInputStream = new ByteArrayInputStream(getCatalogXMLAsString().getBytes());

        catalogTree.insertCatalogElements(parentInputStream, parentUri, root);

        assertEquals(true, root.getChildCount() == 1);
        DefaultMutableTreeNode catalogChild = (DefaultMutableTreeNode) root.getChildAt(0).getChildAt(0);
        CatalogTree.OPeNDAP_Leaf leaf = (CatalogTree.OPeNDAP_Leaf) catalogChild.getUserObject();
        assertEquals(true, leaf.isCatalogReference());
        assertEquals(true, leaf.getCatalogUri().equals("http://sonst.wo.hin/child/catalog.xml"));
    }

    @Test
    public void testInsertionOfDatasets() throws URISyntaxException, IOException {
        final String childCatalogURL = "http://sonst.wo.hin/child/catalog.xml";
        final URI childUri = new URI(childCatalogURL);
        final InputStream childInputStream = new ByteArrayInputStream(getChildCatalogXMLAsString().getBytes());

        catalogTree.insertCatalogElements(childInputStream, childUri, root);

        assertEquals(true, root.getChildCount() == 2);
        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) root.getChildAt(0);
        CatalogTree.OPeNDAP_Leaf firstLeaf = (CatalogTree.OPeNDAP_Leaf)firstChild.getUserObject();
        assertEquals(true, firstLeaf.getFileUri().equals("http://sonst.wo.hin/child/ProductName.N1.nc"));
        DefaultMutableTreeNode secondChild = (DefaultMutableTreeNode) root.getChildAt(1);
        CatalogTree.OPeNDAP_Leaf secondLeaf = (CatalogTree.OPeNDAP_Leaf)secondChild.getUserObject();
        assertEquals(true, secondLeaf.getFileUri().equals("http://sonst.wo.hin/child/OtherProductName.N1.nc"));
    }

    private String getCatalogXMLAsString() {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "    <thredds:catalog xmlns:fn=\"http://www.w3.org/2005/02/xpath-functions\"\n" +
                "                 xmlns:thredds=\"http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0\"\n" +
                "                 xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "                 xmlns:bes=\"http://xml.opendap.org/ns/bes/1.0#\">\n" +
                "   <thredds:service name=\"dap\" serviceType=\"OPeNDAP\" base=\"/opendap/hyrax\"/>\n" +
                "   <thredds:service name=\"file\" serviceType=\"HTTPServer\" base=\"/opendap/hyrax\"/>\n" +
                "        <thredds:dataset name=\"/data\" ID=\"/opendap/hyrax/data/\">\n" +
                "            <thredds:catalogRef name=\"child\" xlink:href=\"child/catalog.xml\" xlink:title=\"child\"\n" +
                "                          xlink:type=\"simple\"\n" +
                "                          ID=\"/opendap/hyrax/data/child/\"/>\n" +
                "        </thredds:dataset>\n" +
                "    </thredds:catalog>";
    }

    private String getChildCatalogXMLAsString() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<thredds:catalog xmlns:fn=\"http://www.w3.org/2005/02/xpath-functions\"\n" +
                "   xmlns:thredds=\"http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0\"\n" +
                "   xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "   xmlns:bes=\"http://xml.opendap.org/ns/bes/1.0#\">\n" +
                "   <thredds:service name=\"dap\" serviceType=\"OPeNDAP\" base=\"/opendap/hyrax\"/>\n" +
                "   <thredds:service name=\"file\" serviceType=\"HTTPServer\" base=\"/opendap/hyrax\"/>\n" +
                "   <thredds:dataset name=\"/data/child/MERIS/2012\" ID=\"/opendap/hyrax/data/child/MERIS/2012/\">\n" +
                "       <thredds:dataset name=\"ProductName.N1.nc\" ID=\"/opendap/hyrax/data/child/MERIS/2012/ProductName.N1.nc\">\n" +
                "           <thredds:dataSize units=\"bytes\">22851448</thredds:dataSize>\n" +
                "         <thredds:date type=\"modified\">2012-01-13T15:18:20</thredds:date>\n" +
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
    }

}
