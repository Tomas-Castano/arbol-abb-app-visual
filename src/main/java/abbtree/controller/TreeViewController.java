package abbtree.controller;

import abbtree.model.BSTTree;
import abbtree.model.TreeNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.*;

public class TreeViewController {

    @FXML private Pane viewportPane;
    @FXML private Pane treePane;
    @FXML private ListView<Text> logListView;
    @FXML private TextField inputField, randomInput;
    @FXML private Slider speedSlider;
    @FXML private Label statusLabel;
    @FXML private Button btnInsert, btnSearch, btnDelete, btnBalance, btnRandom;
    @FXML private Button btnPreorder, btnInorder, btnPostorder, btnClear, btnCenter;

    private BSTTree<Integer> bst = new BSTTree<>();
    private Map<TreeNode<Integer>, StackPane> nodeUIMap = new HashMap<>();
    private boolean isAnimating = false;

    // Layout configuration
    private int inorderCounter = 0;
    private final Map<TreeNode<Integer>, Point2D> logicalPositions = new HashMap<>();
    private final double H_GAP = 45; // Horizontal gap between inorder neighbors
    private final double V_GAP = 60; // Vertical gap between depth levels

    // Zoom and Pan variables
    private double scale = 1.0;
    private double dragStartX, dragStartY;
    private double paneStartX, paneStartY;

    private final Color ACCENT_COLOR = Color.web("#1dd3b0");
    private final Color NODE_BG = Color.web("#1b1d22");
    private final Color LINE_COLOR = Color.web("#4b5059");
    private final Color TRAVERSAL_COLOR = Color.web("#9d4edd");
    private final Color SEARCH_COLOR = Color.web("#f5b700");
    private final Color FOUND_COLOR = Color.web("#38b000");

    @FXML
    public void initialize() {
        log("System Ready.", LogType.INFO);
        setupZoomAndPan();
    }

