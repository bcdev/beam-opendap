package org.esa.beam.opendap;

import opendap.dap.DVector;
import org.esa.beam.framework.datamodel.ProductData;
import org.junit.Test;
import thredds.catalog2.Catalog;
import thredds.catalog2.DatasetNode;
import thredds.catalog2.Metadata;
import thredds.catalog2.Property;
import thredds.catalog2.ThreddsMetadata;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.*;

/**
 * @author Thomas Storm
 */
public class FilterTest {

    @Test
    public void testFilterByFileName() throws Exception {
        final OpenDapInterface openDapInterface = new TestOpenDapInterface();
        final Filter filter = new FilterBuilder()
                .baseUrl(new URL("http://some.arbitrary.base.url/"))
                .fileNamePattern("valid.*file(_ext)?")
                .openDapInterface(openDapInterface)
                .build();
        final Set<String> urls = filter.filterByFileName();
        assertEquals(2, urls.size());
        final String[] urlsArray = urls.toArray(new String[urls.size()]);
        Arrays.sort(urlsArray);
        assertEquals("http://some.arbitrary.base.url/valid_netcdf_file_ext", urlsArray[0]);
        assertEquals("http://some.arbitrary.base.url/valid_other_file", urlsArray[1]);
    }

    @Test
    public void testFilter() throws Exception {
        double minLat = -89.0;
        double maxLat = 89.0;
        double minLon = -179.0;
        double maxLon = 180.0;
        final URL baseUrl = new URL("http://test.opendap.org/dap/data/nc/");
        final OpenDapInterface openDapInterface = new OpenDapInterfaceImpl(baseUrl);
        Filter filter = new FilterBuilder()
                .baseUrl(new URL("http://test.opendap.org/dap/data/nc/"))
                .fileNamePattern("sst.*\\.nc\\.gz")
                .openDapInterface(openDapInterface)
                .minLat(minLat)
                .maxLat(maxLat)
                .minLon(minLon)
                .maxLon(maxLon)
                .startDate(ProductData.UTC.parse("1970-01-01", "yyyy-MM-dd"))
                .endDate(ProductData.UTC.parse("2200-01-01", "yyyy-MM-dd"))
                .build();
        Set<String> productUrls = filter.filter();
        boolean containsSst = false;
        for (String productUrl : productUrls) {
            if (productUrl.equals("http://test.opendap.org/dap/data/nc/sst.mnmean.nc.gz")) {
                containsSst = true;
            }
        }
        assertTrue(containsSst);

        minLat = -80.0; // greater than product's minimum latitude, so product is expected to be filtered out
        maxLat = 89.0;
        minLon = -179.0;
        maxLon = 180.0;
        filter = new FilterBuilder()
                .baseUrl(new URL("http://test.opendap.org/dap/data/nc/"))
                .fileNamePattern("sst.*\\.nc\\.gz")
                .openDapInterface(openDapInterface)
                .minLat(minLat)
                .maxLat(maxLat)
                .minLon(minLon)
                .maxLon(maxLon)
                .startDate(ProductData.UTC.parse("1970-01-01", "yyyy-MM-dd"))
                .endDate(ProductData.UTC.parse("2200-01-01", "yyyy-MM-dd"))
                .build();

        productUrls = filter.filter();
        containsSst = false;
        for (String productUrl : productUrls) {
            if (productUrl.equals("http://test.opendap.org/opendap/hyrax/data/nc/sst.mnmean.nc.gz")) {
                containsSst = true;
            }
        }
        assertFalse(containsSst);

        minLat = -89.0;
        maxLat = 89.0;
        minLon = -20.0;  // greater than product's minimum latitude, so product is expected to be filtered out
        maxLon = 180.0;
        filter = new FilterBuilder()
                .baseUrl(new URL("http://test.opendap.org/dap/data/nc/"))
                .fileNamePattern("sst.*\\.nc\\.gz")
                .openDapInterface(openDapInterface)
                .minLat(minLat)
                .maxLat(maxLat)
                .minLon(minLon)
                .maxLon(maxLon)
                .startDate(ProductData.UTC.parse("1970-01-01", "yyyy-MM-dd"))
                .endDate(ProductData.UTC.parse("2200-01-01", "yyyy-MM-dd"))
                .build();

        productUrls = filter.filter();
        containsSst = false;
        for (String productUrl : productUrls) {
            if (productUrl.equals("http://test.opendap.org/opendap/hyrax/data/nc/sst.mnmean.nc.gz")) {
                containsSst = true;
            }
        }
        assertFalse(containsSst);
    }

    private static class TestOpenDapInterface implements OpenDapInterface {

        @Override
        public DVector getOneDimVariable(String varName, String url) throws IOException {
            return null;
        }

        @Override
        public DatasetNode[] getDatasets() throws IOException {
            return new DatasetNode[] {
                    new MyDatasetNode("valid_netcdf_file_ext"),
                    new MyDatasetNode("valid_other_file"),
                    new MyDatasetNode("invalid_netcdf_file_ext"),
                    new MyDatasetNode("invalid_other_file_ext"),
                    new MyDatasetNode("invalid_other_file")
            };
        }

        private static class MyDatasetNode implements DatasetNode {

            private String name;

            public MyDatasetNode(String name) {
                this.name = name;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getId() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public String getIdAuthority() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public List<Property> getProperties() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public Property getPropertyByName(String name) {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public ThreddsMetadata getThreddsMetadata() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public List<Metadata> getMetadata() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public Catalog getParentCatalog() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public DatasetNode getParent() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public boolean isCollection() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public List<DatasetNode> getDatasets() {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public DatasetNode getDatasetById(String id) {
                throw new IllegalStateException("not implemented");
            }
        }
    }
}
