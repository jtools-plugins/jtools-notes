package com.lhstack.notes;

import com.lhstack.tools.plugins.Helper;
import com.lhstack.tools.plugins.Logger;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

public class HomeView extends JPanel {

    private final NotesView notesView;
    private final Logger logger;
    private final String locationHash;
    private JTree tree;
    private List<Data> datas;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;

    public HomeView(String locationHash, NotesView notesView, Logger logger) {
        this.notesView = notesView;
        this.logger = logger;
        this.locationHash = locationHash;
        this.setLayout(new BorderLayout());
        this.initMenu();
        this.initContent();
    }

    private void initContent() {
        //加载数据
        this.datas = Config.getInstance().isGlobal() ? DataManager.loadData(System.getProperty("user.home") + "/.jtools/notes/data.json", Data.class) : DataManager.loadData(locationHash);
        //创建root节点
        this.root = new DefaultMutableTreeNode();
        //初始化树
        initTree(root, datas);
        //创建树模型
        this.treeModel = new DefaultTreeModel(root);
        //创建tree
        this.tree = new JTree(treeModel);
        //不显示root节点
        this.tree.setRootVisible(false);
        //设置不可编辑
        this.tree.setEditable(false);
        //创建render,自定义未选中的背景色
        DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
        //设置为透明背景
        cellRenderer.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
        this.tree.setCellRenderer(cellRenderer);
        //自定义选择模式
        this.tree.setSelectionModel(new DefaultTreeSelectionModel() {
            @Override
            public void setSelectionPath(TreePath path) {
                super.setSelectionPath(path);
                if (path != null) {
                    tree.scrollPathToVisible(path);
                }
            }
        });
        //设置不支持多选
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //设置鼠标监听
        this.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                // 根据点击的位置获取最近的行
                int row = tree.getClosestRowForLocation(e.getX(), e.getY());

                // 获取行高和树的行数
                int rowHeight = tree.getRowHeight();
                int totalRows = tree.getRowCount();

                // 如果点击的位置超出了树的行数总高度，取消选中
                if (e.getY() > totalRows * rowHeight || row == -1) {
                    tree.clearSelection(); // 如果点击的不是任何行，取消选中
                } else {
                    tree.setSelectionRow(row); // 选中行
                }
                //获取选中的节点
                TreePath treePath = tree.getSelectionPath();
                if (treePath != null) {
                    //右键菜单
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem addNodeItem = new JMenuItem("新增节点", Helper.findIcon("icons/addNode.svg", HomeView.class));
                        addNodeItem.addActionListener(event -> {
                            try {
                                String name = JOptionPane.showInputDialog("请输入节点名称");
                                if (name == null || name.trim().isEmpty()) {
                                    logger.activeConsolePanel();
                                    logger.warn("新增节点,节点名称不能为空");
                                    return;
                                }
                                //当前节点作为父节点
                                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                                //获取节点数据
                                Data parentData = (Data) parentNode.getUserObject();
                                //创建数据节点
                                Data data = new Data();
                                data.setName(name);
                                //获取父级几点children,如果没有,则初始化
                                List<Data> childrenList = parentData.getChildren();
                                if (childrenList == null) {
                                    childrenList = new ArrayList<>();
                                    parentData.setChildren(childrenList);
                                }
                                //添加数据节点到父节点的children
                                childrenList.add(data);
                                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(data);
                                parentNode.add(newNode);
                                //更新视图
                                treeModel.insertNodeInto(newNode, parentNode, parentNode.getIndex(newNode));
                                //持久化数据
                                store();
                            } catch (Throwable err) {
                                logger.error("添加节点失败: " + err);
                            }
                        });
                        JMenuItem removeItem = new JMenuItem("删除节点", Helper.findIcon("icons/deleteNode.svg", HomeView.class));
                        removeItem.addActionListener(event -> {
                            int confirm = JOptionPane.showConfirmDialog(null, "你确定要删除节点吗,如果是树节点,子节点的内容会放到这个节点的父级", "警告", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.OK_OPTION) {
                                try {
                                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                                    List<Data> parenDataList;
                                    if (parent == null) {
                                        parent = root;
                                        parenDataList = datas;
                                    } else {
                                        Data parentData = (Data) parent.getUserObject();
                                        if (parentData == null) {
                                            parenDataList = datas;
                                        } else {
                                            parenDataList = parentData.getChildren();
                                            if (parenDataList == null) {
                                                parenDataList = new ArrayList<>();
                                                parentData.setChildren(parenDataList);
                                            }
                                        }
                                    }
                                    Enumeration<TreeNode> children = node.children();
                                    parent.remove(node);
                                    parenDataList.remove((Data) node.getUserObject());
                                    if (children != null) {
                                        while (children.hasMoreElements()) {
                                            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                                            parent.add(child);
                                            parenDataList.add((Data) child.getUserObject());
                                        }
                                    }
                                    treeModel.reload(parent);
                                    logger.info(datas);
                                    store();
                                } catch (Throwable err) {
                                    logger.error("删除节点错误: " + err);
                                }
                            }
                        });
                        JMenuItem editItem = new JMenuItem("编辑节点", Helper.findIcon("icons/editNode.svg", HomeView.class));
                        editItem.addActionListener(event -> {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                            Data data = (Data) node.getUserObject();
                            String name = JOptionPane.showInputDialog(null, "请输入节点名称", data.getName());
                            if (name == null || name.trim().isEmpty()) {
                                logger.activeConsolePanel();
                                logger.warn("编辑节点名称不能为空");
                                return;
                            }
                            data.setName(name);
                            treeModel.nodeStructureChanged(node);
                            store();
                        });
                        JMenuItem openContentItem = new JMenuItem("打开内容", Helper.findIcon("icons/open.svg", HomeView.class));
                        openContentItem.addActionListener(event -> {
                            notesView.switchContentView();
                        });

                        popupMenu.add(addNodeItem);
                        popupMenu.add(removeItem);
                        popupMenu.add(editItem);
                        popupMenu.add(openContentItem);


                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();

                        if (treeNode.getChildCount() > 0) {
                            JMenuItem removeDirItem = new JMenuItem("删除目录", Helper.findIcon("icons/deleteNode.svg", HomeView.class));
                            removeDirItem.setToolTipText("删除整个目录和目录下所有的节点");
                            removeDirItem.addActionListener(event -> {
                                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeNode.getParent();
                                treeNode.removeFromParent();
                                List<Data> parentDataList;
                                Data data = (Data) parent.getUserObject();
                                if (data == null) {
                                    parentDataList = datas;
                                } else {
                                    parentDataList = data.getChildren();
                                    if (parentDataList == null) {
                                        parentDataList = new ArrayList<>();
                                        data.setChildren(parentDataList);
                                    }
                                }
                                parentDataList.remove((Data) treeNode.getUserObject());
                                treeModel.reload(parent);
                                store();
                            });
                            popupMenu.add(removeDirItem);
                        }

                        popupMenu.show(e.getComponent(), e.getX() + 10, e.getY() + 10);
                    }

                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 3) {
                        notesView.switchContentView();
                    }
                }

            }
        });
        Helper.treeSpeedSearch(tree, true, treePath -> {
            DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            Object userObject = lastPathComponent.getUserObject();
            if (userObject != null) {
                Data data = (Data) userObject;
                return data.getName();
            }
            return "";
        });
        JScrollPane jScrollPane = new JScrollPane(this.tree);
        MatteBorder border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray);
        this.tree.setBorder(border);
        this.add(jScrollPane, BorderLayout.CENTER);
    }


    public Optional<Data> getSelectedData() {

        return Optional.ofNullable(tree.getSelectionPath()).map(TreePath::getLastPathComponent)
                .map(DefaultMutableTreeNode.class::cast).map(DefaultMutableTreeNode::getUserObject).map(Data.class::cast);
    }

    private void initTree(DefaultMutableTreeNode root, List<Data> datas) {
        datas.forEach(data -> {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
            root.add(node);
            if (data.getChildren() != null && !data.getChildren().isEmpty()) {
                initTree(node, data.getChildren());
            }
        });
    }

    private void initMenu() {
        JPanel panel = new JPanel();
        panel.setBorder(null);
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.add(Helper.actionButton(Helper.findIcon("icons/unfold.svg", PluginImpl.class), "全部展开", str -> {
            expandAll();
        }));
        panel.add(Helper.actionButton(Helper.findIcon("icons/packup.svg", PluginImpl.class), "全部收起", str -> {
            collapseAll();
        }));

        panel.add(Helper.actionButton(Helper.findIcon("icons/newNode.svg", PluginImpl.class), "新增节点", str -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String name = JOptionPane.showInputDialog("请输入节点名称");
                    if (name == null || name.trim().isEmpty()) {
                        logger.activeConsolePanel();
                        logger.warn("新增节点,节点名称不能为空");
                        return;
                    }
                    TreePath treePath = tree.getSelectionPath();
                    Data data = new Data();
                    data.setName(name);
                    if (treePath != null) {
                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                        Data parentData = (Data) parentNode.getUserObject();
                        List<Data> childrenList = parentData.getChildren();
                        if (childrenList == null) {
                            childrenList = new ArrayList<>();
                            parentData.setChildren(childrenList);
                        } else {
                            parentData.getChildren().add(data);
                        }
                        childrenList.add(data);
                        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(data);
                        parentNode.add(newNode);
                        treeModel.insertNodeInto(newNode, parentNode, parentNode.getIndex(newNode));
                    } else {
                        datas.add(data);
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
                        root.add(node);
                        if (root.getChildCount() == 1) {
                            treeModel.reload(root);
                        } else {
                            treeModel.insertNodeInto(node, root, root.getIndex(node));
                        }
                    }
                    store();
                } catch (Throwable err) {
                    logger.error("添加节点失败: " + err);
                }
            });
        }));

        this.add(panel, BorderLayout.NORTH);
    }

    private void store() {
        if (!Config.getInstance().isGlobal()) {
            DataManager.storeData(datas, locationHash);
        } else {
            DataManager.store(datas, System.getProperty("user.home") + "/.jtools/notes/data.json");
        }
    }

    private void collapseAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.collapseRow(i);
        }
    }

    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public List<Data> getDatas() {
        return datas;
    }

    public void reload() {
        this.datas = Config.getInstance().isGlobal() ? DataManager.loadData(System.getProperty("user.home") + "/.jtools/notes/data.json", Data.class) : DataManager.loadData(locationHash);
        this.root.removeAllChildren();
        this.initTree(this.root, this.datas);
        this.treeModel.reload();
    }
}
