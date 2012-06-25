package org.esa.beam.opendap.utils;

import opendap.dap.DAP2Exception;
import opendap.dap.DDS;
import opendap.dap.parser.ParseException;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class VariableCollectorTest {

    private VariableCollector variableCollector;

    @Before
    public void setUp() throws Exception {
        variableCollector = new VariableCollector();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCollectFromDDS_TwoVariables() throws DAP2Exception, ParseException {
        // preparation
        final String[] variableNames = {"Chlorophyll", "Total_suspended_matter"};
        final DDS dds = getDDS(variableNames);

        // execution
        variableCollector.collectFrom(dds);

        // verification
        assertExpectedVariableNamesInList(variableNames, variableCollector.getVariableNames());
    }

    @Test
    public void testCollectFromDDS_ThreeVariables() throws DAP2Exception, ParseException {
        //preparation
        final String[] variableNames = {"Baum", "Haus", "Eimer"};
        final DDS dds = getDDS(variableNames);

        //execution
        variableCollector.collectFrom(dds);

        //verification
        assertExpectedVariableNamesInList(variableNames, variableCollector.getVariableNames());
    }

    @Test
    public void testMultipleCollectionOfTheSameDDS() throws DAP2Exception, ParseException {
        //preparation
        final String[] variableNames = new String[]{"Baum"};
        final DDS dds = getDDS(variableNames);

        //execution
        variableCollector.collectFrom(dds);
        variableCollector.collectFrom(dds);

        //verification
        assertExpectedVariableNamesInList(variableNames, variableCollector.getVariableNames());
    }

    private DDS getDDS(String[] variableNames) throws ParseException, DAP2Exception {
        final DDS dds = new DDS();
        final String ddsString = getDDSString(variableNames);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(ddsString.getBytes());
        dds.parse(inputStream);
        return dds;
    }

    private String getDDSString(String[] variableNames) {
        final StringBuffer sb = new StringBuffer("Dataset {\n");
        for (String variableName : variableNames) {
            sb.append(getGridString(variableName));
        }
        sb.append("} MER_RR__2PNKOF20120113_101320_000001493110_00324_51631_6150.N1.nc;\n");
        return sb.toString();
    }

    private String getGridString(String variableName) {
        return "    Grid {\n" +
               "      Array:\n" +
               "        Float32 " + variableName + "[Y = 849][X = 1121];\n" +
               "      Maps:\n" +
               "        Int32 Y[Y = 849];\n" +
               "        Int32 X[X = 1121];\n" +
               "    } " + variableName + ";\n";
    }

    private void assertExpectedVariableNamesInList(String[] variableNames, Set<String> namesSet) {
        assertNotNull(namesSet);
        assertEquals(variableNames.length, namesSet.size());
        for (String variableName : variableNames) {
            assertEquals(variableName, true, namesSet.contains(variableName));
        }
    }
}
