package org.esa.beam.opendap;

import org.esa.beam.framework.datamodel.ProductData;

import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Thomas Storm
 */
class FilterBuilder {

    private URL baseUrl;
    private String fileNamePattern;
    private OpenDapInterface openDapInterface;
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private ProductData.UTC startDate;
    private ProductData.UTC endDate;

    private boolean minLatSet = false;
    private boolean maxLatSet = false;
    private boolean minLonSet = false;
    private boolean maxLonSet = false;

    FilterBuilder baseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    FilterBuilder fileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
        return this;
    }


    FilterBuilder openDapInterface(OpenDapInterface openDapInterface) {
        this.openDapInterface = openDapInterface;
        return this;
    }

    FilterBuilder minLat(double minLat) {
        this.minLat = minLat;
        minLatSet = true;
        return this;
    }

    FilterBuilder maxLat(double maxLat) {
        this.maxLat = maxLat;
        maxLatSet = true;
        return this;
    }

    FilterBuilder minLon(double minLon) {
        this.minLon = minLon;
        minLonSet = true;
        return this;
    }

    FilterBuilder maxLon(double maxLon) {
        this.maxLon = maxLon;
        maxLonSet = true;
        return this;
    }

    FilterBuilder startDate(ProductData.UTC startDate) {
        this.startDate = startDate;
        return this;
    }

    FilterBuilder endDate(ProductData.UTC endDate) {
        this.endDate = endDate;
        return this;
    }

    Filter build() {
        setDefaultValues();
        validate();
        final Filter.FilterConfig filterConfig = new Filter.FilterConfig(baseUrl, fileNamePattern, openDapInterface, minLat, maxLat, minLon, maxLon, startDate, endDate);
        return new Filter(filterConfig);
    }

    private void validate() {
        if (minLat < -90.0) {
            throw new IllegalStateException("minLat < -90.0");
        }
        if (maxLat > 90.0) {
            throw new IllegalStateException("maxLat > 90.0");
        }
        if (minLon < -180.0) {
            throw new IllegalStateException("minLon < -180.0");
        }
        if (maxLon > 180.0) {
            throw new IllegalStateException("maxLon > 180.0");
        }
        if (minLon > maxLon) {
            throw new IllegalStateException("minLon > maxLon");
        }
        if (minLat > maxLat) {
            throw new IllegalStateException("minLat > maxLat");
        }
        if (startDate != null && endDate != null && endDate.getAsDate().before(startDate.getAsDate())) {
            throw new IllegalStateException("endDate before startDate");
        }
        if (baseUrl == null) {
            throw new IllegalStateException("baseUrl is null");
        }
        if (openDapInterface == null) {
            throw new IllegalStateException("openDapInterface is null");
        }
        try {
            Pattern.compile(fileNamePattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException("Invalid file name pattern.", e);
        }
    }

    private void setDefaultValues() {
        if(fileNamePattern == null || fileNamePattern.isEmpty()) {
            fileNamePattern(".*");
        }
        if (!minLonSet) {
            minLon = -180.0;
        }
        if (!maxLonSet) {
            maxLon = 180.0;
        }
        if (!minLatSet) {
            minLat = -90.0;
        }
        if (!maxLatSet) {
            maxLat = 90.0;
        }
    }

}
