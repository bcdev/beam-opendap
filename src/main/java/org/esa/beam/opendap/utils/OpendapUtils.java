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

package org.esa.beam.opendap.utils;

import opendap.dap.http.HTTPMethod;
import opendap.dap.http.HTTPSession;
import org.esa.beam.opendap.OpendapLeaf;
import thredds.catalog.InvDatasetImpl;

import java.io.IOException;

/**
 * Contains some methods useful when dealing with OPeNDAP servers.
 *
 * @author Thomas Storm
 */
public class OpendapUtils {


    public static double getDataSize(OpendapLeaf leaf) {
        return ((InvDatasetImpl) leaf.getDataset()).getLocalMetadata().getDataSize() / (1024 * 1024);
    }

    public static String getResponse(String fileUri) throws IOException {
        HTTPSession session = new HTTPSession();
        final HTTPMethod httpMethod = session.newMethodGet(fileUri);
        httpMethod.execute();
        return httpMethod.getResponseAsString();
    }
}
