package org.esa.beam.opendap.utils;

import opendap.dap.DAP2Exception;
import opendap.dap.DDS;
import opendap.dap.parser.ParseException;
import org.esa.beam.opendap.DAPVariable;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class VariableExtractorTest {

    @Test
    public void testThatNoVariableCanBeExtractedFromEmptyDDS() {
        DDS dds = new DDS();

        final DAPVariable[] dapVariables = VariableExtractor.extractVariables(dds);

        assertEquals(0, dapVariables.length);
    }

    @Test
    public void testThatAVariableCanBeExtractedFromADDSWithOneVariable() throws DAP2Exception, ParseException {
        DDS dds = createDDSWithOneVariable();

        final DAPVariable[] dapVariables = VariableExtractor.extractVariables(dds);

        assertEquals(1, dapVariables.length);
        assertEquals("Chlorophyll", dapVariables[0].getName());
    }

    @Test
    public void testThatMultipleVariablesCanBeExtractedFromADDSWithMultipleVariables() throws DAP2Exception, ParseException {
        DDS dds = createDDSWithMultipleVariables();

        final DAPVariable[] dapVariables = VariableExtractor.extractVariables(dds);

        assertEquals(6, dapVariables.length);
        assertEquals("Chlorophyll", dapVariables[0].getName());
        assertEquals("Total_suspended_matter", dapVariables[1].getName());
        assertEquals("Yellow_substance", dapVariables[2].getName());
        assertEquals("l2_flags", dapVariables[3].getName());
        assertEquals("X", dapVariables[4].getName());
        assertEquals("Y", dapVariables[5].getName());
    }

    private DDS createDDSWithOneVariable() throws DAP2Exception, ParseException {
        DDS dds = new DDS();
        String ddsString =
                "Dataset {\n" +
                "    Grid {\n" +
                "        Array:\n" +
                "            Float32 Chlorophyll[Y = 849][X = 1121];\n" +
                "        Maps:\n" +
                "            Int32 Y[Y = 849];\n" +
                "            Int32 X[X = 1121];\n" +
                "    } Chlorophyll;\n" +
                "} MER_RR__2PNKOF20120113_101320_000001493110_00324_51631_6150.N1.nc;";
        dds.parse(new ByteArrayInputStream(ddsString.getBytes()));
        return dds;
    }

    private DDS createDDSWithMultipleVariables() throws DAP2Exception, ParseException {
        DDS dds = new DDS();
        String ddsString = "Dataset {\n" +
                "    Grid {\n" +
                "      Array:\n" +
                "        Float32 Chlorophyll[Y = 849][X = 1121];\n" +
                "      Maps:\n" +
                "        Int32 Y[Y = 849];\n" +
                "        Int32 X[X = 1121];\n" +
                "    } Chlorophyll;\n" +
                "    Grid {\n" +
                "      Array:\n" +
                "        Float32 Total_suspended_matter[Y = 849][X = 1121];\n" +
                "      Maps:\n" +
                "        Int32 Y[Y = 849];\n" +
                "        Int32 X[X = 1121];\n" +
                "    } Total_suspended_matter;\n" +
                "    Grid {\n" +
                "      Array:\n" +
                "        Float32 Yellow_substance[Y = 849][X = 1121];\n" +
                "      Maps:\n" +
                "        Int32 Y[Y = 849];\n" +
                "        Int32 X[X = 1121];\n" +
                "    } Yellow_substance;\n" +
                "    Grid {\n" +
                "      Array:\n" +
                "        Int32 l2_flags[Y = 849][X = 1121];\n" +
                "      Maps:\n" +
                "        Int32 Y[Y = 849];\n" +
                "        Int32 X[X = 1121];\n" +
                "    } l2_flags;\n" +
                "    Int32 X[X = 1121];\n" +
                "    Int32 Y[Y = 849];\n" +
                "} MER_RR__2PNKOF20120113_101320_000001493110_00324_51631_6150.N1.nc;";
        dds.parse(new ByteArrayInputStream(ddsString.getBytes()));
        return dds;
    }

}