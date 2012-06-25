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
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class CatalogTree_insertCatalogElementsTest {

    private CatalogTree catalogTree;
    private DefaultMutableTreeNode root;

    @Before
    public void setUp() throws Exception {
        catalogTree = new CatalogTree(null);
        root = (DefaultMutableTreeNode)((JTree) catalogTree.getComponent()).getModel().getRoot();
    }

    @Test
    public void testInsertionOfCatalogElementsToEmptyCatalogTree() throws URISyntaxException, IOException {
        final String catalogURL = "http://sonst.wo.hin/catalog.xml";
        URI uri = new URI(catalogURL);
        final InputStream inputStream = new ByteArrayInputStream(getCatalogXMLAsString().getBytes());

        catalogTree.insertCatalogElements(inputStream, uri, root);

        assertEquals(true, root.getChildCount()>0);
    }

    @Test
    public void testInsertionOfCatalogElementsToNonEmptyCatalogTreeWithURL() throws IOException, URISyntaxException {
        //preparation
        final String parentCatalogURL = "http://opendap.hzg.de/opendap/data/catalog.xml";
        final String childCatalogURL = "http://opendap.hzg.de/opendap/data/cosyna/MERIS/2012/catalog.xml";
        final InputStream parentInputStream = new URL(parentCatalogURL).openConnection().getInputStream();
        final InputStream childInputStream = new URL(childCatalogURL).openConnection().getInputStream();
        final URI parentUri = new URI(parentCatalogURL);
        final URI childUri = new URI(childCatalogURL);

        //execution
        catalogTree.insertCatalogElements(parentInputStream, parentUri, root);
        final DefaultMutableTreeNode child = (DefaultMutableTreeNode)((DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot()).getChildAt(0);
        final int oldRootChildrenNumber = root.getChildCount();
        assertEquals("cosyna/", child.getUserObject().toString());
        assertEquals(1, child.getChildCount());
        final DefaultMutableTreeNode catalogNode = (DefaultMutableTreeNode)child.getChildAt(0);
        assertEquals(true, catalogNode.isLeaf());
        assertEquals(true, ((CatalogTree.OPeNDAP_Leaf) catalogNode.getUserObject()).isCatalogReference());

        catalogTree.insertCatalogElements(childInputStream, childUri, child);
        assertEquals(oldRootChildrenNumber, root.getChildCount());
        final DefaultMutableTreeNode sameChild = (DefaultMutableTreeNode)((DefaultMutableTreeNode) ((JTree) catalogTree.getComponent()).getModel().getRoot()).getChildAt(0);
        assertEquals(true, child.equals(sameChild));
        checkForReplacementOfCatalogNode(child, catalogNode);
    }

    @Test
    public void testInsertionOfCatalogelementsToNonEmptyCatalogTreeWithoutSecondURL() throws IOException, URISyntaxException {
        //preparation
        final String parentCatalogURL = "http://opendap.hzg.de/opendap/data/catalog.xml";
        final InputStream parentInputStream = new URL(parentCatalogURL).openConnection().getInputStream();
        final URI parentUri = new URI(parentCatalogURL);


        //execution
        catalogTree.insertCatalogElements(parentInputStream, parentUri, root);
        DefaultMutableTreeNode child = null;
        DefaultMutableTreeNode catalogChild = null;
        int oldRootChildrenNumber = root.getChildCount();
        for(int i=0; i<root.getChildCount(); i++){
            child = (DefaultMutableTreeNode)root.getChildAt(i);
            for (int j=0; j<child.getChildCount(); j++) {
                catalogChild = (DefaultMutableTreeNode)child.getChildAt(0);
                if(catalogChild.isLeaf() && ((CatalogTree.OPeNDAP_Leaf)catalogChild.getUserObject()).isCatalogReference()){
                    assertEquals(1, child.getChildCount());
                    final URI catalogUri = new URI(((CatalogTree.OPeNDAP_Leaf) catalogChild.getUserObject()).getCatalogUri());
                    final URL catalogUrl = catalogUri.toURL();
                    final InputStream catalogInputStream = catalogUrl.openConnection().getInputStream();
                    catalogTree.insertCatalogElements(catalogInputStream, catalogUri, catalogChild);
                    break;
                }
            }
        }
        assertEquals(oldRootChildrenNumber, root.getChildCount());
        checkForReplacementOfCatalogNode(child, catalogChild);
    }

    private void checkForReplacementOfCatalogNode(DefaultMutableTreeNode child, DefaultMutableTreeNode catalogNode) {
        for(int i=0; i<child.getChildCount(); i++){
            final DefaultMutableTreeNode catalogChild = (DefaultMutableTreeNode)child.getChildAt(i);
            assertEquals(false, catalogChild.equals(catalogNode));
        }
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
               "            <thredds:catalogRef name=\"cosyna\" xlink:href=\"cosyna/catalog.xml\" xlink:title=\"cosyna\"\n" +
               "                          xlink:type=\"simple\"\n" +
               "                          ID=\"/opendap/hyrax/data/cosyna/\"/>\n" +
               "            <thredds:catalogRef name=\"ff\" xlink:href=\"ff/catalog.xml\" xlink:title=\"ff\" xlink:type=\"simple\"\n" +
               "                          ID=\"/opendap/hyrax/data/ff/\"/>\n" +
               "            <thredds:catalogRef name=\"nc\" xlink:href=\"nc/catalog.xml\" xlink:title=\"nc\" xlink:type=\"simple\"\n" +
               "                          ID=\"/opendap/hyrax/data/nc/\"/>\n" +
               "        </thredds:dataset>\n" +
               "    </thredds:catalog>";
    }

}