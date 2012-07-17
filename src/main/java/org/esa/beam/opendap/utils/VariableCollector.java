package org.esa.beam.opendap.utils;

import opendap.dap.DDS;
import org.esa.beam.opendap.DAPVariable;
import org.esa.beam.opendap.OpendapLeaf;

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

    public DAPVariable[] collectDAPVariables(DDS dds) {
        final DAPVariable[] dapVariables = VariableExtractor.extractVariables(dds);
        storeDAPVariables(dapVariables);
        return dapVariables;
    }

    public DAPVariable[] collectDAPVariables(OpendapLeaf leaf) {
        final DAPVariable[] dapVariables = VariableExtractor.extractVariables(leaf);
        storeDAPVariables(dapVariables);
        return dapVariables;
    }

    private void storeDAPVariables(DAPVariable[] dapVariables) {
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
