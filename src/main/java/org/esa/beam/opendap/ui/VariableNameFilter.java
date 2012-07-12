package org.esa.beam.opendap.ui;

import org.esa.beam.opendap.OpendapLeaf;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class VariableNameFilter implements FilterComponent {

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

    @Override
    public boolean accept(OpendapLeaf leaf) {
        return false;
    }

    @Override
    public void addFilterChangeListener(FilterChangeListener listener) {
    }
}
