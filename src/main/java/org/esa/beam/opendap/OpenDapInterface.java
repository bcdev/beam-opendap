package org.esa.beam.opendap;


import opendap.dap.DVector;
import thredds.catalog2.DatasetNode;

import java.io.IOException;

/**
 * @author Thomas Storm
 */
interface OpenDapInterface {

    /**
     * @param varName the variable's name
     * @param url     the URL of the file to search for the variable
     *
     * @return the variable with the given name from the file given by <code>url</code>, or <code>null</code> if it does
     *         not exist
     *
     * @throws IOException
     */
    DVector getOneDimVariable(String varName, String url) throws IOException;

    DatasetNode[] getDatasets() throws IOException;
}
