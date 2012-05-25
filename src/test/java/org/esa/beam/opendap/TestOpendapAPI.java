package org.esa.beam.opendap;

import opendap.dap.Attribute;
import opendap.dap.AttributeTable;
import opendap.dap.BaseType;
import opendap.dap.DAS;
import opendap.dap.DArray;
import opendap.dap.DArrayDimension;
import opendap.dap.DConnect2;
import opendap.dap.DDS;
import opendap.dap.DGrid;
import opendap.dap.DataDDS;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import thredds.catalog2.Catalog;
import thredds.catalog2.DatasetNode;
import thredds.catalog2.xml.parser.ThreddsXmlParser;
import thredds.catalog2.xml.parser.stax.StaxThreddsXmlParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import static org.junit.Assert.*;

/**
 * This class contains solely tests which document the behavior and the usage of the OPeNDAP Java API.
 * Because the tests rely on hardcoded URLs and do not test any classes of the org.esa.beam.opendap package, they are
 * all ignored.
 *
 * @author Thomas Storm
 * @author Tonio Fincke
 */
public class TestOpendapAPI {

    private DConnect2 dConnect;

    @Before
    public void setUp() throws Exception {
        String url = "http://test.opendap.org/dap/data/nc/sst.mnmean.nc.gz";
        dConnect = new DConnect2(url);
    }

    @Test
    @Ignore
    public void testGetCatalog() throws Exception {
        String url = "http://test.opendap.org/dap/data/nc/catalog.xml";
        final ThreddsXmlParser xmlParser = StaxThreddsXmlParser.newInstance();
        final Catalog catalog = xmlParser.parse(new URL(url).toURI());
        DatasetNode dataset = catalog.getDatasets().get(0);
        System.out.println("dataset.id = " + dataset.getId());
        for (DatasetNode datasetNode : dataset.getDatasets()) {
            System.out.println("datasetNode.getId() = " + datasetNode.getId());
        }
    }

    @Test
    @Ignore
    public void testGetDDS() throws Exception {
        final DDS dds = dConnect.getDDS();
        final Enumeration variables = dds.getVariables();
        Set<String> variableNames = new HashSet<String>();
        while (variables.hasMoreElements()) {
            final Object currentVariable = variables.nextElement();
            assertTrue(currentVariable instanceof DArray || currentVariable instanceof DGrid);
            variableNames.add(((BaseType) currentVariable).getName());
            if (currentVariable instanceof DArray) {
                final DArray variable = (DArray) currentVariable;
                if (variable.getName().equals("lat")) {
                    assertEquals(1, variable.numDimensions());
                    assertEquals(89, variable.getDimension(0).getSize());
                } else if (variable.getName().equals("lon")) {
                    assertEquals(1, variable.numDimensions());
                    assertEquals(180, variable.getDimension(0).getSize());
                } else if (variable.getName().equals("time")) {
                    assertEquals(1, variable.numDimensions());
                    assertEquals(1857, variable.getDimension(0).getSize());
                } else if (variable.getName().equals("time_bnds")) {
                    assertEquals(2, variable.numDimensions());
                    assertEquals(1857, variable.getDimension(0).getSize());
                    assertEquals(2, variable.getDimension(1).getSize());
                }
            } else if (currentVariable instanceof DGrid) {
                final DGrid variable = (DGrid) currentVariable;
                final DArray gridArray = variable.getArray();
                assertEquals(3, gridArray.numDimensions());
                assertEquals(1857, gridArray.getDimension(0).getSize());
                assertEquals(89, gridArray.getDimension(1).getSize());
                assertEquals(180, gridArray.getDimension(2).getSize());
                final Vector<DArrayDimension> gridMaps = variable.getArrayDims();
                testMap(gridMaps.get(0), "time", 1857);
                testMap(gridMaps.get(1), "lat", 89);
                testMap(gridMaps.get(2), "lon", 180);

            }
        }
        assertTrue(variableNames.contains("lat"));
        assertTrue(variableNames.contains("lon"));
        assertTrue(variableNames.contains("time"));
        assertTrue(variableNames.contains("time_bnds"));
        assertTrue(variableNames.contains("sst"));
    }

