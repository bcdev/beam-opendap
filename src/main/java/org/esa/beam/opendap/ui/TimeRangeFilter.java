package org.esa.beam.opendap.ui;

import com.jidesoft.combobox.DateExComboBox;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TimeRangeFilter implements Filter {

    final DateExComboBox startTimePicker;
    final DateExComboBox stopTimePicker;
    final JButton applyButton;

    public TimeRangeFilter() {
        final int width = 120;

        startTimePicker = new DateExComboBox();
        stopTimePicker = new DateExComboBox();
        final Dimension ps = startTimePicker.getPreferredSize();

        startTimePicker.setPreferredSize(new Dimension(width, ps.height));
        startTimePicker.setMinimumSize(new Dimension(width, ps.height));
        stopTimePicker.setPreferredSize(new Dimension(width, ps.height));
        stopTimePicker.setMinimumSize(new Dimension(width, ps.height));

        applyButton = new JButton("Apply");
    }

    @Override
    public JComponent getUI() {

        final JPanel filterUI = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.insets.bottom = 4;
        gbc.insets.right = 4;
        gbc.anchor = GridBagConstraints.WEST;

        filterUI.add(new JLabel("Start Date:"), gbc);

        gbc.gridx++;
        filterUI.add(startTimePicker, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        filterUI.add(new JLabel("Stop Date:"), gbc);

        gbc.gridx++;
        filterUI.add(stopTimePicker, gbc);

        gbc.gridy++;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        filterUI.add(applyButton, gbc);

        return filterUI;
    }
}
