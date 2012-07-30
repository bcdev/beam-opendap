package org.esa.beam.opendap.ui;

import com.bc.ceres.core.ProgressBarProgressMonitor;
import com.jidesoft.combobox.FolderChooserExComboBox;
import com.jidesoft.combobox.PopupPanel;
import com.jidesoft.status.LabelStatusBarItem;
import com.jidesoft.status.ProgressStatusBarItem;
import com.jidesoft.status.StatusBar;
import com.jidesoft.swing.FolderChooser;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.utils.Lm;
import org.esa.beam.framework.gpf.ui.DefaultAppContext;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;
import org.esa.beam.opendap.OpendapLeaf;
import org.esa.beam.opendap.utils.OpendapUtils;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.util.StringUtils;
import org.esa.beam.util.logging.BeamLogManager;
import org.esa.beam.visat.VisatApp;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpendapAccessPanel extends JPanel {

    private static final String PROPERTY_KEY_SERVER_URLS = "opendap.server.urls";
    private final static int DDS_AREA_INDEX = 0;
    private final static int DAS_AREA_INDEX = 1;
    private static final Integer GENERAL_AREA_INDEX = 2;

    private JComboBox urlField;
    private AbstractButton refreshButton;
    private CatalogTree catalogTree;

    private JTabbedPane metaInfoArea;
    private JCheckBox useDatasetNameFilter;

    private FilterComponent datasetNameFilter;
    private JCheckBox useTimeRangeFilter;

    private FilterComponent timeRangeFilter;
    private JCheckBox useRegionFilter;

    private FilterComponent regionFilter;
    private JCheckBox useVariableFilter;

    private VariableFilter variableFilter;

    private JCheckBox openInVisat;
    private StatusBar statusBar;

    private double currentDataSize = 0.0;
    private final PropertyMap propertyMap;
    private final String helpId;
    private FolderChooserExComboBox folderChooserComboBox;
    private JProgressBar progressBar;
    private JLabel preMessageLabel;
    private JLabel postMessageLabel;
    private Map<Integer,JTextArea> textAreas;
    private JButton downloadButton;
    private AppContext appContext;

    public static void main(String[] args) {
        Lm.verifyLicense("Brockmann Consult", "BEAM", "lCzfhklpZ9ryjomwWxfdupxIcuIoCxg2");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final OpendapAccessPanel opendapAccessPanel = new OpendapAccessPanel(new DefaultAppContext(""), "");
        final JFrame mainFrame = new JFrame("OPeNDAP Access");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setContentPane(opendapAccessPanel);
        mainFrame.pack();
        final Dimension size = mainFrame.getSize();
        mainFrame.setMinimumSize(size);
        mainFrame.setVisible(true);
    }

    public OpendapAccessPanel(AppContext appContext, String helpId) {
        super();
        this.propertyMap = appContext.getPreferences();
        this.helpId = helpId;
        this.appContext = appContext;
        initComponents();
        initContentPane();
    }

    private void initComponents() {
        urlField = new JComboBox();
        urlField.setEditable(true);
        urlField.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    refreshButton.doClick();
                }
            }
        });
        updateUrlField();
        refreshButton = ToolButtonFactory.createButton(
                UIUtils.loadImageIcon("/org/esa/beam/opendap/images/icons/ViewRefresh22.png", OpendapAccessPanel.class),
                false);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean usingUrl = refresh();
                if (usingUrl) {
                    final String urls = propertyMap.getPropertyString(PROPERTY_KEY_SERVER_URLS);
                    final String currentUrl = urlField.getSelectedItem().toString();
                    if (!urls.contains(currentUrl)) {
                        propertyMap.setPropertyString(PROPERTY_KEY_SERVER_URLS, urls + "\n" + currentUrl);
                        updateUrlField();
                    }
                }
            }
        });
        metaInfoArea = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        JTextArea ddsArea = new JTextArea(10, 40);
        JTextArea dasArea = new JTextArea(10, 40);
        JTextArea generalArea = new JTextArea(10, 40);

        ddsArea.setEditable(false);
        dasArea.setEditable(false);
        generalArea.setEditable(false);

        textAreas = new HashMap<Integer, JTextArea>();
        textAreas.put(DAS_AREA_INDEX, dasArea);
        textAreas.put(DDS_AREA_INDEX, ddsArea);
        textAreas.put(GENERAL_AREA_INDEX, generalArea);

        metaInfoArea.addTab("DDS", new JScrollPane(ddsArea));
        metaInfoArea.addTab("DAS", new JScrollPane(dasArea));
        metaInfoArea.addTab("General Info", new JScrollPane(generalArea));

        metaInfoArea.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (catalogTree.getSelectedLeaf() != null) {
                    setMetadataText(metaInfoArea.getSelectedIndex(), catalogTree.getSelectedLeaf());
                }
            }
        });

        catalogTree = new CatalogTree(new CatalogTree.LeafSelectionListener() {

            @Override
            public void dapLeafSelected(OpendapLeaf leaf) {
                setMetadataText(metaInfoArea.getSelectedIndex(), leaf);
            }

            @Override
            public void fileLeafSelected(OpendapLeaf leaf) {
                setMetadataText(metaInfoArea.getSelectedIndex(), leaf);
            }

            @Override
            public void leafSelectionChanged(boolean isSelected, OpendapLeaf dapObject) {
                int dataSize = dapObject.getFileSize();
                currentDataSize += isSelected ? dataSize : -dataSize;
                if (currentDataSize <= 0) {
                    updateStatusBar("Ready.");
                    downloadButton.setEnabled(false);
                } else {
                    downloadButton.setEnabled(true);
                    double dataSizeInMB = currentDataSize / (1024.0 * 1024.0);
                    updateStatusBar(
                            "Total size of currently selected files: " + OpendapUtils.format(dataSizeInMB) + " MB");
                }
            }
        }, appContext);
        useDatasetNameFilter = new JCheckBox("Use dataset name filter");
        useTimeRangeFilter = new JCheckBox("Use time range filter");
        useRegionFilter = new JCheckBox("Use region filter");
        useVariableFilter = new JCheckBox("Use variable name filter");

        DefaultFilterChangeListener filterChangeListener = new DefaultFilterChangeListener();
        datasetNameFilter = new DatasetNameFilter(useDatasetNameFilter);
        datasetNameFilter.addFilterChangeListener(filterChangeListener);
        timeRangeFilter = new TimeRangeFilter(useTimeRangeFilter);
        timeRangeFilter.addFilterChangeListener(filterChangeListener);
        regionFilter = new RegionFilter(useRegionFilter);
        regionFilter.addFilterChangeListener(filterChangeListener);
        variableFilter = new VariableFilter(useVariableFilter, catalogTree);
        variableFilter.addFilterChangeListener(filterChangeListener);

        catalogTree.addCatalogTreeListener(new CatalogTree.CatalogTreeListener() {
            @Override
            public void leafAdded(OpendapLeaf leaf, boolean hasNestedDatasets) {
                if (hasNestedDatasets) {
                    return;
                }
                if (leaf.getDataset().getGeospatialCoverage() != null) {
                    useRegionFilter.setEnabled(true);
                }
                filterLeaf(leaf);
            }

            @Override
            public void catalogElementsInsertionFinished() {
            }
        });

        openInVisat = new JCheckBox("Open in VISAT");
        statusBar = new StatusBar();
        final LabelStatusBarItem message = new LabelStatusBarItem("label");
        message.setText("Ready.");
        message.setAlignment(JLabel.LEFT);
        statusBar.add(message, JideBoxLayout.FLEXIBLE);

        preMessageLabel = new JLabel();
        postMessageLabel = new JLabel();
        final LabelStatusBarItem preMessage = new LabelStatusBarItem() {
            @Override
            protected JLabel createLabel() {
                return preMessageLabel;
            }
        };
        final LabelStatusBarItem postMessage = new LabelStatusBarItem() {
            @Override
            protected JLabel createLabel() {
                return postMessageLabel;
            }
        };

        statusBar.add(preMessage, JideBoxLayout.FIX);

        ProgressStatusBarItem progressBarItem = new ProgressStatusBarItem();
        progressBarItem.setProgress(0);
        progressBar = progressBarItem.getProgressBar();

        statusBar.add(progressBarItem, JideBoxLayout.FIX);

        statusBar.add(postMessage, JideBoxLayout.FIX);

        useRegionFilter.setEnabled(false);
    }


    private void setMetadataText(int componentIndex, OpendapLeaf leaf) {
        String text = null;
        try {
            if (leaf.isDapAccess()) {
                if (metaInfoArea.getSelectedIndex() == DDS_AREA_INDEX) {
                    text = OpendapUtils.getResponse(leaf.getDdsUri());
                } else if (metaInfoArea.getSelectedIndex() == DAS_AREA_INDEX) {
                    text = OpendapUtils.getResponse(leaf.getDasUri());
                } else {
                    text = OpendapUtils.getResponse(leaf.getDdsUri());
                }
            } else if (leaf.isFileAccess()) {
                if (metaInfoArea.getSelectedIndex() == DDS_AREA_INDEX) {
                    text = "No DDS information for file '" + leaf.getName() + "'.";
                } else if (metaInfoArea.getSelectedIndex() == DAS_AREA_INDEX) {
                    text = "No DAS information for file '" + leaf.getName() + "'.";
                } else {
                    text = OpendapUtils.getResponse(leaf.getFileUri());
                }
            }
        } catch (IOException e) {
            BeamLogManager.getSystemLogger().warning("Unable to retrieve meta information for file '" + leaf.getName() + "'.");
        }

        setResponseText(componentIndex, text);
    }

    private void setResponseText(int componentIndex, String response) {
        JTextArea textArea = textAreas.get(componentIndex);
        if (response.length() > 100000) {
            StringBuilder responseBuilder = new StringBuilder(response.substring(0, 10000));
            responseBuilder.append("\n" + "Cut remaining file content");
            response = responseBuilder.toString();
        }
        textArea.setText(response);
        textArea.setCaretPosition(0);
    }

    private void updateStatusBar(String message) {
        LabelStatusBarItem messageItem = (LabelStatusBarItem) statusBar.getItemByName("label");
        messageItem.setText(message);
    }

    private void filterLeaf(OpendapLeaf leaf) {
        if (
                (!useDatasetNameFilter.isSelected() || datasetNameFilter.accept(leaf)) &&
                (!useTimeRangeFilter.isSelected() || timeRangeFilter.accept(leaf)) &&
                (!useRegionFilter.isSelected() || regionFilter.accept(leaf)) &&
                (!useVariableFilter.isSelected() || variableFilter.accept(leaf))) {
            catalogTree.setLeafVisible(leaf, true);
        } else {
            catalogTree.setLeafVisible(leaf, false);
        }
    }

    private void updateUrlField() {
        final String urlsProperty = propertyMap.getPropertyString(PROPERTY_KEY_SERVER_URLS);
        final String[] urls = urlsProperty.split("\n");
        for (String url : urls) {
            if (StringUtils.isNotNullAndNotEmpty(url) && !contains(urlField, url)) {
                urlField.addItem(url);
            }
        }
    }

    private boolean contains(JComboBox urlField, String url) {
        for (int i = 0; i < urlField.getItemCount(); i++) {
            if (urlField.getItemAt(i).toString().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private void initContentPane() {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets.right = 5;
        final JPanel urlPanel = new JPanel(layout);
        urlPanel.add(new JLabel("Root URL:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        urlPanel.add(urlField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        urlPanel.add(refreshButton, gbc);
        gbc.gridx = 3;
        gbc.insets.right = 0;
        final AbstractButton helpButton = ToolButtonFactory.createButton(
                UIUtils.loadImageIcon("/org/esa/beam/opendap/images/icons/Help22.png", OpendapAccessPanel.class),
                false);
        HelpSys.enableHelpOnButton(helpButton, helpId);
        urlPanel.add(helpButton, gbc);

        final JPanel variableInfo = new JPanel(new BorderLayout(5, 5));
        variableInfo.setBorder(new EmptyBorder(10, 0, 0, 0));
        variableInfo.add(metaInfoArea, BorderLayout.CENTER);

        final JScrollPane openDapTree = new JScrollPane(catalogTree.getComponent());
        openDapTree.setPreferredSize(new Dimension(400, 500));

        final JSplitPane centerLeftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, openDapTree, variableInfo);
        centerLeftPane.setResizeWeight(1);
        centerLeftPane.setContinuousLayout(true);

        final JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.add(new TitledPanel(useDatasetNameFilter, datasetNameFilter.getUI()));
        filterPanel.add(new TitledPanel(useTimeRangeFilter, timeRangeFilter.getUI()));
        filterPanel.add(new TitledPanel(useRegionFilter, regionFilter.getUI()));
        filterPanel.add(new TitledPanel(useVariableFilter, variableFilter.getUI()));
        final Dimension size = filterPanel.getSize();
        filterPanel.setMinimumSize(new Dimension(460, size.height));

        final JPanel optionalPanel = new TitledPanel(null, null);

        final JPanel downloadButtonPanel = new JPanel(new BorderLayout(8, 5));
        downloadButtonPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        final DownloadProgressBarProgressMonitor pm = new DownloadProgressBarProgressMonitor(progressBar, preMessageLabel, postMessageLabel);
        progressBar.setVisible(false);
        folderChooserComboBox = new FolderChooserExComboBox() {
            @Override
            public PopupPanel createPopupComponent() {
                final PopupPanel popupComponent = super.createPopupComponent();
                final JScrollPane content = (JScrollPane) popupComponent.getComponents()[0];
                final JComponent upperPane = (JComponent) content.getComponents()[0];
                FolderChooser folderChooser = (FolderChooser) upperPane.getComponents()[0];
                folderChooser.setRecentListVisible(false);
                popupComponent.setTitle("Choose download directory");
                return popupComponent;
            }
        };
        downloadButton = new JButton("Download");
        downloadButton.setEnabled(false);
        downloadButton.addActionListener(createDownloadAction(pm));
        folderChooserComboBox.setEditable(true);
        if (VisatApp.getApp() != null) {
            downloadButtonPanel.add(openInVisat, BorderLayout.NORTH);
        }
        downloadButtonPanel.add(folderChooserComboBox);
        downloadButtonPanel.add(downloadButton, BorderLayout.EAST);

        final JPanel centerRightPane = new JPanel(new BorderLayout());
        centerRightPane.add(filterPanel, BorderLayout.NORTH);
        centerRightPane.add(optionalPanel, BorderLayout.CENTER);
        centerRightPane.add(downloadButtonPanel, BorderLayout.SOUTH);

        final JSplitPane centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerLeftPane, centerRightPane);
        centerPanel.setResizeWeight(1);
        centerPanel.setContinuousLayout(true);

        this.setLayout(new BorderLayout(15, 15));
        this.setBorder(new EmptyBorder(8, 8, 8, 8));
        this.add(urlPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(statusBar, BorderLayout.SOUTH);
    }

    private DownloadAction createDownloadAction(DownloadProgressBarProgressMonitor pm) {
        return new DownloadAction(pm, new ParameterProviderImpl(), new DownloadAction.DownloadHandler() {

            @Override
            public void handleException(Exception e) {
                appContext.handleError("Unable to perform download. Reason: " + e.getMessage(), e);
            }

            @Override
            public void handleDownloadFinished(File[] downloadedFiles) {
                if (openInVisat.isSelected()) {
                    for (File file : downloadedFiles) {
                        VisatApp.getApp().openProduct(file);
                    }
                }
            }
        });
    }

    private File fetchTargetDirectory() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Target Directory");
        final int i = chooser.showDialog(null, "Save to directory");
        if (i == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private boolean refresh() {
        String url;
        if (urlField.getSelectedItem() == null) {
            url = urlField.getEditor().getItem().toString();
        } else {
            url = urlField.getSelectedItem().toString();
        }
        final String catalogUrl;
        if (!url.toLowerCase().endsWith("/catalog.xml")) {
            catalogUrl = url + "/catalog.xml";
        } else {
            catalogUrl = url;
        }
        final InvCatalogFactory factory = InvCatalogFactory.getDefaultFactory(true);
        final InvCatalog catalog = factory.readXML(catalogUrl);
        final List<InvDataset> datasets = catalog.getDatasets();

        if (datasets.size() == 0) {
            JOptionPane.showMessageDialog(this, "'" + url + "' is not a valid OPeNDAP URL.");
            return false;
        }

        catalogTree.setNewRootDatasets(datasets);
        variableFilter.stopFiltering();
        return true;
    }

    private class DefaultFilterChangeListener implements FilterChangeListener {

        @Override
        public void filterChanged() {
            final OpendapLeaf[] leaves = catalogTree.getLeaves();
            for (OpendapLeaf leaf : leaves) {
                filterLeaf(leaf);
            }
        }

    }

    public static class DownloadProgressBarProgressMonitor extends ProgressBarProgressMonitor implements
                                                                                              LabelledProgressBarPM {

        private final JProgressBar progressBar;
        private final JLabel preMessageLabel;
        private JLabel postMessageLabel;
        private int totalWork;
        private int currentWork;
        private long startTime;

        public DownloadProgressBarProgressMonitor(JProgressBar progressBar, JLabel preMessageLabel, JLabel postMessageLabel) {
            super(progressBar, preMessageLabel);
            this.progressBar = progressBar;
            this.preMessageLabel = preMessageLabel;
            this.postMessageLabel = postMessageLabel;
        }

        @Override
        public void setPreMessage(String preMessageText) {
            setTaskName(preMessageText);
        }

        @Override
        public void setPostMessage(String postMessageText) {
            postMessageLabel.setText(postMessageText);
        }

        @Override
        public void setTooltip(String tooltip) {
            preMessageLabel.setToolTipText(tooltip);
            postMessageLabel.setToolTipText(tooltip);
            progressBar.setToolTipText(tooltip);
        }

        @Override
        public void beginTask(String name, int totalWork) {
            super.beginTask(name, totalWork);
            this.totalWork = totalWork;
            this.currentWork = 0;
            progressBar.setValue(0);
        }

        @Override
        public void worked(int work) {
            super.worked(work);
            currentWork += work;
        }

        @Override
        protected void setDescription(String description) {
        }

        @Override
        protected void setVisibility(boolean visible) {
            // once the progress bar has been made visible, it shall always be visible
            progressBar.setVisible(true);
        }

        @Override
        protected void setRunning() {
        }

        @Override
        protected void finish() {
        }

        @Override
        public int getTotalWork() {
            return totalWork;
        }

        @Override
        public int getCurrentWork() {
            return currentWork;
        }

        public void updateTask(int additionalWork) {
            totalWork += additionalWork;
            progressBar.setMaximum(totalWork);
            progressBar.updateUI();
        }

        public void resetStartTime() {
            GregorianCalendar gc = new GregorianCalendar();
            startTime = gc.getTimeInMillis();
        }

        public long getStartTime() {
            return startTime;
        }
    }

    private class ParameterProviderImpl implements DownloadAction.ParameterProvider {

        List<String> dapURIs = new ArrayList<String>();
        List<String> fileURIs = new ArrayList<String>();

        @Override
        public List<String> getDapURIs() {
            if (dapURIs.isEmpty() && fileURIs.isEmpty()) {
                collectURIs();
            }
            return new ArrayList<String>(dapURIs);
        }

        @Override
        public List<String> getFileURIs() {
            if (dapURIs.isEmpty() && fileURIs.isEmpty()) {
                collectURIs();
            }
            return new ArrayList<String>(fileURIs);
        }

        private void collectURIs() {
            final TreePath[] selectionPaths = ((JTree) catalogTree.getComponent()).getSelectionModel().getSelectionPaths();
            if (selectionPaths == null || selectionPaths.length <= 0) {
                return;
            }

            for (TreePath selectionPath : selectionPaths) {
                final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                if (CatalogTree.isDapNode(treeNode) || CatalogTree.isFileNode(treeNode)) {
                    final OpendapLeaf leaf = (OpendapLeaf) treeNode.getUserObject();
                    if (leaf.isDapAccess()) {
                        dapURIs.add(leaf.getDapUri());
                    } else if (leaf.isFileAccess()) {
                        fileURIs.add(leaf.getFileUri());
                    }
                }
            }
        }

        @Override
        public void reset() {
            dapURIs.clear();
            fileURIs.clear();
        }

        @Override
        public int getDatasize() {
            return (int) currentDataSize;
        }

        @Override
        public File getTargetDirectory() {
            final File targetDirectory;
            if (folderChooserComboBox.getSelectedItem() == null ||
                folderChooserComboBox.getSelectedItem().toString().equals("")) {
                targetDirectory = fetchTargetDirectory();
            } else {
                targetDirectory = new File(folderChooserComboBox.getSelectedItem().toString());
            }
            return targetDirectory;
        }
    }

}