    @Test
    @Ignore
    public void testGetDAS() throws Exception {
        final DAS das = dConnect.getDAS();
        final Enumeration attributeNames = das.getNames();
        assertTrue(attributeNames.hasMoreElements());
        final Set<String> attributeNameSet = new HashSet<String>();
        while (attributeNames.hasMoreElements()) {
            attributeNameSet.add(attributeNames.nextElement().toString());
        }
        assertTrue(attributeNameSet.contains("lat"));
        assertTrue(attributeNameSet.contains("lon"));
        assertTrue(attributeNameSet.contains("time"));
        assertTrue(attributeNameSet.contains("time_bnds"));
        assertTrue(attributeNameSet.contains("sst"));
        assertTrue(attributeNameSet.contains("NC_GLOBAL"));

        final AttributeTable attributeTableLat = das.getAttribute("lat").getContainer();
        final Enumeration attributeTableLatNames = attributeTableLat.getNames();
        testLatLonAttributes(attributeTableLat, attributeTableLatNames, "Latitude", 88.0f, -88.0f, "latitude_north", "y");

        final AttributeTable attributeTableLon = das.getAttribute("lon").getContainer();
        final Enumeration attributeTableLonNames = attributeTableLon.getNames();
        testLatLonAttributes(attributeTableLon, attributeTableLonNames, "Longitude", 0.0f, 358.0f, "longitude_east", "x");

        final AttributeTable globalAttributes = das.getAttributeTable("NC_GLOBAL");
        assertNotNull(globalAttributes);

        final HashSet<String> globalAttributesNamesSet = new HashSet<String>();
        final Enumeration globalAttributesNames = globalAttributes.getNames();
        while (globalAttributesNames.hasMoreElements()) {
            globalAttributesNamesSet.add(globalAttributesNames.nextElement().toString());
        }
        assertTrue(globalAttributesNamesSet.contains("title"));
        assertTrue(globalAttributesNamesSet.contains("conventions"));
        assertTrue(globalAttributesNamesSet.contains("history"));
        assertTrue(globalAttributesNamesSet.contains("comments"));
        assertTrue(globalAttributesNamesSet.contains("platform"));
        assertTrue(globalAttributesNamesSet.contains("source"));
        assertTrue(globalAttributesNamesSet.contains("institution"));
        assertTrue(globalAttributesNamesSet.contains("references"));
        assertTrue(globalAttributesNamesSet.contains("citation"));

        assertEquals("NOAA Extended Reconstructed SST V3", globalAttributes.getAttribute("title").getValueAt(0));
    }

    private void testMap(DArrayDimension map, String expectedName, int expectedSize) {
        assertEquals(expectedName, map.getName());
        assertEquals(expectedSize, map.getSize());
    }

    @Test
    @Ignore
    public void testDownloadData() throws Exception {
        InputStream inputStream = null;
        OutputStream os = null;
        File file = null;
        try {
            URL url = new URL("http://test.opendap.org/dap/data/nc/data.nc");
            final URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();
            file = new File("data.nc");
            os = new FileOutputStream(file);
            final byte[] buffer = new byte[50 * 1024];
            while (inputStream.read(buffer) != -1) {
                os.write(buffer);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (os != null) {
                os.close();
            }
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @Ignore
    public void testGetDDX() throws Exception {
        final DConnect2 dConnect2 = new DConnect2("http://test.opendap.org/opendap/data/nc/sst.mnmean.nc.gz");
        final DataDDS data = dConnect2.getData("geogrid(sst,-90,-150,-89,-140)");
//        final DataDDS data = dConnect2.getData("geogrid(sst,-89,-179,-88,-178");
        System.out.println("TestOpendapAPI.testGetDDX");
    }

    private void testLatLonAttributes(AttributeTable attributeTable, Enumeration attributeTableNames, String expectedLongName, float expectedMin, float expectedMax, String expectedStandardName, String expectedAxis) throws Exception {
        final Set<Attribute> attributeSet = new HashSet<Attribute>();
        final Set<String> attributeNamesSet = new HashSet<String>();
        while (attributeTableNames.hasMoreElements()) {
            final String attributeName = attributeTableNames.nextElement().toString();
            final Attribute attribute = attributeTable.getAttribute(attributeName);
            attributeSet.add(attribute);
            attributeNamesSet.add(attributeName);
        }
        for (Attribute attribute : attributeSet) {
            if (attribute.getName().equals("units")) {
                assertTrue(attribute.getValueAt(0).matches("degrees_.*"));
            } else if (attribute.getName().equals("long_name")) {
                assertEquals(expectedLongName, attribute.getValueAt(0));
            } else if (attribute.getName().equals("actual_range")) {
                assertEquals(Attribute.FLOAT32, attribute.getType());
                final Iterator valuesIterator = attribute.getValuesIterator();
                assertEquals(expectedMin, Float.parseFloat(valuesIterator.next().toString()), 1.0E-7);
                assertEquals(expectedMax, Float.parseFloat(valuesIterator.next().toString()), 1.0E-7);
            } else if (attribute.getName().equals("standard_name")) {
                assertEquals(expectedStandardName, attribute.getValueAt(0));
            } else if (attribute.getName().equals("axis")) {
                assertEquals(expectedAxis, attribute.getValueAt(0));
            } else if (attribute.getName().equals("coordinate_defines")) {
                assertEquals("center", attribute.getValueAt(0));
            }
        }

        assertTrue(attributeNamesSet.contains("units"));
        assertTrue(attributeNamesSet.contains("long_name"));
        assertTrue(attributeNamesSet.contains("actual_range"));
        assertTrue(attributeNamesSet.contains("standard_name"));
        assertTrue(attributeNamesSet.contains("axis"));
        assertTrue(attributeNamesSet.contains("coordinate_defines"));
    }
}
