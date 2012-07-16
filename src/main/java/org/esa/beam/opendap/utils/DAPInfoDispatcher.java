package org.esa.beam.opendap.utils;

import opendap.dap.DDS;
import opendap.dap.http.HTTPException;
import opendap.dap.http.HTTPMethod;
import opendap.dap.http.HTTPSession;
import org.esa.beam.opendap.DAPVariable;
import org.esa.beam.opendap.OpendapLeaf;

import java.util.Set;

/**
 * A class responsible for dealing with DAP node related information
 *
 * @author Tonio Fincke
 */
public class DAPInfoDispatcher {

    private VariableCollector variableCollector;

    public DAPInfoDispatcher() {
        variableCollector = new VariableCollector();
    }

    public void dispatchLeafInfo(OpendapLeaf leaf){
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
        final DAPVariable[] dapVariables = variableCollector.collectDAPVariablesFromDDS(dds);
        leaf.addDAPVariables(dapVariables);
    }

    public Set<DAPVariable> getVariablesFromAllLeaves() {
        return variableCollector.getVariables();
    }

}
