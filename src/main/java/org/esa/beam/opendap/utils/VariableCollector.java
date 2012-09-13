package org.esa.beam.opendap.utils;

import opendap.dap.DDS;
import org.esa.beam.opendap.datamodel.DAPVariable;
import org.esa.beam.opendap.datamodel.OpendapLeaf;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class VariableCollector {


    private final Set<String> variableNames;
    private final Set<DAPVariable> variables;
    private VariableExtractor variableExtractor;

    public VariableCollector() {
        variableNames = new TreeSet<String>();
        variables = new HashSet<DAPVariable>();
        variableExtractor = new VariableExtractor();
    }

    public DAPVariable[] collectDAPVariables(DDS dds) {
        final DAPVariable[] dapVariables = variableExtractor.extractVariables(dds);
        storeDAPVariables(dapVariables);
        return dapVariables;
    }

    public DAPVariable[] collectDAPVariables(OpendapLeaf leaf) {
        final DAPVariable[] dapVariables = variableExtractor.extractVariables(leaf);
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
