package org.esa.beam.opendap;

import opendap.dap.DArrayDimension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DAPVariableTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInfoString() {
        final DAPVariable dapVariable = new DAPVariable("vname", "vtype", "vdatatype",
                new DArrayDimension[]{new DArrayDimension(1, "dim")});
//        assertEquals(
//                "Name: vname" +
//                "Type: vtype" +
//                "Dimensions: 1" +
//                "Datatype: vdatatype" +
//                "dim(dim) size = 1", dapVariable.getInfoString());
    }

} 