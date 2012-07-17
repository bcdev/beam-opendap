package org.esa.beam.opendap.utils;

import opendap.dap.BaseType;
import opendap.dap.DArray;
import opendap.dap.DArrayDimension;
import opendap.dap.DDS;
import opendap.dap.DGrid;
import opendap.dap.http.HTTPException;
import opendap.dap.http.HTTPMethod;
import opendap.dap.http.HTTPSession;
import org.esa.beam.opendap.DAPVariable;
import org.esa.beam.opendap.OpendapLeaf;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class VariableExtractor {

    public static DAPVariable[] extractVariables(OpendapLeaf leaf) {
        DDS dds = getDDS(leaf);
        return extractVariables(dds);
    }

    public static DAPVariable[] extractVariables(DDS dds) {
        final Enumeration ddsVariables = dds.getVariables();
        final List<DAPVariable> dapVariables = new ArrayList<DAPVariable>();
        while (ddsVariables.hasMoreElements()) {
            final BaseType ddsVariable = (BaseType) ddsVariables.nextElement();
            DAPVariable dapVariable = convertToDAPVariable(ddsVariable);
            dapVariables.add(dapVariable);
        }
        return dapVariables.toArray(new DAPVariable[dapVariables.size()]);
    }

    private static DDS getDDS(OpendapLeaf leaf) {
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
        return dds;
    }

    private static DAPVariable convertToDAPVariable(BaseType ddsVariable) {
        final DArray array;
        if (ddsVariable instanceof DGrid) {
            final DGrid grid = (DGrid) ddsVariable;
            array = grid.getArray();
        } else if (ddsVariable instanceof DArray) {
            array = (DArray) ddsVariable;
        } else {
            array = null;
        }

        final String name = ddsVariable.getName();
        final String typeName = ddsVariable.getTypeName();
        final String dataTypeName = getDataTypeName(array);
        final DArrayDimension[] dimensions = getDimensions(array);

        return new DAPVariable(name, typeName, dataTypeName, dimensions);
    }

    private static String getDataTypeName(DArray array) {
        return array.getPrimitiveVector().getTemplate().getTypeName();
    }

    private static DArrayDimension[] getDimensions(DArray array) {
        final Enumeration dimensions = array.getDimensions();
        final List<DArrayDimension> dims = new ArrayList();
        while (dimensions.hasMoreElements()) {
            DArrayDimension dimension = (DArrayDimension) dimensions.nextElement();
            dims.add(dimension);
        }
        return dims.toArray(new DArrayDimension[dims.size()]);
    }

}