    private void setupZoomAndPan() {
        // Clip viewport so tree doesn't draw over Sidebars
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(viewportPane.widthProperty());
        clip.heightProperty().bind(viewportPane.heightProperty());
        viewportPane.setClip(clip);

        // Panning
        viewportPane.setOnMousePressed(e -> {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            paneStartX = treePane.getTranslateX();
            paneStartY = treePane.getTranslateY();
            viewportPane.setCursor(javafx.scene.Cursor.CLOSED_HAND);
        });

        viewportPane.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - dragStartX;
            double dy = e.getSceneY() - dragStartY;
            treePane.setTranslateX(paneStartX + dx);
            treePane.setTranslateY(paneStartY + dy);
        });

        viewportPane.setOnMouseReleased(e -> viewportPane.setCursor(javafx.scene.Cursor.DEFAULT));

        // Zooming
        viewportPane.setOnScroll(e -> {
            double zoomFactor = 1.1;
            if (e.getDeltaY() < 0) zoomFactor = 1 / zoomFactor;
            
            double newScale = scale * zoomFactor;
            // Limit zoom scale between 0.1 and 5.0
            if (newScale >= 0.1 && newScale <= 5.0) {
                scale = newScale;
                treePane.setScaleX(scale);
                treePane.setScaleY(scale);
            }
            e.consume();
        });
    }

    @FXML
    public void handleCenterView() {
        if (bst.isEmpty()) return;
        scale = 1.0;
        treePane.setScaleX(1.0);
        treePane.setScaleY(1.0);

        Point2D rootPos = logicalPositions.get(bst.getRoot());
        if (rootPos != null) {
            double rootX = rootPos.getX() * H_GAP;
            double centerX = viewportPane.getWidth() / 2;
            treePane.setTranslateX(centerX - rootX);
            treePane.setTranslateY(50);
        }
    }

    @FXML public void handleInsert() { processOperation(Operation.INSERT); }
    @FXML public void handleSearch() { processOperation(Operation.SEARCH); }
    @FXML public void handleDelete() { processOperation(Operation.DELETE); }

    @FXML
    public void handleClear() {
        if (isAnimating) return;
        bst.deleteTree();
        redrawTree();
        treePane.setTranslateX(0);
        treePane.setTranslateY(0);
        log("Tree cleared.", LogType.WARNING);
    }

    @FXML
    public void handleGenerateRandom() {
        if (isAnimating) return;
        String text = randomInput.getText();
        try {
            int count = Integer.parseInt(text);
            if (count <= 0 || count > 200) {
                log("Please enter a number between 1 and 200", LogType.ERROR);
                return;
            }
            bst.deleteTree();
            Random rand = new Random();
            for (int i = 0; i < count; i++) {
                bst.add(rand.nextInt(1000));
            }
            redrawTree();
            handleCenterView(); // Auto-center after generation
            log("Generated random tree with " + count + " nodes", LogType.SUCCESS);
            randomInput.clear();
        } catch (NumberFormatException e) {
            log("Invalid number for random nodes", LogType.ERROR);
        }
    }

    @FXML
    public void handleBalance() {
        if (isAnimating || bst.isEmpty()) return;
        runWithAnimation(() -> {
            log("Starting Rebalance operation", LogType.ACTION);
            List<Integer> sorted = bst.inorder();
            log("Extracted values: " + sorted, LogType.INFO);
            Platform.runLater(() -> {
                bst.deleteTree();
                redrawTree();
            });
            sleep(getDelay() * 2);
            log("Rebuilding balanced tree...", LogType.ACTION);
            buildBalanced(sorted, 0, sorted.size() - 1);
            log("Tree Balanced successfully.", LogType.SUCCESS);
            Platform.runLater(() -> {
                redrawTree();
                handleCenterView();
            });
        });
    }

    @FXML public void handlePreorder() { executeTraversal("Preorder", this::animatePreorder); }
    @FXML public void handleInorder() { executeTraversal("Inorder", this::animateInorder); }
    @FXML public void handlePostorder() { executeTraversal("Postorder", this::animatePostorder); }

    private void executeTraversal(String name, TraversalFunction traversal) {
        if (isAnimating || bst.isEmpty()) {
            log("Tree is empty or busy.", LogType.WARNING);
            return;
        }
        runWithAnimation(() -> {
            log("Starting " + name + " Traversal", LogType.ACTION);
            List<Integer> result = new ArrayList<>();
            traversal.traverse(bst.getRoot(), result);
            log(name + " Result: " + result, LogType.SUCCESS);
        });
    }

    private void processOperation(Operation op) {
        if (isAnimating) return;
        String text = inputField.getText();
        if (text == null || text.isEmpty()) return;

        try {
            int val = Integer.parseInt(text);
            inputField.clear();
            runWithAnimation(() -> {
                if (op == Operation.INSERT) animateInsert(val);
                else if (op == Operation.SEARCH) animateSearch(val);
                else if (op == Operation.DELETE) animateDelete(val);
            });
        } catch (NumberFormatException e) {
            log("Please enter a valid integer.", LogType.ERROR);
        }
    }

    private void animateInsert(int val) {
        log("Inserting: " + val, LogType.ACTION);
        TreeNode<Integer> current = bst.getRoot();

        if (current == null) {
            log("Tree empty. Root created.", LogType.INFO);
            bst.add(val);
            Platform.runLater(this::handleCenterView);
            return;
        }

        while (current != null) {
            highlightNode(current, SEARCH_COLOR);
            sleep(getDelay());
            
            if (val < current.getData()) {
                log(val + " < " + current.getData() + " → Left", LogType.INFO);
                unhighlightNode(current);
                if (current.getLeft() == null) break;
                current = current.getLeft();
            } else if (val > current.getData()) {
                log(val + " > " + current.getData() + " → Right", LogType.INFO);
                unhighlightNode(current);
                if (current.getRight() == null) break;
                current = current.getRight();
            } else {
                log("Value " + val + " already exists.", LogType.WARNING);
                unhighlightNode(current);
                return;
            }
        }
        log("Spot found. Inserted " + val, LogType.SUCCESS);
        bst.add(val);
    }

    private void animateSearch(int val) {
        log("Searching for: " + val, LogType.ACTION);
        TreeNode<Integer> current = bst.getRoot();

        while (current != null) {
            highlightNode(current, SEARCH_COLOR);
            sleep(getDelay());

            if (val == current.getData()) {
                log("Target " + val + " FOUND!", LogType.SUCCESS);
                highlightNode(current, FOUND_COLOR);
                sleep(getDelay() * 3);
                unhighlightNode(current);
                return;
            }

            if (val < current.getData()) {
                log(val + " < " + current.getData() + " → Left", LogType.INFO);
                unhighlightNode(current);
                current = current.getLeft();
            } else {
                log(val + " > " + current.getData() + " → Right", LogType.INFO);
                unhighlightNode(current);
                current = current.getRight();
            }
        }
        log("Target " + val + " not found.", LogType.ERROR);
    }

    private void animateDelete(int val) {
        log("Attempting deletion of: " + val, LogType.ACTION);
        animateSearch(val);
        if (bst.dataExists(val)) {
            bst.delete(val);
            log("Successfully deleted " + val, LogType.SUCCESS);
        }
    }

    private void buildBalanced(List<Integer> list, int start, int end) {
        if (start > end) return;
        int mid = (start + end) / 2;
        int val = list.get(mid);
        log("Inserting median: " + val, LogType.INFO);
        bst.add(val);
        Platform.runLater(this::redrawTree);
        sleep(getDelay());
        
        buildBalanced(list, start, mid - 1);
        buildBalanced(list, mid + 1, end);
    }

    private void animatePreorder(TreeNode<Integer> node, List<Integer> result) {
        if (node == null) return;
        processNodeVisual(node, result);
        animatePreorder(node.getLeft(), result);
        animatePreorder(node.getRight(), result);
    }

    private void animateInorder(TreeNode<Integer> node, List<Integer> result) {
        if (node == null) return;
        animateInorder(node.getLeft(), result);
        processNodeVisual(node, result);
        animateInorder(node.getRight(), result);
    }

    private void animatePostorder(TreeNode<Integer> node, List<Integer> result) {
        if (node == null) return;
        animatePostorder(node.getLeft(), result);
        animatePostorder(node.getRight(), result);
        processNodeVisual(node, result);
    }

    private void processNodeVisual(TreeNode<Integer> node, List<Integer> result) {
        highlightNode(node, TRAVERSAL_COLOR);
        log("Visited: " + node.getData(), LogType.INFO);
        result.add(node.getData());
        sleep(getDelay());
        unhighlightNode(node);
    }

    private void runWithAnimation(Runnable task) {
        isAnimating = true;
        Platform.runLater(() -> setControlsDisabled(true));
        new Thread(() -> {
            task.run();
            Platform.runLater(() -> {
                redrawTree();
                isAnimating = false;
                setControlsDisabled(false);
            });
        }).start();
    }

    private void redrawTree() {
        treePane.getChildren().clear();
        nodeUIMap.clear();
        logicalPositions.clear();
        inorderCounter = 0;

        if (!bst.isEmpty()) {
            calculatePositions(bst.getRoot(), 0);
            drawEdges(bst.getRoot());
            drawNodes(bst.getRoot());
        }
    }

    // Assigns an increasing X coordinate to each node by Inorder rank
    private void calculatePositions(TreeNode<Integer> node, int depth) {
        if (node == null) return;
        calculatePositions(node.getLeft(), depth + 1);
        logicalPositions.put(node, new Point2D(inorderCounter++, depth));
        calculatePositions(node.getRight(), depth + 1);
    }

    private void drawEdges(TreeNode<Integer> node) {
        if (node == null) return;
        Point2D p1 = logicalPositions.get(node);
        double x1 = p1.getX() * H_GAP;
        double y1 = p1.getY() * V_GAP;

        if (node.getLeft() != null) {
            Point2D p2 = logicalPositions.get(node.getLeft());
            drawLine(x1, y1, p2.getX() * H_GAP, p2.getY() * V_GAP);
            drawEdges(node.getLeft());
        }
        if (node.getRight() != null) {
            Point2D p2 = logicalPositions.get(node.getRight());
            drawLine(x1, y1, p2.getX() * H_GAP, p2.getY() * V_GAP);
            drawEdges(node.getRight());
        }
    }

    private void drawNodes(TreeNode<Integer> node) {
        if (node == null) return;
        Point2D pos = logicalPositions.get(node);
        double x = pos.getX() * H_GAP;
        double y = pos.getY() * V_GAP;

        StackPane uiNode = createUINode(node.getData().toString());
        // offset center point by circle radius
        uiNode.setLayoutX(x - 18);
        uiNode.setLayoutY(y - 18);
        treePane.getChildren().add(uiNode);
        nodeUIMap.put(node, uiNode);

        drawNodes(node.getLeft());
        drawNodes(node.getRight());
    }

    private void drawLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(LINE_COLOR);
        line.setStrokeWidth(2);
        treePane.getChildren().add(line);
    }

    private StackPane createUINode(String textVal) {
        Circle circle = new Circle(18);
        circle.setFill(NODE_BG);
        circle.setStroke(ACCENT_COLOR);
        circle.setStrokeWidth(2);

        Text text = new Text(textVal);
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        return new StackPane(circle, text);
    }

    private void highlightNode(TreeNode<Integer> node, Color color) {
        Platform.runLater(() -> {
            StackPane uiNode = nodeUIMap.get(node);
            if (uiNode != null) {
                Circle c = (Circle) uiNode.getChildren().get(0);
                c.setFill(color);
                c.setStroke(Color.WHITE);
            }
        });
    }

    private void unhighlightNode(TreeNode<Integer> node) {
        Platform.runLater(() -> {
            StackPane uiNode = nodeUIMap.get(node);
            if (uiNode != null) {
                Circle c = (Circle) uiNode.getChildren().get(0);
                c.setFill(NODE_BG);
                c.setStroke(ACCENT_COLOR);
            }
        });
    }

    private void log(String message, LogType type) {
        Platform.runLater(() -> {
            Text text = new Text("▶ " + message);
            text.getStyleClass().add(type.getStyleClass());
            logListView.getItems().add(text);
            logListView.scrollTo(logListView.getItems().size() - 1);
        });
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    private long getDelay() {
        return (long) (speedSlider.getValue() * 100); // 100ms to 1000ms delay scaling
    }

    private void setControlsDisabled(boolean disabled) {
        btnInsert.setDisable(disabled);
        btnSearch.setDisable(disabled);
        btnDelete.setDisable(disabled);
        btnBalance.setDisable(disabled);
        btnRandom.setDisable(disabled);
        btnPreorder.setDisable(disabled);
        btnInorder.setDisable(disabled);
        btnPostorder.setDisable(disabled);
        btnClear.setDisable(disabled);
        btnCenter.setDisable(disabled);
        statusLabel.setText(disabled ? "Status: Executing" : "Status: Idle");
        statusLabel.setTextFill(disabled ? SEARCH_COLOR : ACCENT_COLOR);
    }

    private enum Operation { INSERT, SEARCH, DELETE }

    private enum LogType {
        INFO("log-text-info"),
        SUCCESS("log-text-success"),
        WARNING("log-text-warning"),
        ERROR("log-text-error"),
        ACTION("log-text-action");

        private final String styleClass;
        LogType(String styleClass) { this.styleClass = styleClass; }
        public String getStyleClass() { return styleClass; }
    }

    @FunctionalInterface
    private interface TraversalFunction {
        void traverse(TreeNode<Integer> node, List<Integer> result);
    }
}