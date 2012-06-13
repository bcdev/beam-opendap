package org.esa.beam.opendap.ui;

import com.jidesoft.tree.StyledTreeCellRenderer;
import org.esa.beam.dataio.netcdf.GenericNetCdfReaderPlugIn;
import org.esa.beam.dataio.netcdf.util.Constants;
import org.esa.beam.framework.dataio.ProductIOPlugInManager;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.util.Debug;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvService;
import ucar.nc2.dods.DODSNetcdfFile;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;

public class CatalogTree {

    private final JTree jTree;

    public CatalogTree() {
        jTree = new JTree();
        jTree.setRootVisible(false);
        ((DefaultTreeModel) jTree.getModel()).setRoot(createRootNode());
        addCellRenderer(jTree);
        addWillExpandListener(jTree);
        addTreeSelectionListener(jTree);
    }

    public Component getComponent() {
        return jTree;
    }

    public void setNewRootDatasets(List<InvDataset> rootDatasets) {
        final DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
        final DefaultMutableTreeNode rootNode = createRootNode();
        model.setRoot(rootNode);
        appendToNode(jTree, rootDatasets, rootNode);
        jTree.expandPath(new TreePath(rootNode.getPath()));
    }

    static void addCellRenderer(final JTree jTree) {

        final StyledTreeCellRenderer dapNodeRenderer = new StyledTreeCellRenderer();
        dapNodeRenderer.setLeafIcon(UIUtils.loadImageIcon("icons/Edit24.gif"));

        final StyledTreeCellRenderer fileNodeRenderer = new StyledTreeCellRenderer();
        fileNodeRenderer.setLeafIcon(UIUtils.loadImageIcon("icons/Print24.gif"));

        final TreeCellRenderer renderer = new TreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (isDapNode(value)) {
                    return dapNodeRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                }
                return fileNodeRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        };
        jTree.setCellRenderer(renderer);
    }

    static void addWillExpandListener(final JTree jTree) {
        jTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                final Object lastPathComponent = event.getPath().getLastPathComponent();
                final TreeNode child = ((DefaultMutableTreeNode) lastPathComponent).getChildAt(0);
                if (isDapNode(child)) {
                    final DefaultMutableTreeNode dapNode = (DefaultMutableTreeNode) child;
                    final String catalogUrlString = (String) dapNode.getUserObject();
                    final DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
                    final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dapNode.getParent();
                    model.removeNodeFromParent(dapNode);
                    try {
                        final URL catalogUrl = new URL(catalogUrlString);
                        final URLConnection urlConnection = catalogUrl.openConnection();
                        final InputStream inputStream = urlConnection.getInputStream();
                        insertCatalogElements(inputStream, catalogUrl.toURI(), parent);

                    } catch (MalformedURLException e) {
                        // todo handle with error collection and message dialog at the end.
                        Debug.trace(e);
//                    } catch (JDOMException e) {
//                        Debug.trace(e);
                    } catch (URISyntaxException e) {
                        Debug.trace(e);
                    } catch (IOException e) {
                        Debug.trace(e);
                    }
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                //Todo change body of created method. Use File | Settings | File Templates to change
            }
        });
    }

    static void insertCatalogElements(InputStream catalogIS, URI catalogBaseUri, DefaultMutableTreeNode parent) {
        final InvCatalogFactory factory = InvCatalogFactory.getDefaultFactory(true);
        final InvCatalogImpl catalog = factory.readXML(catalogIS, catalogBaseUri);
    }


    static void addTreeSelectionListener(final JTree jTree) {
        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                final TreePath path = e.getPath();
                final DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) path.getLastPathComponent();
                final Object userObject = lastPathComponent.getUserObject();
                if (!(userObject instanceof OPeNDAP_Leaf)) {
                    return;
                }
                final OPeNDAP_Leaf dapObject = (OPeNDAP_Leaf) userObject;
                if (!dapObject.isDapAccess()) {
                    return;
                }
                final String uri = dapObject.getDodsUri();
