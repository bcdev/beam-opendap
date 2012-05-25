package org.esa.beam.opendap;

import opendap.dap.DVector;
import opendap.dap.PrimitiveVector;
import org.esa.beam.framework.datamodel.ProductData;
import thredds.catalog2.DatasetNode;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Storm
 */
class Filter {

    final FilterConfig config;

    Filter(FilterConfig config) {
        this.config = config;
    }

    Set<String> filter() throws Exception {
        final Set<String> productUrls = filterByFileName();
        for (String productUrl : productUrls) {
            // todo - this is the CF case. Add strategy for other cases, too.
            final DVector latVariable = config.openDapInterface.getOneDimVariable("lat", productUrl);
            final DVector lonVariable = config.openDapInterface.getOneDimVariable("lon", productUrl);
            final PrimitiveVector primitiveVector = latVariable.getPrimitiveVector();
            System.out.println("Filter.filter");
        }
        return productUrls;
    }

    Set<String> filterByFileName() throws IOException {
        final Set<String> productUrls = new HashSet<String>();
        DatasetNode[] datasetNodes = config.openDapInterface.getDatasets();
        String baseUrl = config.baseUrl.toString().endsWith("/") ? config.baseUrl.toString() : config.baseUrl.toString() + "/";
        for (DatasetNode datasetNode : datasetNodes) {
            if (datasetNode.getName().matches(config.fileNamePattern)) {
                productUrls.add(baseUrl.concat(datasetNode.getName()));
            }
        }
        return productUrls;
    }

    static class FilterConfig {

        final URL baseUrl;
        final String fileNamePattern;
        final OpenDapInterface openDapInterface;
        final double minLat;
        final double maxLat;
        final double minLon;
        final double maxLon;
        final ProductData.UTC startDate;
        final ProductData.UTC endDate;

        FilterConfig(URL baseUrl, String fileNamePattern, OpenDapInterface openDapInterface, double minLat, double maxLat, double minLon, double maxLon, ProductData.UTC startDate, ProductData.UTC endDate) {

            this.baseUrl = baseUrl;
            this.openDapInterface = openDapInterface;
            this.fileNamePattern = fileNamePattern;
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
