/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.opendap;

import opendap.dap.BaseType;
import opendap.dap.DAP2Exception;
import opendap.dap.DConnect2;
import opendap.dap.DDS;
import opendap.dap.DVector;
import thredds.catalog2.Catalog;
import thredds.catalog2.DatasetNode;
import thredds.catalog2.xml.parser.ThreddsXmlParser;
import thredds.catalog2.xml.parser.ThreddsXmlParserException;
import thredds.catalog2.xml.parser.stax.StaxThreddsXmlParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link OpenDapInterface} that connects to an OPeNDAP server.
 *
* @author Thomas Storm
*/
class OpenDapInterfaceImpl implements OpenDapInterface {

    private final URL baseUrl;
    private final Map<String, DDS> ddsMap = new HashMap<String, DDS>();

    OpenDapInterfaceImpl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public DVector getOneDimVariable(String name, String url) throws IOException {
        final DDS dds = getDDS(url);
        final Enumeration variables = dds.getVariables();
        while (variables.hasMoreElements()) {
            final BaseType variable = (BaseType) variables.nextElement();
            if(variable instanceof DVector && variable.getName().equals(name)) {
                return (DVector) variable;
            }
        }
        return null;
    }

    private DDS getDDS(String url) throws IOException {
        if (ddsMap.get(url) != null) {
            return ddsMap.get(url);
        }
        final DDS dds;
        try {
            final DConnect2 dConnect2 = new DConnect2(new URL(url).openStream());
            dds = dConnect2.getDDS();
        } catch (DAP2Exception e) {
            throw new IOException(e);
        }
        ddsMap.put(url, dds);
        return dds;
    }

    @Override
    public DatasetNode[] getDatasets() throws IOException {
        final URI catalogUri = getCatalogUri();
        final Catalog threddsCatalog = getThreddsCatalog(catalogUri);
        final List<DatasetNode> datasets = threddsCatalog.getDatasets().get(0).getDatasets();
        return datasets.toArray(new DatasetNode[datasets.size()]);
    }

    private URI getCatalogUri() throws IOException {
        final URL catalogUrl = new URL(baseUrl.toString().concat("catalog.xml"));
        final URI uri;
        try {
            uri = catalogUrl.toURI();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return uri;
    }

    private Catalog getThreddsCatalog(URI catalogUri) throws IOException {
        final ThreddsXmlParser xmlParser = StaxThreddsXmlParser.newInstance();
        final Catalog catalog;
        try {
            catalog = xmlParser.parse(catalogUri);
        } catch (ThreddsXmlParserException e) {
            throw new IOException(e);
        }
        return catalog;
    }

}
