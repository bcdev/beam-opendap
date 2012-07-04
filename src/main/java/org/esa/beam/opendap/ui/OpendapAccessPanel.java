package org.esa.beam.opendap.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.bc.io.FileDownloader;
import opendap.dap.DAP2Exception;
import opendap.dap.DDS;
import opendap.dap.parser.ParseException;
import org.esa.beam.opendap.utils.DAPDownloader;
import org.esa.beam.opendap.utils.VariableCollector;
import org.esa.beam.util.Debug;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;

public class OpendapAccessPanel extends JPanel {

    private JTextField urlField;
    private JButton refreshButton;
    private CatalogTree catalogTree;
    private JTextArea dapResponseArea;

    private JCheckBox useDatasetNameFilter;
    private Filter datasetNameFilter;

    private JCheckBox useTimeRangeFilter;
    private Filter timeRangeFilter;

    private JCheckBox useRegionFilter;
    private Filter regionFilter;

    private JCheckBox useVariableNameFilter;
    private Filter variableNameFilter;


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        final OpendapAccessPanel opendapAccessPanel = new OpendapAccessPanel();
        final JFrame mainFrame = new JFrame("OPeNDAP Access");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setContentPane(opendapAccessPanel);
        mainFrame.pack();
        final Dimension size = mainFrame.getSize();
        mainFrame.setMinimumSize(size);
        mainFrame.setVisible(true);
    }

    public OpendapAccessPanel() throws HeadlessException {
        super();
        initComponents();
        initContentPane();
    }

    private void initComponents() {
        urlField = new JTextField();
        urlField.setColumns(60);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
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
        datasetNameFilter = new DatasetNameFilter();
        timeRangeFilter = new TimeRangeFilter();
        regionFilter = new RegionFilter();
        variableNameFilter = new VariableNameFilter();
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

        final JPanel downloadButtonPanel = new JPanel(new BorderLayout());
        final JButton downloadButton = new JButton("Download");
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final TreePath[] selectionPaths = ((JTree) catalogTree.getComponent()).getSelectionModel().getSelectionPaths();
                if (selectionPaths == null || selectionPaths.length <= 0) {
                    return;
                }
                List<String> dapURIs = new ArrayList<String>();
                for (TreePath selectionPath : selectionPaths) {
                    final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                    if(CatalogTree.isDapNode(treeNode)){
                        final CatalogTree.OPeNDAP_Leaf leaf = (CatalogTree.OPeNDAP_Leaf) treeNode.getUserObject();
                        final String downloadUri;
                        if (leaf.isFileAccess()) {
                            downloadUri = leaf.getFileUri();
                        } else {
                            downloadUri = leaf.getDapUri();
                        }

                        dapURIs.add(downloadUri);
                    }
                }
                if (dapURIs.size() == 0) {
                    return;
                }

                final DAPDownloader downloader = new DAPDownloader(dapURIs);
                downloader.saveAndOpenProducts();
            }
        });
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

    private void refresh() {
        final String text = urlField.getText();
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
            return;
        }

        catalogTree.setNewRootDatasets(datasets);
    }
}
