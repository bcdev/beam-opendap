package org.esa.beam.opendap.utils;

import opendap.dap.BaseType;
import opendap.dap.DArray;
import opendap.dap.DArrayDimension;
import opendap.dap.DDS;
import org.esa.beam.opendap.DAPVariable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class VariableExtractor {

    public static DAPVariable[] extractVariables(DDS dds) {
        final Enumeration ddsVariables = dds.getVariables();
        ArrayList<DAPVariable> dapVariables = new ArrayList<DAPVariable>();
        while (ddsVariables.hasMoreElements()) {
            final BaseType ddsVariable = (BaseType) ddsVariables.nextElement();
            DAPVariable dapVariable = convertToDAPVariable(ddsVariable);
            dapVariables.add(dapVariable);
        }
        return dapVariables.toArray(new DAPVariable[dapVariables.size()]);
    }

    private static DAPVariable convertToDAPVariable(BaseType ddsVariable) {
//        final DArray array;
//        if (ddsVariable instanceof DGrid) {
//            final DGrid grid = (DGrid) ddsVariable;
//            array = grid.getArray();
//        } else if (ddsVariable instanceof DArray) {
//            array = (DArray) ddsVariable;
//        } else {
//            array = null;
//        }

        final String name = ddsVariable.getName();
//        final String typeName = ddsVariable.getTypeName();
//        final String dataTypeName = getDataTypeName(array);
//        final DArrayDimension[] dimensions = getDimenstions(array);

//        return new DAPVariable(name, typeName, dataTypeName, dimensions);
        return new DAPVariable(name, null, null, null);
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
