package org.esa.beam.opendap.ui;

import org.esa.beam.framework.ui.RegionBoundsInputUI;
import org.esa.beam.opendap.OpendapLeaf;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

public class RegionFilter implements FilterComponent {

    private final JButton applyButton;

    public RegionFilter() {
        applyButton = new JButton("Apply");
    }

    @Override
    public JComponent getUI() {
        final JPanel regionPanel = new JPanel(new BorderLayout());
        final RegionBoundsInputUI regionBoundsInputUI = new RegionBoundsInputUI();
        regionBoundsInputUI.getUI().setBorder(new EmptyBorder(0, 0, 8, 0));
        regionPanel.add(regionBoundsInputUI.getUI(), BorderLayout.NORTH);
        regionPanel.add(applyButton, BorderLayout.EAST);
        return regionPanel;
    }

    @Override
    public boolean accept(OpendapLeaf leaf) {
        return false;
    }

    @Override
    public void addFilterChangeListener(FilterChangeListener listener) {
    }

}