package org.esa.beam.opendap.ui;

import com.bc.ceres.core.ProgressBarProgressMonitor;
import com.bc.ceres.core.ProgressMonitor;
import com.jidesoft.combobox.FolderChooserExComboBox;
import com.jidesoft.combobox.PopupPanel;
import com.jidesoft.status.LabelStatusBarItem;
import com.jidesoft.status.ProgressStatusBarItem;
import com.jidesoft.status.StatusBar;
import com.jidesoft.swing.FolderChooser;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.utils.Lm;
import org.esa.beam.opendap.OpendapLeaf;
import org.esa.beam.opendap.utils.DAPDownloader;
import org.esa.beam.opendap.utils.OpendapUtils;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.util.StringUtils;
import org.esa.beam.visat.VisatApp;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;

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
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class OpendapAccessPanel extends JPanel {

    private static final String PROPERTY_KEY_SERVER_URLS = "opendap.server.urls";

    private JComboBox urlField;
    private JButton refreshButton;
    private CatalogTree catalogTree;
    private JTextArea dapResponseArea;

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
    private FolderChooserExComboBox folderChooserComboBox;
    private JProgressBar progressBar;

    public static void main(String[] args) {
        Lm.verifyLicense("Brockmann Consult", "BEAM", "lCzfhklpZ9ryjomwWxfdupxIcuIoCxg2");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final OpendapAccessPanel opendapAccessPanel = new OpendapAccessPanel(new PropertyMap());
        final JFrame mainFrame = new JFrame("OPeNDAP Access");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setContentPane(opendapAccessPanel);
        mainFrame.pack();
        final Dimension size = mainFrame.getSize();
        mainFrame.setMinimumSize(size);
        mainFrame.setVisible(true);
    }

    public OpendapAccessPanel(PropertyMap propertyMap) {
        super();
        this.propertyMap = propertyMap;
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
        refreshButton = new JButton("Refresh");
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
        dapResponseArea = new JTextArea(10, 40);
        catalogTree = new CatalogTree(new CatalogTree.LeafSelectionListener() {

            @Override
            public void dapLeafSelected(OpendapLeaf leaf) {
                setResponseAreaText(leaf.getDdsUri());
            }

            @Override
            public void fileLeafSelected(OpendapLeaf leaf) {
                setResponseAreaText(leaf.getFileUri());
            }

            @Override
            public void leafSelectionChanged(boolean isSelected, OpendapLeaf dapObject) {
                double dataSize = dapObject.getFileSize();
                currentDataSize += isSelected ? dataSize : -dataSize;
                if (currentDataSize <= 0) {
                    updateStatusBar("Ready.");
                } else {
                    DecimalFormatSymbols formatSymbols = DecimalFormatSymbols.getInstance();
                    formatSymbols.setDecimalSeparator('.');
                    DecimalFormat decimalFormat = new DecimalFormat("0.00", formatSymbols);
                    updateStatusBar("Total size of currently selected files: " + decimalFormat.format(currentDataSize) + " MB");
                }
            }

            private void setResponseAreaText(String uri) {
                String response = null;
                try {
                    response = OpendapUtils.getResponse(uri);
                } catch (Exception e) {
                    // todo - exception handling
                    e.printStackTrace();
                }
                dapResponseArea.setText(response);
            }

        });
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
                useDatasetNameFilter.setEnabled(true);
                useTimeRangeFilter.setEnabled(true);
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
        statusBar.add(message, JideBoxLayout.VARY);

        ProgressStatusBarItem progressBarItem = new ProgressStatusBarItem();
        progressBarItem.setProgress(0);
        progressBar = progressBarItem.getProgressBar();

        statusBar.add(progressBarItem, JideBoxLayout.FIX);

        useRegionFilter.setEnabled(false);
        useDatasetNameFilter.setEnabled(false);
        useTimeRangeFilter.setEnabled(false);
        useVariableFilter.setEnabled(false);
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
        final JPanel urlPanel = new JPanel(new BorderLayout(4, 4));
        urlPanel.add(new JLabel("Root URL"), BorderLayout.NORTH);
        urlPanel.add(urlField);
        urlPanel.add(refreshButton, BorderLayout.EAST);

        final JScrollPane dapResponse = new JScrollPane(dapResponseArea);

        final JPanel variableInfo = new JPanel(new BorderLayout(5, 5));
        variableInfo.setBorder(new EmptyBorder(10, 0, 0, 0));
        variableInfo.add(new JLabel("  Variable Info:"), BorderLayout.NORTH);
        variableInfo.add(dapResponse, BorderLayout.CENTER);

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
        final JButton downloadButton = new JButton("Download");
        JLabel messageLabel = new JLabel();
        final ProgressMonitor pm = new DownloadProgressBarProgressMonitor(progressBar, messageLabel);
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
        downloadButton.addActionListener(new DownloadAction(pm));
        folderChooserComboBox.setEditable(true);
        if (VisatApp.getApp() != null) {
            downloadButtonPanel.add(openInVisat, BorderLayout.NORTH);
        }
        downloadButtonPanel.add(folderChooserComboBox);
        downloadButtonPanel.add(downloadButton, BorderLayout.EAST);
        downloadButtonPanel.add(messageLabel, BorderLayout.NORTH);

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
        final String text = urlField.getSelectedItem().toString();
        final String catalogUrl;
        if (!text.toLowerCase().endsWith("/catalog.xml")) {
            catalogUrl = text + "/catalog.xml";
        } else {
            catalogUrl = text;
        }
        final InvCatalogFactory factory = InvCatalogFactory.getDefaultFactory(true);
        final InvCatalog catalog = factory.readXML(catalogUrl);
        final List<InvDataset> datasets = catalog.getDatasets();

        if (datasets.size() == 0) {
            JOptionPane.showMessageDialog(this, "'" + text + "' is not a valid OPeNDAP URL.");
            return false;
        }

        catalogTree.setNewRootDatasets(datasets);
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

    private static class DownloadProgressBarProgressMonitor extends ProgressBarProgressMonitor {

        private DownloadProgressBarProgressMonitor(JProgressBar progressBar, JLabel messageLabel) {
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

    private class DownloadAction implements ActionListener {

        private final ProgressMonitor pm;

        public DownloadAction(ProgressMonitor pm) {
            this.pm = pm;
            pm.worked(0);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final TreePath[] selectionPaths = ((JTree) catalogTree.getComponent()).getSelectionModel().getSelectionPaths();
            if (selectionPaths == null || selectionPaths.length <= 0) {
                return;
            }
            List<String> dapURIs = new ArrayList<String>();
            List<String> fileURIs = new ArrayList<String>();
            for (TreePath selectionPath : selectionPaths) {
                final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                if (CatalogTree.isDapNode(treeNode) || CatalogTree.isFileNode(treeNode)) {
                    final OpendapLeaf leaf = (OpendapLeaf) treeNode.getUserObject();
                    if (leaf.isDapAccess()) {
                        dapURIs.add(leaf.getDapUri());
                    } else if (leaf.isFileAccess()){
                        fileURIs.add(leaf.getFileUri());
                    }
                }
            }
            if (dapURIs.size() == 0 && fileURIs.size() == 0) {
                return;
            }
            pm.beginTask("Downloading", (int) currentDataSize);
            final DAPDownloader downloader = new DAPDownloader(dapURIs, fileURIs, pm);
            final File targetDirectory;
            if (folderChooserComboBox.getSelectedItem() == null ||
                folderChooserComboBox.getSelectedItem().toString().equals("")) {
                targetDirectory = fetchTargetDirectory();
            } else {
                targetDirectory = new File(folderChooserComboBox.getSelectedItem().toString());
            }
            SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    downloader.saveProducts(targetDirectory);
                    if (openInVisat.isSelected()) {
                        downloader.openProducts(VisatApp.getApp());
                    }
                    return null;
                }

                @Override
                protected void done() {
                    pm.done();
                }
            };
            swingWorker.execute();
        }
    }

}