package org.esa.beam.opendap.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class VariableNameFilter implements Filter {

    @Override
    public JComponent getUI() {
        return new JLabel("Variable Name Filter UI");
    }
}
