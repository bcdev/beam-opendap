package org.esa.beam.opendap.ui;

import com.jidesoft.combobox.FolderChooserExComboBox;
import com.jidesoft.combobox.PopupPanel;
import com.jidesoft.swing.FolderChooser;
import com.jidesoft.utils.Lm;
import opendap.dap.DAP2Exception;
import opendap.dap.DDS;
import opendap.dap.parser.ParseException;
import org.esa.beam.opendap.OpendapLeaf;
import org.esa.beam.opendap.utils.DAPDownloader;
import org.esa.beam.opendap.utils.VariableCollector;
import org.esa.beam.util.Debug;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.util.StringUtils;
import org.esa.beam.visat.VisatApp;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
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
import java.io.ByteArrayInputStream;
import java.io.File;
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

    private JCheckBox useVariableNameFilter;
    private FilterComponent variableNameFilter;

    private final PropertyMap propertyMap;


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
        final VariableCollector variableCollector = new VariableCollector();
        catalogTree = new CatalogTree(new CatalogTree.ResponseDispatcher() {
            @Override
            public void dispatchDASResponse(String response) {
                dapResponseArea.setText(response);
            }

            @Override
            public void dispatchDDSResponse(String response) {
                dapResponseArea.setText(response);

                final DDS dds = new DDS();
                try {
                    dds.parse(new ByteArrayInputStream(response.getBytes()));
                    variableCollector.collectFrom(dds);
                } catch (ParseException e) {
                    Debug.trace(e);
                } catch (DAP2Exception e) {
                    Debug.trace(e);
                }
            }

            @Override
            public void dispatchFileResponse(String response) {
                dapResponseArea.setText(response);
            }
        });
        useDatasetNameFilter = new JCheckBox("Use dataset name filter");
        useTimeRangeFilter = new JCheckBox("Use time range filter");
        useRegionFilter = new JCheckBox("Use region filter");
        useVariableNameFilter = new JCheckBox("Use variable name filter");
        datasetNameFilter = new DatasetNameFilter(useDatasetNameFilter);
        datasetNameFilter.addFilterChangeListener(new FilterChangeListener() {
            @Override
            public void filterChanged() {
                final OpendapLeaf[] leaves = catalogTree.getLeaves();
                for (OpendapLeaf leaf : leaves) {
                    if (datasetNameFilter.accept(leaf)) {
                        catalogTree.setLeafVisible(leaf, true);
                    } else {
                        catalogTree.setLeafVisible(leaf, false);
                    }
                }
            }
        });
        timeRangeFilter = new TimeRangeFilter();
        regionFilter = new RegionFilter();
        variableNameFilter = new VariableNameFilter();


        catalogTree.addCatalogTreeListener(new CatalogTree.CatalogTreeListener() {
            @Override
            public void dataNodeAdded(OpendapLeaf leaf, boolean hasNestedDatasets) {
                if (hasNestedDatasets) {
                    return;
                }
                // todo - add other filters
                if (datasetNameFilter.accept(leaf)) {
                    catalogTree.setLeafVisible(leaf, true);
                } else {
                    catalogTree.setLeafVisible(leaf, false);
                }
            }
        });
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
        filterPanel.add(new TitledPanel(useVariableNameFilter, variableNameFilter.getUI()));
        final Dimension size = filterPanel.getSize();
        filterPanel.setMinimumSize(new Dimension(460, size.height));

        final JPanel optionalPanel = new TitledPanel(null, null);

        final JPanel downloadButtonPanel = new JPanel(new BorderLayout(8, 0));
        downloadButtonPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        final JButton downloadButton = new JButton("Download");
        final FolderChooserExComboBox folderChooserComboBox = new FolderChooserExComboBox() {
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
        downloadButton.addActionListener(new ActionListener() {
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
                    if (CatalogTree.isDapNode(treeNode)) {
                        final OpendapLeaf leaf = (OpendapLeaf) treeNode.getUserObject();
                        if (leaf.isDapAccess()) {
                            dapURIs.add(leaf.getDapUri());
                        } else {
                            fileURIs.add(leaf.getFileUri());
                        }
                    }
                }
                if (dapURIs.size() == 0) {
                    return;
                }
                final DAPDownloader downloader = new DAPDownloader(dapURIs, fileURIs);
                File targetDirectory;
                if (folderChooserComboBox.getSelectedItem() == null ||
                    folderChooserComboBox.getSelectedItem().toString().equals("")) {
                    targetDirectory = fetchTargetDirectory();
                } else {
                    targetDirectory = new File(folderChooserComboBox.getSelectedItem().toString());
                }
                downloader.saveProducts(targetDirectory);
                downloader.openProducts(VisatApp.getApp());
            }
        });
        folderChooserComboBox.setEditable(true);
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
        final InvCatalogImpl catalog = factory.readXML(catalogUrl);
        final List<InvDataset> datasets = catalog.getDatasets();

        if (datasets.size() == 0) {
            JOptionPane.showMessageDialog(this, "'" + text + "' is not a valid OPeNDAP URL.");
            return false;
        }

        catalogTree.setNewRootDatasets(datasets);
        return true;
    }
}
