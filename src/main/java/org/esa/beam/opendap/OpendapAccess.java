package org.esa.beam.opendap;

import com.jidesoft.swing.JideSplitPaneLayout;
import org.esa.beam.opendap.ui.CatalogTree;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.SplitPaneUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class OpendapAccess extends JPanel {

    private JTextField urlField;
    private JButton refreshButton;
    private CatalogTree catalogTree;
    private JTextArea dapResponseArea;

    public static void main(String[] args) {
        final OpendapAccess opendapAccess = new OpendapAccess();
        final JFrame mainFrame = new JFrame("OPeNDAP Access");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setContentPane(opendapAccess);
        mainFrame.pack();
        final Dimension size = mainFrame.getSize();
        mainFrame.setMinimumSize(size);
        mainFrame.setVisible(true);
    }

    public OpendapAccess() throws HeadlessException {
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
        dapResponseArea = new JTextArea(30, 40);
        catalogTree = new CatalogTree(new CatalogTree.ResponseDispatcher() {
            @Override
            public void dispatchDASResponse(String response) {
                dapResponseArea.setText(response);
            }

            @Override
            public void dispatchDDSResponse(String response) {
                dapResponseArea.setText(response);
            }

            @Override
            public void dispatchFileResponse(String response) {
                dapResponseArea.setText(response);
            }
        });
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
        } else if (datasets.size() == 1) {
            final InvDataset dataset = datasets.get(0);
            final List<InvDataset> rootDatasets = dataset.getDatasets();
            catalogTree.setNewRootDatasets(rootDatasets);
        } else {
            catalogTree.setNewRootDatasets(datasets);
        }
    }

    private void initContentPane() {
        final JPanel urlPanel = new JPanel(new BorderLayout(4, 4));
        urlPanel.add(new JLabel("Root URL"), BorderLayout.NORTH);
        urlPanel.add(urlField);
        urlPanel.add(refreshButton, BorderLayout.EAST);

        final JSplitPane centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(catalogTree.getComponent()), new JScrollPane(dapResponseArea));
        centerPanel.setDividerLocation(300);
        centerPanel.setContinuousLayout(true);
//        final JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
//        centerPanel.add(new JScrollPane(catalogTree.getComponent()), BorderLayout.CENTER);
//        centerPanel.add(new JScrollPane(dapResponseArea), BorderLayout.EAST);

        this.setLayout(new BorderLayout(15, 15));
        this.setBorder(new EmptyBorder(8, 8, 8, 8));
        this.add(urlPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
    }

}