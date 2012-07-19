package org.esa.beam.opendap.ui;

import com.bc.ceres.core.ProgressBarProgressMonitor;
import com.bc.ceres.core.ProgressMonitor;
import com.jidesoft.list.CheckBoxListSelectionModelWithWrapper;
import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import org.esa.beam.framework.ui.GridBagUtils;
import org.esa.beam.opendap.DAPVariable;
import org.esa.beam.opendap.OpendapLeaf;
import org.esa.beam.opendap.utils.VariableCollector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class VariableFilter implements FilterComponent, CatalogTree.CatalogTreeListener {

    private static final int MAX_THREAD_COUNT = 10;
    private final JCheckBox filterCheckBox;
    private VariableCollector collector = new VariableCollector();

    private FilterListModel listModel;
    private JButton selectAllButton;
    private JButton selectNoneButton;
    private JButton applyButton;
    private FilterableCheckBoxList checkBoxList;
    private QuickListFilterField field;
    private List<FilterChangeListener> listeners;
    private final HashSet<VariableFilterPreparator> filterPreparators = new HashSet<VariableFilterPreparator>();
    private final List<VariableFilterPreparator> filterPreparatorsInWait = new ArrayList<VariableFilterPreparator>();
    private ProgressMonitor pm;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private int totalWork;
    private int worked;

    public VariableFilter(JCheckBox filterCheckBox, CatalogTree catalogTree) {
        this.filterCheckBox = filterCheckBox;
        catalogTree.addCatalogTreeListener(this);
        listeners = new ArrayList<FilterChangeListener>();
    }

    @Override
    public JComponent getUI() {
        JPanel panel = GridBagUtils.createPanel();
        initComponents();
        configureComponents();
        addComponents(panel);
        updateUI(false, false, false);
        return panel;
    }

    private void configureComponents() {
        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int variableCount = checkBoxList.getModel().getSize();
                int[] selectedIndices = new int[variableCount];
                for (int i = 0; i < variableCount; i++) {
                    selectedIndices[i] = i;
                }
                checkBoxList.setCheckBoxListSelectedIndices(selectedIndices);
                updateUI(true, false, true);
            }
        });
        selectNoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkBoxList.setCheckBoxListSelectedIndices(new int[0]);
                updateUI(true, true, false);
            }
        });
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireFilterChanged();
                updateUI(false, selectAllButton.isEnabled(), selectNoneButton.isEnabled());
            }
        });
        filterCheckBox.setEnabled(false);
        filterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean useFilter = filterCheckBox.isSelected();
                fireFilterChanged();
                updateUI(useFilter, useFilter, useFilter);
            }
        });
        checkBoxList.getCheckBoxListSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                CheckBoxListSelectionModelWithWrapper model = (CheckBoxListSelectionModelWithWrapper) e.getSource();
                int anchorSelectionIndex = model.getAnchorSelectionIndex();
                if (e.getValueIsAdjusting() || anchorSelectionIndex == -1) {
                    return;
                }
                for (int i = 0; i < listModel.getSize(); i++) {
                    DAPVariable variable = (DAPVariable) listModel.getElementAt(i);
                    DAPVariable currentVariable = (DAPVariable) model.getModel().getElementAt(anchorSelectionIndex);
                    if (variable.equals(currentVariable)) {
                        boolean isSelected = model.isSelectedIndex(anchorSelectionIndex);
                        setVariableSelected(currentVariable, isSelected);
                    }
                }
                updateUI(true, true, true);
            }
        });

        listModel.addListDataListener(field.getDisplayListModel());
        Font font = selectAllButton.getFont().deriveFont(10.0F);
        selectAllButton.setFont(font);
        selectNoneButton.setFont(font);
        field.setHintText("Type here to filter variables");
        setProgressComponentsVisible(false);
    }

    private void addComponents(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();

        JScrollPane scrollPane = new JScrollPane(checkBoxList);
        scrollPane.setPreferredSize(new Dimension(250, 100));
        GridBagUtils.addToPanel(panel, progressBar, gbc, "insets.top=5, gridx=0, gridy=0, gridwidth=2, anchor=WEST, fill=HORIZONTAL, weightx=1.0");
        GridBagUtils.addToPanel(panel, statusLabel, gbc, "gridx=2, anchor=EAST, gridwidth=1, fill=NONE, weightx=0.0");
        GridBagUtils.addToPanel(panel, field, gbc, "gridx=0, gridy=1, anchor=WEST, gridwidth=3, fill=HORIZONTAL, weightx=1.0");
        GridBagUtils.addToPanel(panel, scrollPane, gbc, "gridy=2");
        GridBagUtils.addToPanel(panel, selectAllButton, gbc, "insets.right=5, gridy=3, gridwidth=1, fill=NONE, weightx=0");
        GridBagUtils.addToPanel(panel, selectNoneButton, gbc, "gridx=1");
        GridBagUtils.addToPanel(panel, applyButton, gbc, "insets.right=0, gridx=2, gridy=4, anchor=EAST");
    }

    private void initComponents() {
        applyButton = new JButton("Apply");
        selectAllButton = new JButton("Select all");
        selectNoneButton = new JButton("Select none");
        listModel = new FilterListModel();
        field = new QuickListFilterField(listModel);
        checkBoxList = new FilterableCheckBoxList(field.getDisplayListModel());
        progressBar = new JProgressBar();
        statusLabel = new JLabel();
        pm = new VariableFilterProgressBarProgressMonitor(progressBar, statusLabel);
    }

    @Override
    public boolean accept(OpendapLeaf leaf) {
        DAPVariable[] dapVariables = leaf.getDAPVariables();
        if (noVariablesAreSelected()) {
            return true;
        }
        for (DAPVariable dapVariable : dapVariables) {
            Boolean isSelected = listModel.variableToSelected.get(dapVariable);
            boolean leafContainsVariable = isSelected == null ? false : isSelected;
            if (leafContainsVariable) {
                return true;
            }
        }
        return false;
    }

    private boolean noVariablesAreSelected() {
        for (Boolean selected : listModel.variableToSelected.values()) {
            if (selected) {
                return false;
            }
        }
        return true;
    }

    private void updateUI(boolean enableApplyButton, boolean enableSelectAllButton, boolean enableSelectNoneButton) {
        boolean notAllSelected = checkBoxList.getModel().getSize() == 0 ||
                                 checkBoxList.getCheckBoxListSelectedIndices().length <
                                 checkBoxList.getModel().getSize();
        boolean someSelected = checkBoxList.getCheckBoxListSelectedIndices().length > 0;
        boolean filtersAvailable = checkBoxList.getModel().getSize() > 0;

        selectAllButton.setEnabled(filterCheckBox.isSelected() && enableSelectAllButton && notAllSelected);
        selectNoneButton.setEnabled(filterCheckBox.isSelected() && enableSelectNoneButton && someSelected);
        applyButton.setEnabled(filterCheckBox.isSelected() && enableApplyButton && filtersAvailable);
        checkBoxList.setEnabled(filterCheckBox.isSelected());
        field.setEnabled(filterCheckBox.isSelected());
    }

    @Override
    public void addFilterChangeListener(FilterChangeListener listener) {
        listeners.add(listener);
    }

    private void fireFilterChanged() {
        for (FilterChangeListener listener : listeners) {
            listener.filterChanged();
        }
    }

    public void addVariable(DAPVariable dapVariable) {
        listModel.allVariables.add(dapVariable);
    }

    public void setVariableSelected(DAPVariable dapVariable, boolean selected) {
        listModel.variableToSelected.put(dapVariable, selected);
    }

    private static class FilterListModel implements ListModel {

        private SortedSet<DAPVariable> allVariables = new TreeSet<DAPVariable>();
        private Map<DAPVariable, Boolean> variableToSelected = new HashMap<DAPVariable, Boolean>();
        private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

        void addVariables(DAPVariable[] dapVariables) {
            allVariables.addAll(Arrays.asList(dapVariables));
            for (ListDataListener listener : listeners) {
                listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() - 1));
            }
        }

        @Override
        public int getSize() {
            return allVariables.size();
        }

        @Override
        public Object getElementAt(int index) {
            return allVariables.toArray()[index];
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
    }

    @Override
    public void leafAdded(OpendapLeaf leaf, boolean hasNestedDatasets) {
        VariableFilterPreparator filterPreparator = new VariableFilterPreparator(leaf);

        if (totalWork > 0) {
            setProgressComponentsVisible(true);
        }

        if (filterPreparators.size() <= MAX_THREAD_COUNT) {
            filterPreparators.add(filterPreparator);
            filterPreparator.execute();
            filterCheckBox.setEnabled(false);
            filterCheckBox.setSelected(false);
            updateUI(false, false, false);
        } else {
            filterPreparatorsInWait.add(filterPreparator);
        }

        totalWork++;
    }

    @Override
    public void catalogElementsInsertionFinished() {
        pm.setTaskName("");
        pm.beginTask("", totalWork);
        pm.worked(worked);
    }

    private class VariableFilterPreparator extends SwingWorker<DAPVariable[], Void> {

        private OpendapLeaf leaf;

        private VariableFilterPreparator(OpendapLeaf leaf) {
            this.leaf = leaf;
        }

        @Override
        protected DAPVariable[] doInBackground() throws Exception {
            DAPVariable[] leafVariables = collector.collectDAPVariables(leaf);
            leaf.addDAPVariables(leafVariables);
            return leafVariables;
        }

        @Override
        protected void done() {
            try {
                DAPVariable[] dapVariables = get();
                listModel.addVariables(dapVariables);
            } catch (InterruptedException e) {
                // todo - implement
                e.printStackTrace();
            } catch (ExecutionException e) {
                // todo - implement
                e.printStackTrace();
            } finally {
                filterPreparators.remove(this);
                pm.worked(1);
                worked++;
                if (!filterPreparatorsInWait.isEmpty()) {
                    VariableFilterPreparator nextFilterPreparator = filterPreparatorsInWait.remove(0);
                    filterPreparators.add(nextFilterPreparator);
                    nextFilterPreparator.execute();
                }
                int percentage = (int) (((double) worked / (double) totalWork) * 100);
                pm.setTaskName(percentage + " %");
                if (filterPreparators.isEmpty()) {
                    updateUI(true, true, true);
                    filterCheckBox.setEnabled(true);
                    pm.done();
                    setProgressComponentsVisible(false);
                    worked = 0;
                    totalWork = 0;
                }
            }
        }
    }

    private void setProgressComponentsVisible(boolean visible) {
        progressBar.setVisible(visible);
        statusLabel.setVisible(visible);
    }

    private static class VariableFilterProgressBarProgressMonitor extends ProgressBarProgressMonitor {

        public VariableFilterProgressBarProgressMonitor(JProgressBar progressBar, JLabel messageLabel) {
            super(progressBar, messageLabel);
        }

        @Override
        protected void setDescription(String description) {
        }

        @Override
        protected void setVisibility(boolean visible) {
        }

        @Override
        protected void setRunning() {
        }

        @Override
        protected void finish() {
        }
    }

}
