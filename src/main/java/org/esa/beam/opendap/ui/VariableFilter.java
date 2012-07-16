package org.esa.beam.opendap.ui;

import org.esa.beam.opendap.DAPVariable;
import org.esa.beam.opendap.OpendapLeaf;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VariableFilter implements FilterComponent {

    private Set<DAPVariable> allVariables = new HashSet<DAPVariable>();
    private Map<DAPVariable, Boolean> variableToSelected = new HashMap<DAPVariable, Boolean>();
    private final JCheckBox checkBox;

    public VariableFilter(JCheckBox filterCheckBox) {
        checkBox = filterCheckBox;
    }

    @Override
    public JComponent getUI() {
        JPanel ui = new JPanel(new BorderLayout(4, 4));
        JList namesList = new JList();
        ui.add(namesList);
        return ui;
    }

    @Override
    public boolean accept(OpendapLeaf leaf) {
        DAPVariable[] dapVariables = leaf.getDAPVariables();
        if (noVariablesAreSelected()) {
            return true;
        }
        for (DAPVariable dapVariable : dapVariables) {
            Boolean isSelected = variableToSelected.get(dapVariable);
            boolean leafContainsVariable = isSelected == null ? false : isSelected;
            if(leafContainsVariable) {
                return true;
            }
        }
        return false;
    }

    private boolean noVariablesAreSelected() {
        for (Boolean selected : variableToSelected.values()) {
            if (selected) {
                return false;
            }
        }
        return true;
    }

    boolean hasVariableSet(){
        return allVariables.size()>0;
    }


    @Override
    public void addFilterChangeListener(FilterChangeListener listener) {
    }

    public void addVariable(DAPVariable dapVariable) {
        allVariables.add(dapVariable);
    }

    public void setVariableSelected(DAPVariable dapVariable, boolean selected) {
        variableToSelected.put(dapVariable, selected);
    }

    void setVariables(Set<DAPVariable> variables) {
        allVariables = variables;
        for (DAPVariable variable : allVariables) {
            Boolean selected = false;
            setVariableSelected(variable, selected);
        }
    }

}
