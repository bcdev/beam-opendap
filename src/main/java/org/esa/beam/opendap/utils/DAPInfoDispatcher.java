package org.esa.beam.opendap.utils;

import opendap.dap.DDS;
import opendap.dap.http.HTTPException;
import opendap.dap.http.HTTPMethod;
import opendap.dap.http.HTTPSession;
import org.esa.beam.opendap.ui.CatalogTree;

public class DAPInfoDispatcher {

    public static void dispatchLeafInfo(CatalogTree.OPeNDAP_Leaf leaf){
        final String ddsUri = leaf.getDdsUri();
        DDS dds = null;
        try {
            HTTPSession session = new HTTPSession();
            final HTTPMethod httpMethod = session.newMethodGet(ddsUri);
            dds = new DDS(httpMethod.getResponseAsString());
        } catch (HTTPException e) {
            // todo handle exceptions
            e.printStackTrace();
        }
        //todo continue work on class
    }

}
