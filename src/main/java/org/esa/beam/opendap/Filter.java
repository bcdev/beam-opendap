package org.esa.beam.opendap;

import opendap.dap.DVector;
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


    private final URL baseUrl;
    private final String fileNamePattern;
    private final OpenDapInterface openDapInterface;
    private final double minLat;
    private final double maxLat;
    private final double minLon;
    private final double maxLon;
    private final ProductData.UTC startDate;
    private final ProductData.UTC endDate;

    public Filter(URL baseUrl, String fileNamePattern, OpenDapInterface openDapInterface, double minLat, double maxLat, double minLon, double maxLon, ProductData.UTC startDate, ProductData.UTC endDate) {
        // todo - move this insanity into config class and create builder around it
        this.baseUrl = baseUrl;
        this.fileNamePattern = fileNamePattern;
        this.openDapInterface = openDapInterface;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Set<String> filter() throws Exception {
        final Set<String> productUrls = filterByFileName();
        for (String productUrl : productUrls) {
            final DVector latVariable = openDapInterface.getOneDimVariable("lat", productUrl);
            // todo - continue
        }
        return productUrls;
    }

    Set<String> filterByFileName() throws IOException {
        final Set<String> productUrls = new HashSet<String>();
        DatasetNode[] datasetNodes = openDapInterface.getDatasets();
        String baseUrl = this.baseUrl.toString().endsWith("/") ? this.baseUrl.toString() : this.baseUrl.toString() + "/";
        for (DatasetNode datasetNode : datasetNodes) {
            if(datasetNode.getName().matches(fileNamePattern)) {
                productUrls.add(baseUrl.concat(datasetNode.getName()));
            }
        }
        return productUrls;
    }

}
