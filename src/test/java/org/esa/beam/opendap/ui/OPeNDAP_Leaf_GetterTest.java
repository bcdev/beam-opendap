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


    private CatalogTree.OPeNDAP_Leaf oPeNDAP_leaf;

    @Before
    public void setUp() throws Exception {
        oPeNDAP_leaf = new CatalogTree.OPeNDAP_Leaf("blah");
        oPeNDAP_leaf.setDapUri("http://domain/dap_node");
        oPeNDAP_leaf.setFileUri("http://domain/file_node");
    }

    @Test
    public void testGetDasURI() {
        assertEquals("http://domain/dap_node.das", oPeNDAP_leaf.getDasUri());
    }

    @Test
    public void testGetDdsURI() {
        assertEquals("http://domain/dap_node.dds", oPeNDAP_leaf.getDdsUri());
    }

    @Test
    public void testGetDdxURI() {
        assertEquals("http://domain/dap_node.ddx", oPeNDAP_leaf.getDdxUri());
    }

    @Test
    public void testGetDodsURI() {
        assertEquals("http://domain/dap_node", oPeNDAP_leaf.getDapUri());
    }

    @Test
    public void testGetFileURI() {
        assertEquals("http://domain/file_node", oPeNDAP_leaf.getFileUri());
    }
}