//                final String uri = dapObject.getDdxUri();
//                final String uri = dapObject.getDdsUri();
//                final String uri = dapObject.getDasUri();
                try {
                    final DODSNetcdfFile netcdfFile = new DODSNetcdfFile(uri);
                    System.out.println(netcdfFile.getFileTypeId());
                    final Iterator<ProductReaderPlugIn> readerPlugIns = ProductIOPlugInManager.getInstance().getReaderPlugIns(Constants.FORMAT_NAME);
                    while (readerPlugIns.hasNext()) {
                        ProductReaderPlugIn readerPlugIn = readerPlugIns.next();
                        if (readerPlugIn instanceof GenericNetCdfReaderPlugIn) {
                            final ProductReader readerInstance = readerPlugIn.createReaderInstance();
                            final Product product = readerInstance.readProductNodes(netcdfFile, null);
                            System.out.println("product.getNumBands() = " + product.getNumBands());
                            product.closeIO();
                        }
                    }
                } catch (IOException e1) {
                    Debug.trace(e1);
                }
            }
        });
    }

    static boolean isDapNode(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            return (userObject instanceof String) && ((String) userObject).toLowerCase().startsWith("http:");
        }
        return false;
    }

    static void appendToNode(final JTree jTree, List<InvDataset> datasets, DefaultMutableTreeNode treeNode) {
        final DefaultTreeModel treeModel = (DefaultTreeModel) jTree.getModel();
        for (InvDataset dataset : datasets) {
            if (dataset instanceof InvCatalogRef) {
                final InvCatalogRef catalogRef = (InvCatalogRef) dataset;
                final DefaultMutableTreeNode catalogNode = new DefaultMutableTreeNode(catalogRef.getName() + "/");
                final String urlPath = catalogRef.getURI().toASCIIString();
                catalogNode.add(new DefaultMutableTreeNode(urlPath));
                treeModel.insertNodeInto(catalogNode, treeNode, treeNode.getChildCount());
            } else {
                final String uriString = dataset.getParentCatalog().getUriString();
                final String dapUri = uriString.substring(0, uriString.lastIndexOf("/") + 1) + dataset.getName();
                final OPeNDAP_Leaf leafObject = new OPeNDAP_Leaf(dataset.getName(), dapUri);
                final List<InvAccess> accessList = dataset.getAccess();
                for (InvAccess access : accessList) {
                    InvService service = access.getService();
                    final String serviceName = service.getName();
                    leafObject.setService(serviceName);
                }
                final DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(leafObject);
                treeModel.insertNodeInto(leafNode, treeNode, treeNode.getChildCount());
            }
        }
    }

    static DefaultMutableTreeNode createRootNode() {
        return new DefaultMutableTreeNode("root", true);
    }

    private static class OPeNDAP_Leaf {

        private final String name;
        private boolean dapAccess;
        private boolean fileAccess;
        private final String dapUri;

        public OPeNDAP_Leaf(String name, String dapUri) {
            this.name = name;
            this.dapUri = dapUri;
        }

        @Override
        public String toString() {
            return name;
        }

        public void setService(String serviceName) {
            if (serviceName != null) {
                if (serviceName.trim().equalsIgnoreCase("dap")) {
                    dapAccess = true;
                }
                if (serviceName.trim().equalsIgnoreCase("file")) {
                    fileAccess = true;
                }
            }
        }

        public boolean isDapAccess() {
            return dapAccess;
        }

        public boolean isFileAccess() {
            return fileAccess;
        }

        public String getDasUri() {
            return dapUri + ".das";
        }

        public String getDdsUri() {
            return dapUri + ".dds";
        }

        public String getDdxUri() {
            return dapUri + ".ddx";
        }

        public String getDodsUri() {
            return dapUri;
        }
    }
}
