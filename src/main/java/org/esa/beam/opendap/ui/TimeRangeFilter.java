package org.esa.beam.opendap.ui;

import com.bc.ceres.binding.ValidationException;
import com.jidesoft.combobox.DateExComboBox;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.opendap.OpendapLeaf;
import org.esa.beam.opendap.utils.PatternProvider;
import org.esa.beam.opendap.utils.TimeStampExtractor;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeRangeFilter implements FilterComponent {

    private JComboBox datePatternComboBox;
    private JComboBox fileNamePatternComboBox;
    private DateExComboBox startTimePicker;
    private DateExComboBox stopTimePicker;
    private JButton applyButton;
    private JCheckBox filterCheckBox;
    TimeStampExtractor timeStampExtractor;
    List<FilterChangeListener> listeners;

    Date startDate;
    Date endDate;

    public TimeRangeFilter(final JCheckBox filterCheckBox) {
        this.filterCheckBox = filterCheckBox;
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        startTimePicker = new DateExComboBox();
        startTimePicker.setLocale(Locale.ENGLISH);
        startTimePicker.setFormat(dateFormat);
        stopTimePicker = new DateExComboBox();
        stopTimePicker.setLocale(Locale.ENGLISH);
        stopTimePicker.setFormat(dateFormat);

        final int width = 150;
        final Dimension ps = startTimePicker.getPreferredSize();
        final Dimension comboBoxDimension = new Dimension(width, ps.height);
        setComboBoxSize(comboBoxDimension, startTimePicker);
        setComboBoxSize(comboBoxDimension, stopTimePicker);

        datePatternComboBox = new JComboBox();
        setComboBoxSize(comboBoxDimension, datePatternComboBox);
        datePatternComboBox.setEditable(true);

        fileNamePatternComboBox = new JComboBox();
        setComboBoxSize(comboBoxDimension, fileNamePatternComboBox);
        fileNamePatternComboBox.setEditable(true);

        initPatterns();

        final UIUpdater uiUpdater = new UIUpdater();
        final TimePickerValidator timePickerValidator = new TimePickerValidator();
        filterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUIState();
                if (timeStampExtractor != null) {
                    fireFilterChangedEvent();
                }
            }
        });
        startTimePicker.addActionListener(uiUpdater);
        startTimePicker.addActionListener(timePickerValidator);
        stopTimePicker.addActionListener(uiUpdater);
        startTimePicker.addActionListener(timePickerValidator);
        datePatternComboBox.addActionListener(uiUpdater);
        fileNamePatternComboBox.addActionListener(uiUpdater);

        listeners = new ArrayList<FilterChangeListener>();

        applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeStampExtractor = new TimeStampExtractor(datePatternComboBox.getSelectedItem().toString(),
                        fileNamePatternComboBox.getSelectedItem().toString());
                startDate = startTimePicker.getDate();
                endDate = stopTimePicker.getDate();
                updateUIState();
                applyButton.setEnabled(false);
                fireFilterChangedEvent();
            }
        });
        updateUIState();
    }

    private void updateUIState() {
        final boolean isSelected = filterCheckBox.isSelected();
        datePatternComboBox.setEnabled(isSelected);
        fileNamePatternComboBox.setEnabled(isSelected);
        startTimePicker.setEnabled(isSelected);
        stopTimePicker.setEnabled(isSelected);
        final String datePattern = datePatternComboBox.getSelectedItem().toString();
        final String fileNamePattern = fileNamePatternComboBox.getSelectedItem().toString();
        final boolean hasStartDate = startTimePicker.getDate() != null;
        final boolean hasEndDate = stopTimePicker.getDate() != null;
        final boolean patternProvided = !("".equals(datePattern) || "".equals(fileNamePattern));
        if (!isSelected || (!patternProvided && (hasStartDate || hasEndDate))) {
            applyButton.setEnabled(false);
        } else {
            applyButton.setEnabled(true);
        }
    }

    private void initPatterns() {
        for (String datePattern : PatternProvider.DATE_PATTERNS) {
            datePatternComboBox.addItem(datePattern);
        }
        for (String fileNamePattern : PatternProvider.FILENAME_PATTERNS) {
            fileNamePatternComboBox.addItem(fileNamePattern);
        }
    }

    private void setComboBoxSize(Dimension comboBoxDimension, JComboBox comboBox) {
        comboBox.setPreferredSize(comboBoxDimension);
        comboBox.setMinimumSize(comboBoxDimension);
    }

    @Override
    public JComponent getUI() {

        final JPanel filterUI = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.insets.bottom = 4;
        gbc.insets.right = 4;
        gbc.anchor = GridBagConstraints.WEST;

        filterUI.add(new JLabel("Date pattern:"), gbc);
        gbc.gridx++;

        filterUI.add(datePatternComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        filterUI.add(new JLabel("Filename pattern:"), gbc);
        gbc.gridx++;
        filterUI.add(fileNamePatternComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        filterUI.add(new JLabel("Start date:"), gbc);
        gbc.gridx++;
        filterUI.add(startTimePicker, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        filterUI.add(new JLabel("Stop date:"), gbc);

        gbc.gridx++;
        filterUI.add(stopTimePicker, gbc);
        gbc.gridy++;

        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        filterUI.add(applyButton, gbc);

        return filterUI;
    }

    @Override
    public boolean accept(OpendapLeaf leaf) {
        try {
            final ProductData.UTC[] timeStamps = timeStampExtractor.extractTimeStamps(leaf.getName());

            final boolean startDateEqualsEndDate = timeStamps[0].getAsDate().getTime() == timeStamps[1].getAsDate().getTime();
            if (startDateEqualsEndDate) {
                timeStamps[1] = null;
            }

            boolean fileHasEndDate = timeStamps[1] != null;
            boolean userHasStartDate = startDate != null;
            boolean userHasEndDate = endDate != null;

            if (userHasStartDate) {
                if (startDate.after(timeStamps[0].getAsDate())) {
                    return false;
                }
            }

            if (userHasEndDate) {
                if (fileHasEndDate) {
                    if (endDate.before(timeStamps[1].getAsDate())) {
                        return false;
                    }
                } else {
                    if (endDate.before(timeStamps[0].getAsDate())) {
                        return false;
                    }
                }
            }
            return true;

        } catch (ValidationException e) {
            return true;
        }
    }

    @Override
    public void addFilterChangeListener(FilterChangeListener listener) {
        listeners.add(listener);
    }

    private void fireFilterChangedEvent() {
        for (FilterChangeListener listener : listeners) {
            listener.filterChanged();
        }
    }

    private class UIUpdater implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            updateUIState();
        }
    }

    private class TimePickerValidator implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Date startDate = startTimePicker.getDate();
            Date endDate = stopTimePicker.getDate();
            if (startDate == null || endDate == null) {
                return;
            }
            if (startDate.after(endDate)) {
                if (e.getSource().equals(startTimePicker)) {
                    startTimePicker.setDate(endDate);
                } else if (e.getSource().equals(stopTimePicker)) {
                    stopTimePicker.setDate(startDate);
                }
                // todo - log warning
            }
        }
    }
}
