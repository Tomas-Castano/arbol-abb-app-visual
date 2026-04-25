package abbtree.model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

public class BSTTree<T extends Comparable<T>> {

    private TreeNode<T> root;

    public BSTTree() {
        this.root = null;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void add(T data) {
        root = addRec(root, data);
    }
    private TreeNode<T> addRec(TreeNode<T> node, T data) {
        if (node == null) return new TreeNode<>(data);
        int cmp = data.compareTo(node.getData());
        if (cmp < 0)
            node.setLeft(addRec(node.getLeft(), data));
        else if (cmp > 0)
            node.setRight(addRec(node.getRight(), data));
        // if cmp == 0 duplicates are not inserted
        return node;
    }

    public List<T> inorder() {
        List<T> list = new ArrayList<>();
        inorderRec(root, list);
        return list;
    }
    private void inorderRec(TreeNode<T> node, List<T> list) {
        if (node == null) return;
        inorderRec(node.getLeft(), list);
        list.add(node.getData());
        inorderRec(node.getRight(), list);
    }

    public List<T> preorder() {
        List<T> list = new ArrayList<>();
        preorderRec(root, list);
        return list;
    }
    private void preorderRec(TreeNode<T> node, List<T> list) {
        if (node == null) return;
        list.add(node.getData());
        preorderRec(node.getLeft(), list);
        preorderRec(node.getRight(), list);
    }

    public List<T> postorder() {
        List<T> list = new ArrayList<>();
        postorderRec(root, list);
        return list;
    }
    private void postorderRec(TreeNode<T> node, List<T> list) {
        if (node == null) return;
        postorderRec(node.getLeft(), list);
        postorderRec(node.getRight(), list);
        list.add(node.getData());
    }


    public boolean dataExists(T data) {
        return dataExistsRec(root, data);
    }
    private boolean dataExistsRec(TreeNode<T> node, T data) {
        if (node == null) return false;
        int cmp = data.compareTo(node.getData());
        if (cmp == 0) return true;
        return cmp < 0
            ? dataExistsRec(node.getLeft(), data)
            : dataExistsRec(node.getRight(), data);
    }

    public int getWeight() {
        return getWeightRec(root);
    }
    private int getWeightRec(TreeNode<T> node) {
        if (node == null) return 0;
        return 1 + getWeightRec(node.getLeft()) + getWeightRec(node.getRight());
    }

    public int getHeight() {
        return getHeightRec(root);
    }
    private int getHeightRec(TreeNode<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(getHeightRec(node.getLeft()), getHeightRec(node.getRight()));
    }

    public int getLevel(T data) {
        return getLevelRec(root, data, 1);
    }
    private int getLevelRec(TreeNode<T> node, T data, int level) {
        if (node == null) return -1;   // not found
        int cmp = data.compareTo(node.getData());
        if (cmp == 0) return level;
        return cmp < 0
            ? getLevelRec(node.getLeft(), data, level + 1)
            : getLevelRec(node.getRight(), data, level + 1);
    }

    public int countLeaves() {
        return countLeavesRec(root);
    }
    private int countLeavesRec(TreeNode<T> node) {
        if (node == null) return 0;
        if (node.getLeft() == null && node.getRight() == null) return 1;
        return countLeavesRec(node.getLeft()) + countLeavesRec(node.getRight());
    }

    public T getMin() {
        if (isEmpty()) return null;
        return getMinRec(root);
    }
    private T getMinRec(TreeNode<T> node) {
        return node.getLeft() == null
            ? node.getData()
            : getMinRec(node.getLeft());
    }

    public T getMax() {
        if (isEmpty()) return null;
        return getMaxRec(root);
    }
    private T getMaxRec(TreeNode<T> node) {
        return node.getRight() == null
            ? node.getData()
            : getMaxRec(node.getRight());
    }

    public List<List<T>> levelOrderTraversal() {
        List<List<T>> levels = new ArrayList<>();
        if (isEmpty()) return levels;

        Queue<TreeNode<T>> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int size = queue.size();
            List<T> level = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                TreeNode<T> current = queue.poll();
                level.add(current.getData());
                if (current.getLeft() != null) queue.add(current.getLeft());
                if (current.getRight() != null) queue.add(current.getRight());
            }
            levels.add(level);
        }
        return levels;
    }

    public void delete(T data) {
        root = deleteRec(root, data);
    }
    private TreeNode<T> deleteRec(TreeNode<T> node, T data) {
        if (node == null) return null;
        int cmp = data.compareTo(node.getData());
        if (cmp < 0) {
            node.setLeft(deleteRec(node.getLeft(), data));
        } else if (cmp > 0) {
            node.setRight(deleteRec(node.getRight(), data));
        } else {
            // Node found - 3 cases
            if (node.getLeft() == null) return node.getRight();
            if (node.getRight() == null)   return node.getLeft();
            // Tiene dos hijos: reemplazar con el menor del subárbol derecho
            T sucesor = getMinRec(node.getRight());
            node.setData(sucesor);
            node.setRight(deleteRec(node.getRight(), sucesor));
        }
        return node;
    }

    public void deleteTree() {
        root = null;
    }

    public void cascadeDelete(TreeNode<T> currentNode) {
    if (currentNode == null) return;

    cascadeDelete(currentNode.getLeft());
    cascadeDelete(currentNode.getRight());

    currentNode.setLeft(null);
    currentNode.setRight(null);
    }

    public void addMultipleNodes(T... values) {
    if (values == null || values.length == 0) {
        return;
    }

    for (T value : values) {
        add(value);
    }
    }

    public TreeNode<T> getRoot() {
        return root;
    }
}