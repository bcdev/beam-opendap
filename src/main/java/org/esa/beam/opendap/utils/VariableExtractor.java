package org.esa.beam.opendap.utils;

import opendap.dap.BaseType;
import opendap.dap.DDS;
import org.esa.beam.opendap.DAPVariable;

import java.util.ArrayList;
import java.util.Enumeration;

public class VariableExtractor {

    public static DAPVariable[] extractVariables(DDS dds) {
        final Enumeration ddsVariables = dds.getVariables();
        ArrayList<DAPVariable> dapVariables = new ArrayList<DAPVariable>();
        while(ddsVariables.hasMoreElements()){
            final BaseType ddsVariable = (BaseType)ddsVariables.nextElement();
            DAPVariable dapVariable = new DAPVariable();
            dapVariable.setName(ddsVariable.getName());
            dapVariables.add(dapVariable);
        }
        return dapVariables.toArray(new DAPVariable[dapVariables.size()]);
    }

}
