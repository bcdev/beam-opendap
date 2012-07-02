package org.esa.beam.opendap.ui;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

public class VariableNameFilter implements Filter {

    public VariableNameFilter() {
    }

    @Override
    public JComponent getUI() {
        final JPanel ui = new JPanel(new BorderLayout(4, 4));
        final JList namesList = new JList(new Object[]{"scan variable names ..."});
//        namesList.setListData(); //On scan completed;
        ui.add(namesList);
        return ui;
    }
}
