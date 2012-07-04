package org.esa.beam.opendap.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import opendap.dap.Attribute;
import opendap.dap.BaseType;
import opendap.dap.DArray;
import opendap.dap.DArrayDimension;
import opendap.dap.DDS;
import opendap.dap.DGrid;
import opendap.dap.NoSuchVariableException;
import opendap.dap.PrimitiveVector;
import org.esa.beam.opendap.DAPVariable;
import org.esa.beam.util.Debug;

public class VariableExtractor {

    public static DAPVariable[] extractVariables(DDS dds) {
        final Enumeration ddsVariables = dds.getVariables();
        ArrayList<DAPVariable> dapVariables = new ArrayList<DAPVariable>();
        while(ddsVariables.hasMoreElements()){
            final BaseType ddsVariable = (BaseType)ddsVariables.nextElement();
            DAPVariable dapVariable = new DAPVariable(ddsVariable.getName(), ddsVariable.getTypeName(),
                    ddsVariable.getLongName(), new DArrayDimension[0]);
//            dapVariable.setName(ddsVariable.getName());
            dapVariables.add(dapVariable);

            System.out.println();
            System.out.println("-------------");
            System.out.println("Name: " + ddsVariable.getName());
            System.out.println("Type: " + ddsVariable.getTypeName());
            if (ddsVariable instanceof DGrid) {
                final DGrid grid = (DGrid) ddsVariable;
                final DArray array = grid.getArray();
                printArray(array);
            } else {
                final DArray array = (DArray) ddsVariable;
                printArray(array);
            }
            System.out.println("-------------");
            System.out.println();
        }
        return dapVariables.toArray(new DAPVariable[dapVariables.size()]);
    }

    private static void printArray(DArray array) {
        System.out.println("Dimensions: " + array.numDimensions());
        System.out.println("Datatype: " + array.getPrimitiveVector().getTemplate().getTypeName());
        final Enumeration dimensions = array.getDimensions();
        while (dimensions.hasMoreElements()) {
            DArrayDimension dim = (DArrayDimension) dimensions.nextElement();
            System.out.println("dim("+dim.getName()+") size = " + dim.getSize());
        }
    }

}
