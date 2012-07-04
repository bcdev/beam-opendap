package org.esa.beam.opendap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import opendap.dap.DArrayDimension;
import org.junit.*;

public class DAPVariableTest {

    private String vType;
    private String vDataType;
    private String vName;
    private DArrayDimension validXDim;
    private DArrayDimension validYDim;
    private DArrayDimension[] vDimensions;
    private DAPVariable dapVariable;

    @Before
    public void setUp() throws Exception {
        vName = "validName";
        vType = "validType";
        vDataType = "validDataType";
        validXDim = new DArrayDimension(1121, "X");
        validYDim = new DArrayDimension(812, "Y");
        vDimensions = new DArrayDimension[]{validYDim, validXDim};
        dapVariable = new DAPVariable(vName, vType, vDataType, vDimensions);
    }

    @Test
    public void testGetInfoString() {
        //preparation
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
//        final DArrayDimension[] dimensions = dapVariable.getDimensions();
        pw.println("Name: " + dapVariable.getName());
//        pw.println("Type: " + dapVariable.getType());
//        pw.println("Dimensions: " + dimensions.length);
//        pw.println("Datatype: " + dapVariable.getDataType());
//        for (DArrayDimension dimension : dimensions) {
//            pw.println("dim(" + dimension.getName() + ") size: " + dimension.getSize());
//        }
        pw.close();

        //execution
        final String infotext = dapVariable.getInfotext();

        //verification
        assertEquals(sw.toString(), infotext);
    }

    @Test
    public void testIllegalArgumentExceptionIsThrownIfNameIsNotValid() {
        final String invalidName1 = null;
        final String invalidName2 = "";
        final String invalidName3 = "    ";

        try {
            new DAPVariable(invalidName1, vType, vDataType, vDimensions);
            fail("never come here");
        } catch (IllegalArgumentException e) {
            assertEquals("[name] is null", e.getMessage());
        }

        try {
            new DAPVariable(invalidName2, vType, vDataType, vDimensions);
            fail("never come here");
        } catch (IllegalArgumentException e) {
            assertEquals("[name] is an empty string", e.getMessage());
        }

        try {
            new DAPVariable(invalidName3, vType, vDataType, vDimensions);
            fail("never come here");
        } catch (IllegalArgumentException e) {
            assertEquals("'    ' is not a valid name", e.getMessage());
        }
    }

//    @Test
//    public void testIllegalArgumentExceptionIsThrownIfTypeIsNotValid() {
//        final String invalidType1 = null;
//        final String invalidType2 = "";
//        final String invalidType3 = "    ";
//
//        try {
//            new DAPVariable(vName, invalidType1, vDataType, vDimensions);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("[type] is null", e.getMessage());
//        }
//
//        try {
//            new DAPVariable(vName, invalidType2, vDataType, vDimensions);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("[type] is an empty string", e.getMessage());
//        }
//
//        try {
//            new DAPVariable(vName, invalidType3, vDataType, vDimensions);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("'    ' is not a valid type", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testIllegalArgumentExceptionIsThrownIfDataTypeIsNotValid() {
//        final String invalidDataType1 = null;
//        final String invalidDataType2 = "";
//        final String invalidDataType3 = "    ";
//
//        try {
//            new DAPVariable(vName, vType, invalidDataType1, vDimensions);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("[dataType] is null", e.getMessage());
//        }
//
//        try {
//            new DAPVariable(vName, vType, invalidDataType2, vDimensions);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("[dataType] is an empty string", e.getMessage());
//        }
//
//        try {
//            new DAPVariable(vName, vType, invalidDataType3, vDimensions);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("'    ' is not a valid dataType", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testIllegalArgumentExceptionIsThrownIfDimensionsIsNotValid() {
//        final DArrayDimension[] invalidDimensions1 = null;
//        final DArrayDimension[] invalidDimensions2 = new DArrayDimension[0];
//
//        try {
//            new DAPVariable(vName, vType, vDataType, invalidDimensions1);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("[dimensions] is null", e.getMessage());
//        }
//
//        try {
//            new DAPVariable(vName, vType, vDataType, invalidDimensions2);
//            fail("never come here");
//        } catch (IllegalArgumentException e) {
//            assertEquals("[dimensions] is an empty array", e.getMessage());
//        }
//
//    }
}