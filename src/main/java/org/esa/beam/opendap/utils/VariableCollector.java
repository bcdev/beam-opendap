package org.esa.beam.opendap.utils;

import opendap.dap.DDS;
import org.esa.beam.opendap.DAPVariable;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class VariableCollector {


    private final Set<String> variableNames;
    private final Set<DAPVariable> variables;

    public VariableCollector() {
        variableNames = new TreeSet<String>();
        variables = new HashSet<DAPVariable>();
    }

    public void collectFrom(DDS dds) {
        final DAPVariable[] dapVariables = VariableExtractor.extractVariables(dds);
        for (DAPVariable dapVariable : dapVariables) {
            variables.add(dapVariable);
            variableNames.add(dapVariable.getName());
        }
    }

    public Set<String> getVariableNames() {
        return variableNames;
    }

    public Set<DAPVariable> getVariables(){
        return variables;
    }

}
