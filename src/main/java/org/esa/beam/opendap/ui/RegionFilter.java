package org.esa.beam.opendap.ui;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.binding.BindingContext;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.esa.beam.framework.ui.RegionSelectableWorldMapPane;
import org.esa.beam.framework.ui.WorldMapPaneDataModel;

public class RegionFilter implements Filter {

    @Override
    public JComponent getUI() {
        final WorldMapPaneDataModel dataModel = new WorldMapPaneDataModel();
        final RegionSelectableWorldMapPane worldMapPane;
        final RegionModel regionModel = new RegionModel();
        worldMapPane = new RegionSelectableWorldMapPane(dataModel, new BindingContext(PropertyContainer.createObjectBacked(regionModel)));

        final JPanel filterUI = new JPanel(new BorderLayout());
        filterUI.add(worldMapPane.createUI());
        return filterUI;
    }

    static class RegionModel {

        float northBound = 50;
        float southBound = 40;
        float eastBound = 8;
        float westBound = -2;
    }
}
