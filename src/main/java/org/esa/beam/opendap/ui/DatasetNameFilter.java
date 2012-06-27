package org.esa.beam.opendap.ui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DatasetNameFilter implements Filter {

    final JTextField expressioneTextField;
    final JButton applyButton;

    public DatasetNameFilter() {
        expressioneTextField = new JTextField();
        applyButton = new JButton("Apply");
    }

    @Override
    public JComponent getUI() {
        final JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(applyButton, BorderLayout.EAST);

        final JPanel filterUI = new JPanel(new BorderLayout(4, 4));
        filterUI.add(expressioneTextField, BorderLayout.NORTH);
        filterUI.add(buttonPanel, BorderLayout.SOUTH);
        return filterUI;
    }
}
