package org.esa.beam.opendap.utils;

import opendap.dap.BaseType;
import opendap.dap.DDS;

import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

public class VariableCollector {


    private final Set<String> variableNames;

    public VariableCollector() {
        variableNames = new TreeSet<String>();
    }

    public void collectFrom(DDS dds) {
        final Enumeration<BaseType> variables = dds.getVariables();
        while (variables.hasMoreElements()) {
            final BaseType variable = variables.nextElement();
            variableNames.add(variable.getLongName());
        }
    }

    public Set<String> getVariableNames() {
        return variableNames;
    }
}
