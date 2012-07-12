package org.esa.beam.opendap;

import opendap.dap.DArrayDimension;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OpendapLeafGetterTest {


    private OpendapLeaf opendapLeaf;

    @Before
    public void setUp() throws Exception {
        opendapLeaf = new OpendapLeaf("blah");
        opendapLeaf.setDapUri("http://domain/dap_node");
        opendapLeaf.setFileUri("http://domain/file_node");
    }

    @Test
    public void testGetDasURI() {
        assertEquals("http://domain/dap_node.das", opendapLeaf.getDasUri());
    }

    @Test
    public void testGetDdsURI() {
        assertEquals("http://domain/dap_node.dds", opendapLeaf.getDdsUri());
    }

    @Test
    public void testGetDdxURI() {
        assertEquals("http://domain/dap_node.ddx", opendapLeaf.getDdxUri());
    }

    @Test
    public void testGetDodsURI() {
        assertEquals("http://domain/dap_node", opendapLeaf.getDapUri());
    }

    @Test
    public void testGetFileURI() {
        assertEquals("http://domain/file_node", opendapLeaf.getFileUri());
    }

    @Test
    public void testGetVariables() {
        DAPVariable variable = new DAPVariable("vname", "vtype", "vdatatype", new DArrayDimension[0]);
        opendapLeaf.addDAPVariable(variable);
        assertEquals(1, opendapLeaf.getDAPVariables().length);
        assertEquals(variable, opendapLeaf.getDAPVariables()[0]);
    }

}