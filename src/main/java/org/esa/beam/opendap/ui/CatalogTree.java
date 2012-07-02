package org.esa.beam.opendap.ui;

import opendap.dap.http.HTTPException;
import opendap.dap.http.HTTPMethod;
import opendap.dap.http.HTTPSession;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.util.Debug;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
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
import java.util.List;

public class CatalogTree {

    private final JTree jTree;

    public CatalogTree(final ResponseDispatcher responseDispatcher) {
        jTree = new JTree();
        jTree.setRootVisible(false);
        ((DefaultTreeModel) jTree.getModel()).setRoot(createRootNode());
        addCellRenderer(jTree);
        addWillExpandListener();
        addTreeSelectionListener(jTree, responseDispatcher);
    }

    public Component getComponent() {
        return jTree;
    }

    public void setNewRootDatasets(List<InvDataset> rootDatasets) {
        final DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
        final DefaultMutableTreeNode rootNode = createRootNode();
        model.setRoot(rootNode);
        appendToNode(jTree, rootDatasets, rootNode);
        expandPath(rootNode);
    }

    static void addCellRenderer(final JTree jTree) {
        final ImageIcon dapIcon = UIUtils.loadImageIcon("/org/esa/beam/opendap/images/icons/DRsProduct16.png");
        final ImageIcon fileIcon = UIUtils.loadImageIcon("/org/esa/beam/opendap/images/icons/FRsProduct16.png");
        final ImageIcon standardIcon = UIUtils.loadImageIcon("/org/esa/beam/opendap/images/icons/NoAccess16.png");
        jTree.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (isDapNode(value)) {
                    setLeafIcon(dapIcon);
                } else if (isFileNode(value)) {
                    setLeafIcon(fileIcon);
                } else {
                    setLeafIcon(standardIcon);
                }
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                return this;
            }
        });
    }

    void addWillExpandListener() {
        jTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                final Object lastPathComponent = event.getPath().getLastPathComponent();
                final TreeNode child = ((DefaultMutableTreeNode) lastPathComponent).getChildAt(0);
                if (isCatalogReferenceNode(child)) {
                    final DefaultMutableTreeNode dapNode = (DefaultMutableTreeNode) child;
                    final OPeNDAP_Leaf catalogLeaf = (OPeNDAP_Leaf) dapNode.getUserObject();
                    final DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
                    final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dapNode.getParent();
                    model.removeNodeFromParent(dapNode);
                    try {
                        final URL catalogUrl = new URL(catalogLeaf.getCatalogUri());
                        final URLConnection urlConnection = catalogUrl.openConnection();
                        final InputStream inputStream = urlConnection.getInputStream();
                        insertCatalogElements(inputStream, catalogUrl.toURI(), parent);
                    } catch (MalformedURLException e) {
                        // todo handle with error collection and message dialog at the end.
                        Debug.trace(e);
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

    void insertCatalogElements(InputStream catalogIS, URI catalogBaseUri, DefaultMutableTreeNode parent) {
        final InvCatalogFactory factory = InvCatalogFactory.getDefaultFactory(true);
        final InvCatalogImpl catalog = factory.readXML(catalogIS, catalogBaseUri);
        final List<InvDataset> catalogDatasets = catalog.getDatasets();
        appendToNode(jTree, catalogDatasets, parent);
        expandPath(parent);
    }

    static void addTreeSelectionListener(final JTree jTree, final ResponseDispatcher responseDispatcher) {

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
                if (dapObject.isDapAccess()) {
//                final String uri = dapObject.getDodsUri();
//                final String uri = dapObject.getDdxUri();
                    final String uri = dapObject.getDdsUri();
//                final String uri = dapObject.getDasUri();
                    try {
                        HTTPSession session = new HTTPSession();
                        final HTTPMethod httpMethod = session.newMethodGet(uri);
                        final int execute = httpMethod.execute();
                        responseDispatcher.dispatchDDSResponse(httpMethod.getResponseAsString());

                        // final DODSNetcdfFile netcdfFile = new DODSNetcdfFile(uri);
                        // System.out.println(netcdfFile.getFileTypeId());
                        // final Iterator<ProductReaderPlugIn> readerPlugIns = ProductIOPlugInManager.getInstance().getReaderPlugIns(Constants.FORMAT_NAME);
                        // while (readerPlugIns.hasNext()) {
                        //     ProductReaderPlugIn readerPlugIn = readerPlugIns.next();
                        //     if (readerPlugIn instanceof GenericNetCdfReaderPlugIn) {
                        //         final ProductReader readerInstance = readerPlugIn.createReaderInstance();
                        //         final Product product = readerInstance.readProductNodes(netcdfFile, null);
                        //         System.out.println("product.getNumBands() = " + product.getNumBands());
                        //         product.closeIO();
                        //     }
                        // }
                    } catch (IOException e1) {
                        //todo
                        Debug.trace(e1);
                    }
                } else if (dapObject.isFileAccess()) {
                    final String fileUri = dapObject.getFileUri();
                    try {
                        HTTPSession session = new HTTPSession();
                        final HTTPMethod httpMethod = session.newMethodGet(fileUri);
                        final int execute = httpMethod.execute();
                        responseDispatcher.dispatchFileResponse(httpMethod.getResponseAsString());
                    } catch (HTTPException e1) {
                        //todo
                        Debug.trace(e1);
                    }
                }
            }
        });
    }


    static boolean isDapNode(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            return (userObject instanceof OPeNDAP_Leaf) && ((OPeNDAP_Leaf) userObject).isDapAccess();
        }
        return false;
    }

    static boolean isFileNode(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            return (userObject instanceof OPeNDAP_Leaf) && ((OPeNDAP_Leaf) userObject).isFileAccess();
        }
        return false;
    }

    static boolean isCatalogReferenceNode(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            return (userObject instanceof OPeNDAP_Leaf) && ((OPeNDAP_Leaf) userObject).isCatalogReference();
        }
        return false;
    }

    static void appendToNode(final JTree jTree, List<InvDataset> datasets, MutableTreeNode parentNode) {
        for (InvDataset dataset : datasets) {
            final boolean dataSetIsNoCatalog = !(dataset instanceof InvCatalogRef);
            if (dataSetIsNoCatalog && dataset.hasNestedDatasets()) {
                appendToNode(jTree, dataset.getDatasets(), parentNode);
            } else {
                appendToNode(jTree, dataset, parentNode);
            }
        }
    }

    private static void appendToNode(JTree jTree, InvDataset dataset, MutableTreeNode parentNode) {
        final DefaultTreeModel treeModel = (DefaultTreeModel) jTree.getModel();
        if (dataset instanceof InvCatalogRef) {
            final InvCatalogRef catalogRef = (InvCatalogRef) dataset;
            appendCatalogNodeToParent(parentNode, treeModel, catalogRef);
        } else {
            appendDataNodeToParent(parentNode, treeModel, dataset);
        }
    }

    static void appendCatalogNodeToParent(MutableTreeNode parentNode, DefaultTreeModel treeModel, InvCatalogRef catalogRef) {
        final DefaultMutableTreeNode catalogNode = new DefaultMutableTreeNode(catalogRef.getName() + "/");
        final String urlPath = catalogRef.getURI().toASCIIString();
        final OPeNDAP_Leaf oPeNDAP_leaf = new OPeNDAP_Leaf(urlPath);
        oPeNDAP_leaf.setCatalogReference(true);
        oPeNDAP_leaf.setCatalogUri(urlPath);
        catalogNode.add(new DefaultMutableTreeNode(oPeNDAP_leaf));
        treeModel.insertNodeInto(catalogNode, parentNode, parentNode.getChildCount());
    }

    static void appendDataNodeToParent(MutableTreeNode parentNode, DefaultTreeModel treeModel, InvDataset dataset) {
        final OPeNDAP_Leaf leafObject = new OPeNDAP_Leaf(dataset.getName());

        final InvAccess dapAccess = dataset.getAccess(ServiceType.OPENDAP);
        if (dapAccess != null) {
            leafObject.setDapAccess(true);
            leafObject.setDapUri(dapAccess.getStandardUrlName());
        }

        final InvAccess fileAccess = dataset.getAccess(ServiceType.FILE);
        if (fileAccess != null) {
            leafObject.setFileAccess(true);
            leafObject.setFileUri(fileAccess.getStandardUrlName());
        }

        final DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(leafObject);
        treeModel.insertNodeInto(leafNode, parentNode, parentNode.getChildCount());
    }

    private void expandPath(DefaultMutableTreeNode node) {
        jTree.expandPath(new TreePath(node.getPath()));
    }

    static DefaultMutableTreeNode createRootNode() {
        return new DefaultMutableTreeNode("root", true);
    }

    static class OPeNDAP_Leaf {

        private final String name;
        private boolean dapAccess;
        private boolean fileAccess;
        private boolean catalogReference;
        private String catalogUri;
        private String dapUri;
        private String fileUri;

        public OPeNDAP_Leaf(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public boolean isCatalogReference() {
            return catalogReference;
        }

        public void setCatalogReference(boolean catalogReference) {
            this.catalogReference = catalogReference;
        }

        public boolean isDapAccess() {
            return dapAccess;
        }

        public boolean isFileAccess() {
            return fileAccess;
        }

        public String getDasUri() {
            return getDapUri() + ".das";
        }

        public String getDdsUri() {
            return getDapUri() + ".dds";
        }

        public String getDdxUri() {
            return getDapUri() + ".ddx";
        }

        public String getDapUri() {
            return dapUri;
        }

        public void setDapUri(String dapUri) {
            this.dapUri = dapUri;
        }

        public String getFileUri() {
            return fileUri;
        }

        public void setFileUri(String fileUri) {
            this.fileUri = fileUri;
        }

        public void setDapAccess(boolean dapAccess) {
            this.dapAccess = dapAccess;
        }

        public void setFileAccess(boolean fileAccess) {
            this.fileAccess = fileAccess;
        }

        public String getCatalogUri() {
            return catalogUri;
        }

        public void setCatalogUri(String catalogUri) {
            this.catalogUri = catalogUri;
        }
    }

    public static interface ResponseDispatcher {

        void dispatchDASResponse(String response);

        void dispatchDDSResponse(String response);

        void dispatchFileResponse(String response);
    }
}
