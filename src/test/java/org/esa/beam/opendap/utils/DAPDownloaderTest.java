package org.esa.beam.opendap.utils;

import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dods.DODSNetcdfFile;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DAPDownloaderTest {

    static final File TESTDATA_DIR = new File("target/dap_download_test");

    @Before
    public void setUp() throws Exception {
        TESTDATA_DIR.mkdirs();
        if (!TESTDATA_DIR.isDirectory()) {
            fail("Can't create test I/O directory: " + TESTDATA_DIR);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!FileUtils.deleteTree(TESTDATA_DIR)) {
            System.out.println("Warning: failed to completely delete test I/O directory:" + TESTDATA_DIR);
        }
    }

    @Test
    public void testDownloadFile() throws Exception {
        DAPDownloader dapDownloader = new DAPDownloader(new ArrayList<String>(), new ArrayList<String>());
        String fileName = "fileToTextDownload.txt";
        assertFalse(getTestFile(fileName).exists());
        assertEquals(0, dapDownloader.downloadedFiles.size());

        URL resource = getClass().getResource(fileName);
        dapDownloader.downloadFile(TESTDATA_DIR, resource.toString());

        assertEquals(1, dapDownloader.downloadedFiles.size());
        assertEquals(fileName, ((File) dapDownloader.downloadedFiles.toArray()[0]).getName());
        assertTrue(getTestFile(fileName).exists());
    }

    @Test
    public void testGetVariableNames() throws Exception {
        List<String> variableNames = DAPDownloader.getVariableNames("iop_a_total_443[0:1:717][0:1:308],iop_a_ys_443[0:1:717][0:1:308]");

        String[] expected = {"iop_a_total_443", "iop_a_ys_443"};
        assertArrayEquals(expected, variableNames.toArray(new String[variableNames.size()]));

        variableNames = DAPDownloader.getVariableNames("");
        assertNull(variableNames);

        variableNames = DAPDownloader.getVariableNames(null);
        assertNull(variableNames);

        variableNames = DAPDownloader.getVariableNames("someUnconstrainedVariable");
        expected = new String[]{"someUnconstrainedVariable"};
        assertArrayEquals(expected, variableNames.toArray(new String[variableNames.size()]));

        variableNames = DAPDownloader.getVariableNames("someUnconstrainedVariable,someConstrainedVariable[0:1:717][0:1:308]");
        expected = new String[]{"someUnconstrainedVariable", "someConstrainedVariable"};
        assertArrayEquals(expected, variableNames.toArray(new String[variableNames.size()]));
    }

    @Test
    public void testFilterVariables() throws Exception {
        final URL resource = getClass().getResource("test.nc");
        final NetcdfFile netcdfFile = NetcdfFile.open(resource.toString());
        final List<Variable> variables = netcdfFile.getVariables();
        final List<String> variableNames = new ArrayList<String>();
        for (Variable variable : variables) {
            variableNames.add(variable.getName());
        }
        String constraintExpression = null;
        List<String> filteredVariables = DAPDownloader.filterVariables(variableNames, constraintExpression);
        assertEquals(2, filteredVariables.size());
        assertEquals("sst", filteredVariables.get(0));
        assertEquals("wind", filteredVariables.get(1));

        constraintExpression = "sst[0:1:10][0:1:10]";
        filteredVariables = DAPDownloader.filterVariables(variableNames, constraintExpression);
        assertEquals(1, filteredVariables.size());
        assertEquals("sst", filteredVariables.get(0));

        constraintExpression = "bogusVariable[0:1:10][0:1:10]";
        filteredVariables = DAPDownloader.filterVariables(variableNames, constraintExpression);
        assertEquals(2, filteredVariables.size());
        assertEquals("sst", filteredVariables.get(0));
        assertEquals("wind", filteredVariables.get(1));

        constraintExpression = "sst[0:1:10][0:1:10],wind[0:1:10][0:1:10]";
        filteredVariables = DAPDownloader.filterVariables(variableNames, constraintExpression);
        assertEquals(2, filteredVariables.size());
        assertEquals("sst", filteredVariables.get(0));
        assertEquals("wind", filteredVariables.get(1));

        constraintExpression = "sst[0:1:10][0:1:10],wind[0:1:10][0:1:10],sst";
        filteredVariables = DAPDownloader.filterVariables(variableNames, constraintExpression);
        assertEquals(2, filteredVariables.size());
        assertEquals("sst", filteredVariables.get(0));
        assertEquals("wind", filteredVariables.get(1));
    }

    @Test
    public void testFilterDimensions() throws Exception {
        final URL resource = getClass().getResource("test.nc");
        final NetcdfFile netcdfFile = NetcdfFile.open(resource.toString());

        List<String> variableNames = new ArrayList<String>();
        variableNames.add("sst");
        variableNames.add("wind");

        List<Dimension> dimensions = DAPDownloader.filterDimensions(variableNames, netcdfFile);
        Collections.sort(dimensions);
        assertEquals(3, dimensions.size());
        assertEquals("COADSX", dimensions.get(0).getName());
        assertEquals("COADSY", dimensions.get(1).getName());
        assertEquals("TIME", dimensions.get(2).getName());

        variableNames.clear();
        variableNames.add("wind");
        dimensions = DAPDownloader.filterDimensions(variableNames, netcdfFile);
        Collections.sort(dimensions);
        assertEquals(2, dimensions.size());
        assertEquals("COADSX", dimensions.get(0).getName());
        assertEquals("COADSY", dimensions.get(1).getName());

        variableNames.clear();
        variableNames.add("sst");
        dimensions = DAPDownloader.filterDimensions(variableNames, netcdfFile);
        Collections.sort(dimensions);
        assertEquals("COADSX", dimensions.get(0).getName());
        assertEquals("COADSY", dimensions.get(1).getName());
        assertEquals("TIME", dimensions.get(2).getName());
    }

    @Test
    public void testGetOrigin() throws Exception {
        int[] origin = DAPDownloader.getOrigin("sst", "sst[0:1:10][0:1:10],wind[0:1:10][0:1:10]", 3);
        assertArrayEquals(new int[]{0, 0, 0}, origin);

        origin = DAPDownloader.getOrigin("wind", "sst[0:1:10][0:1:10],wind[0:1:10][0:1:10]", 2);
        assertArrayEquals(new int[]{0, 0}, origin);

        origin = DAPDownloader.getOrigin("sst", "sst[5:1:10][10:1:10],wind[1:1:10][0:1:10]", 3);
        assertArrayEquals(new int[]{5, 10, 0}, origin);

        origin = DAPDownloader.getOrigin("sst", "", 3);
        assertArrayEquals(new int[]{0, 0, 0}, origin);
    }

    @Test
    public void testGetConstraintsExpressionForVariable() throws Exception {
        assertEquals("sst[0:1:10][0:1:10]", DAPDownloader.getConstraintExpression("sst", "sst[0:1:10][0:1:10],wind[0:1:10][0:1:10]"));
        assertEquals("wind[0:1:10][0:1:10]", DAPDownloader.getConstraintExpression("wind", "sst[0:1:10][0:1:10],wind[0:1:10][0:1:10]"));
        try {
            DAPDownloader.getConstraintExpression("pig_density", "sst[0:1:10][0:1:10],wind[0:1:10][0:1:10]");
            fail();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("must be included"));
        }

        assertEquals("sst[0:1:10]", DAPDownloader.getConstraintExpression("sst", "sst_flag[0:1:10][0:1:10],wind[0:1:10][0:1:10],sst[0:1:10]"));
        assertEquals("sst[0:1:10]", DAPDownloader.getConstraintExpression("sst", "flag_sst[0:1:10][0:1:10],wind[0:1:10][0:1:10],sst[0:1:10]"));
    }

    @Ignore
    @Test
    public void testActualWriting() throws Exception {
        final DAPDownloader dapDownloader = new DAPDownloader(null, null);
        final DODSNetcdfFile sourceNetcdfFile = new DODSNetcdfFile("http://test.opendap.org:80/opendap/data/nc/coads_climatology.nc");
        dapDownloader.writeNetcdfFile(TESTDATA_DIR, "deleteme.nc", "", sourceNetcdfFile);

        final File testFile = getTestFile("deleteme.nc");
        assertTrue(testFile.exists());
        assertTrue(NetcdfFile.canOpen(testFile.getAbsolutePath()));
        final NetcdfFile netcdfFile = NetcdfFile.open(testFile.getAbsolutePath());
        assertNotNull(netcdfFile.findVariable("SST"));
    }

    @Ignore
    @Test
    public void testActualWriting_WithConstraint() throws Exception {
        final DAPDownloader dapDownloader = new DAPDownloader(null, null);
        final DODSNetcdfFile sourceNetcdfFile = new DODSNetcdfFile("http://test.opendap.org:80/opendap/data/nc/coads_climatology.nc");
        dapDownloader.writeNetcdfFile(TESTDATA_DIR, "deleteme.nc", "COADSX[0:1:4]", sourceNetcdfFile);

        final File testFile = getTestFile("deleteme.nc");
        assertTrue(testFile.exists());
        assertTrue(NetcdfFile.canOpen(testFile.getAbsolutePath()));
        final NetcdfFile netcdfFile = NetcdfFile.open(testFile.getAbsolutePath());
        assertNull(netcdfFile.findVariable("SST"));
        assertNotNull(netcdfFile.findVariable("COADSX"));
    }

    static File getTestFile(String fileName) {
        return new File(TESTDATA_DIR, fileName);
    }

}
