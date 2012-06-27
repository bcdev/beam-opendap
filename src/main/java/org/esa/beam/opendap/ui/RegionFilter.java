package org.esa.beam.opendap.ui;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.binding.BindingContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import javax.measure.unit.NonSI;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.esa.beam.framework.ui.BoundsInputPanel;
import org.esa.beam.framework.ui.GridBagUtils;
import org.esa.beam.framework.ui.RegionSelectableWorldMapPane;
import org.esa.beam.framework.ui.WorldMapPaneDataModel;

public class RegionFilter implements Filter {

    private static String PROPERTY_WEST_BOUND = "westBound";
    private static String PROPERTY_NORTH_BOUND = "northBound";
    private static String PROPERTY_SOUTH_BOUND = "southBound";
    private static String PROPERTY_EAST_BOUND = "eastBound";

    @Override
    public JComponent getUI() {

        final RegionModel regionModel = new RegionModel();
        final BindingContext bindingContext = new BindingContext(PropertyContainer.createObjectBacked(regionModel));

        final WorldMapPaneDataModel worldMapPaneDataModel = new WorldMapPaneDataModel();
        final RegionSelectableWorldMapPane worldMapPane = new RegionSelectableWorldMapPane(worldMapPaneDataModel, bindingContext);

        final DoubleFormatter doubleFormatter = new DoubleFormatter("###0.0##");

        final JLabel westDegreeLabel = new JLabel(NonSI.DEGREE_ANGLE.toString());
        final JLabel westLabel = new JLabel("West:");
        final JFormattedTextField westLonField = new JFormattedTextField(doubleFormatter);
        westLonField.setHorizontalAlignment(JTextField.RIGHT);
        bindingContext.bind(PROPERTY_WEST_BOUND, westLonField);
        bindingContext.getBinding(PROPERTY_WEST_BOUND).addComponent(westLabel);
        bindingContext.getBinding(PROPERTY_WEST_BOUND).addComponent(westDegreeLabel);

        final JLabel eastDegreeLabel = new JLabel(NonSI.DEGREE_ANGLE.toString());
        final JLabel eastLabel = new JLabel("East:");
        final JFormattedTextField eastLonField = new JFormattedTextField(doubleFormatter);
        eastLonField.setHorizontalAlignment(JTextField.RIGHT);
        bindingContext.bind(PROPERTY_EAST_BOUND, eastLonField);
        bindingContext.getBinding(PROPERTY_EAST_BOUND).addComponent(eastLabel);
        bindingContext.getBinding(PROPERTY_EAST_BOUND).addComponent(eastDegreeLabel);

        final JLabel northLabel = new JLabel("North:");
        final JLabel northDegreeLabel = new JLabel(NonSI.DEGREE_ANGLE.toString());
        final JFormattedTextField northLatField = new JFormattedTextField(doubleFormatter);
        northLatField.setHorizontalAlignment(JTextField.RIGHT);
        bindingContext.bind(PROPERTY_NORTH_BOUND, northLatField);
        bindingContext.getBinding(PROPERTY_NORTH_BOUND).addComponent(northLabel);
        bindingContext.getBinding(PROPERTY_NORTH_BOUND).addComponent(northDegreeLabel);

        final JLabel southLabel = new JLabel("South:");
        final JLabel southDegreeLabel = new JLabel(NonSI.DEGREE_ANGLE.toString());
        final JFormattedTextField southLatField = new JFormattedTextField(doubleFormatter);
        southLatField.setHorizontalAlignment(JTextField.RIGHT);
        bindingContext.bind(PROPERTY_SOUTH_BOUND, southLatField);
        bindingContext.getBinding(PROPERTY_SOUTH_BOUND).addComponent(southLabel);
        bindingContext.getBinding(PROPERTY_SOUTH_BOUND).addComponent(southDegreeLabel);

        westLonField.setMinimumSize(new Dimension(120, 20));
        westLonField.setPreferredSize(new Dimension(120, 20));
        northLatField.setMinimumSize(new Dimension(120, 20));
        northLatField.setPreferredSize(new Dimension(120, 20));
        eastLonField.setMinimumSize(new Dimension(120, 20));
        eastLonField.setPreferredSize(new Dimension(120, 20));
        southLatField.setMinimumSize(new Dimension(120, 20));
        southLatField.setPreferredSize(new Dimension(120, 20));

        final JPanel worldMapPaneUI = worldMapPane.createUI();

        final JPanel filterUI = GridBagUtils.createPanel();
        final GridBagConstraints gbc = GridBagUtils.createDefaultConstraints();
        GridBagUtils.addToPanel(filterUI, westLabel, gbc, "anchor=WEST,gridx=0,gridy=1");
        GridBagUtils.addToPanel(filterUI, westLonField, gbc, "gridx=1");
        GridBagUtils.addToPanel(filterUI, westDegreeLabel, gbc, "gridx=2");
        GridBagUtils.addToPanel(filterUI, northLabel, gbc, "gridx=3,gridy=0");
        GridBagUtils.addToPanel(filterUI, northLatField, gbc, "gridx=4");
        GridBagUtils.addToPanel(filterUI, northDegreeLabel, gbc, "gridx=5");
        GridBagUtils.addToPanel(filterUI, eastLabel, gbc, "gridx=6,gridy=1");
        GridBagUtils.addToPanel(filterUI, eastLonField, gbc, "gridx=7");
        GridBagUtils.addToPanel(filterUI, eastDegreeLabel, gbc, "gridx=8");
        GridBagUtils.addToPanel(filterUI, southLabel, gbc, "gridx=3,gridy=2");
        GridBagUtils.addToPanel(filterUI, southLatField, gbc, "gridx=4");
        GridBagUtils.addToPanel(filterUI, southDegreeLabel, gbc, "gridx=5");
        GridBagUtils.addToPanel(filterUI, worldMapPaneUI, gbc, "gridy=3,gridx=0,gridwidth=REMAINDER,insets.top=10,anchor=CENTER");

        return filterUI;
    }

    private static class DoubleFormatter extends JFormattedTextField.AbstractFormatter {

        private final DecimalFormat format;

        DoubleFormatter(String pattern) {
            final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            format = new DecimalFormat(pattern, decimalFormatSymbols);

            format.setParseIntegerOnly(false);
            format.setParseBigDecimal(false);
            format.setDecimalSeparatorAlwaysShown(true);
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            return format.parse(text).doubleValue();
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value == null) {
                return "";
            }
            return format.format(value);
        }
    }

    static class RegionModel {

        double northBound;
        double southBound;
        double eastBound;
        double westBound;
    }
}