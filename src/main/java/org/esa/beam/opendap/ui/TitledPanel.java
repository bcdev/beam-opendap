package org.esa.beam.opendap.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

public class TitledPanel extends JPanel {

    public TitledPanel(JComponent titleComponent, JComponent bodyComponent) {
        super(new BorderLayout());

        final JPanel titleArea = new JPanel(new BorderLayout());
        if (titleComponent != null) {
            titleArea.add(titleComponent, BorderLayout.WEST);
        }
        titleArea.add(getSeparator(), BorderLayout.CENTER);
        add(titleArea, BorderLayout.NORTH);

        if (bodyComponent != null) {
            bodyComponent.setBorder(new EmptyBorder(0, 30, 0, 0));
            add(bodyComponent, BorderLayout.CENTER);
        }

        setBorder(new EmptyBorder(4, 8, 4, 8));
    }

    private JPanel getSeparator() {
        final JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        final JPanel separatorPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        separatorPanel.add(separator, gbc);
        return separatorPanel;
    }
}
