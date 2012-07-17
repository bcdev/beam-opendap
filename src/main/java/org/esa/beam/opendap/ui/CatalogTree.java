package org.esa.beam.opendap.ui;

import opendap.dap.http.HTTPException;
import opendap.dap.http.HTTPMethod;
import opendap.dap.http.HTTPSession;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.opendap.CatalogNode;
import org.esa.beam.opendap.OpendapLeaf;
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
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CatalogTree {

    private final JTree jTree;
    private final HashMap<OpendapLeaf, MutableTreeNode> leafToParentNode = new HashMap<OpendapLeaf, MutableTreeNode>();
    private final Set<CatalogTreeListener> catalogTreeListeners = new HashSet<CatalogTreeListener>();

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
        appendToNode(jTree, rootDatasets, rootNode, true);
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
                final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) lastPathComponent;
                final DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getChildAt(0);
                if (isCatalogReferenceNode(child)) {
                    resolveCatalogReferenceNode(child, true);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
    }

    public synchronized void resolveCatalogReferenceNode(DefaultMutableTreeNode catalogReferenceNode, boolean expandPath) {
        if (catalogReferenceNode != null) {
            final CatalogNode catalogNode = (CatalogNode) catalogReferenceNode.getUserObject();
            final DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
            model.removeNodeFromParent(catalogReferenceNode);
            try {
                final URL catalogUrl = new URL(catalogNode.getCatalogUri());
                final URLConnection urlConnection = catalogUrl.openConnection();
                final InputStream inputStream = urlConnection.getInputStream();
                final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) catalogReferenceNode.getParent();
                insertCatalogElements(inputStream, catalogUrl.toURI(), parent, expandPath);
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

    void insertCatalogElements(InputStream catalogIS, URI catalogBaseUri, DefaultMutableTreeNode parent,
                               boolean expandPath) {
        final InvCatalogFactory factory = InvCatalogFactory.getDefaultFactory(true);
        final InvCatalogImpl catalog = factory.readXML(catalogIS, catalogBaseUri);
        final List<InvDataset> catalogDatasets = catalog.getDatasets();
        appendToNode(jTree, catalogDatasets, parent, true);
        if(expandPath) {
            expandPath(parent);
        }
    }

    static void addTreeSelectionListener(final JTree jTree, final ResponseDispatcher responseDispatcher) {

        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                final TreePath path = e.getPath();
                final DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) path.getLastPathComponent();
                final Object userObject = lastPathComponent.getUserObject();
                if (!(userObject instanceof OpendapLeaf)) {
                    return;
                }
                final OpendapLeaf dapObject = (OpendapLeaf) userObject;
                if (dapObject.isDapAccess()) {
                    final String uri = dapObject.getDdsUri();
                    try {
                        HTTPSession session = new HTTPSession();
                        final HTTPMethod httpMethod = session.newMethodGet(uri);
                        httpMethod.execute();
                        responseDispatcher.dispatchDDSResponse(httpMethod.getResponseAsString());
                    } catch (IOException e1) {
                        //todo
                        Debug.trace(e1);
                    }
                } else if (dapObject.isFileAccess()) {
                    final String fileUri = dapObject.getFileUri();
                    try {
                        HTTPSession session = new HTTPSession();
                        final HTTPMethod httpMethod = session.newMethodGet(fileUri);
                        httpMethod.execute();
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
            return (userObject instanceof OpendapLeaf) && ((OpendapLeaf) userObject).isDapAccess();
        }
        return false;
    }

    static boolean isFileNode(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            return (userObject instanceof OpendapLeaf) && ((OpendapLeaf) userObject).isFileAccess();
        }
        return false;
    }

    static boolean isCatalogReferenceNode(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            return (userObject instanceof CatalogNode);
        }
        return false;
    }

    void appendToNode(final JTree jTree, List<InvDataset> datasets, MutableTreeNode parentNode, boolean goDeeper) {
        for (InvDataset dataset : datasets) {
            final MutableTreeNode deeperParent;
            if (!goDeeper || !isHyraxId(dataset.getID())) {
                appendToNode(jTree, dataset, parentNode);
                deeperParent = (MutableTreeNode) parentNode.getChildAt(parentNode.getChildCount() - 1);
                if (isDapNode(deeperParent) || isFileNode(deeperParent)) {
                    final boolean hasNestedDatasets = dataset.hasNestedDatasets();
                    final OpendapLeaf leaf = (OpendapLeaf) ((DefaultMutableTreeNode) deeperParent).getUserObject();
                    fireLeafAdded(leaf, hasNestedDatasets);
                }
            } else {
                deeperParent = parentNode;
            }
            if (goDeeper && !(dataset instanceof InvCatalogRef)) {
                appendToNode(jTree, dataset.getDatasets(), deeperParent, false);
            }
        }
    }

    private static boolean isHyraxId(String id) {
        return id != null && id.startsWith("/") && id.endsWith("/");
    }

    private void appendToNode(JTree jTree, InvDataset dataset, MutableTreeNode parentNode) {
        final DefaultTreeModel treeModel = (DefaultTreeModel) jTree.getModel();
        if (dataset instanceof InvCatalogRef) {
            appendCatalogNodeToParent(parentNode, treeModel, (InvCatalogRef) dataset);
        } else {
            appendDataNodeToParent(parentNode, treeModel, dataset);
        }
    }

    static void appendCatalogNodeToParent(MutableTreeNode parentNode, DefaultTreeModel treeModel, InvCatalogRef catalogRef) {
        final DefaultMutableTreeNode catalogNode = new DefaultMutableTreeNode(catalogRef.getName() + "/");
        final String catalogPath = catalogRef.getURI().toASCIIString();
        final CatalogNode opendapNode = new CatalogNode(catalogPath, catalogRef);
        opendapNode.setCatalogUri(catalogPath);
        catalogNode.add(new DefaultMutableTreeNode(opendapNode));
        treeModel.insertNodeInto(catalogNode, parentNode, parentNode.getChildCount());
    }

    void appendDataNodeToParent(MutableTreeNode parentNode, DefaultTreeModel treeModel, InvDataset dataset) {
        final OpendapLeaf leafObject = new OpendapLeaf(dataset.getName(), dataset);

        final InvAccess dapAccess = dataset.getAccess(ServiceType.OPENDAP);
        if (dapAccess != null) {
            leafObject.setDapAccess(true);
            leafObject.setDapUri(dapAccess.getStandardUrlName());

            final InvAccess fileAccess = dataset.getAccess(ServiceType.FILE);
            if (fileAccess != null) {
                leafObject.setFileAccess(true);
                leafObject.setFileUri(fileAccess.getStandardUrlName());
            } else {
                final InvAccess serverAccess = dataset.getAccess(ServiceType.HTTPServer);
                if (serverAccess != null) {
                    leafObject.setFileAccess(true);
                    leafObject.setFileUri(serverAccess.getStandardUrlName());
                }
            }
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


    MutableTreeNode getNode(OpendapLeaf leaf) {
        final MutableTreeNode node = getNode(jTree.getModel(), jTree.getModel().getRoot(), leaf);
        if (node == null) {
            throw new IllegalStateException("node of leaf '" + leaf.toString() + "' is null.");
        }
        return node;
    }

    private MutableTreeNode getNode(TreeModel model, Object node, OpendapLeaf leaf) {
        for (int i = 0; i < model.getChildCount(node); i++) {
            final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) (model.getChild(node, i));
            if (childNode.getUserObject() == leaf) {
                return childNode;
            } else {
                final MutableTreeNode temp = getNode(model, model.getChild(node, i), leaf);
                if (temp != null) {
                    return temp;
                }
            }
        }
        return null;
    }

    OpendapLeaf[] getLeaves() {
        final Set<OpendapLeaf> leafs = new HashSet<OpendapLeaf>();
        getLeaves(jTree.getModel().getRoot(), jTree.getModel(), leafs);
        leafs.addAll(leafToParentNode.keySet());
        return leafs.toArray(new OpendapLeaf[leafs.size()]);
    }

    private void getLeaves(Object node, TreeModel model, Set<OpendapLeaf> result) {
        for (int i = 0; i < model.getChildCount(node); i++) {
            if (model.isLeaf(model.getChild(node, i))) {
                final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) (model.getChild(node, i));
                if (isDapNode(childNode) || isFileNode(childNode)) {
                    result.add((OpendapLeaf) childNode.getUserObject());
                }
            } else {
                getLeaves(model.getChild(node, i), model, result);
            }
        }
    }

    void setLeafVisible(OpendapLeaf leaf, boolean visible) {
        if (visible) {
            setLeafVisible(leaf);
        } else {
            setLeafInvisible(leaf);
        }
    }

    private void setLeafVisible(OpendapLeaf leaf) {
        final boolean leafIsRemovedFromTree = leafToParentNode.containsKey(leaf);
        if (leafIsRemovedFromTree) {
            appendDataNodeToParent(leafToParentNode.get(leaf), (DefaultTreeModel) jTree.getModel(), leaf.getDataset());
            leafToParentNode.remove(leaf);
        }
    }

    private void setLeafInvisible(OpendapLeaf leaf) {
        final boolean leafIsRemovedFromTree = leafToParentNode.containsKey(leaf);
        if (!leafIsRemovedFromTree) {
            final MutableTreeNode node = getNode(leaf);
            final DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
            leafToParentNode.put(leaf, (MutableTreeNode) node.getParent());
            model.removeNodeFromParent(node);
        }
    }

    private void fireLeafAdded(OpendapLeaf leaf, boolean hasNestedDatasets) {
        for (CatalogTreeListener catalogTreeListener : catalogTreeListeners) {
            catalogTreeListener.leafAdded(leaf, hasNestedDatasets);
        }
    }

    void addCatalogTreeListener(CatalogTreeListener listener) {
        catalogTreeListeners.add(listener);
    }

    public static interface ResponseDispatcher {

        void dispatchDASResponse(String response);

        void dispatchDDSResponse(String response);

        void dispatchFileResponse(String response);
    }

    static interface CatalogTreeListener {

        void leafAdded(OpendapLeaf leaf, boolean hasNestedDatasets);

    }
}
