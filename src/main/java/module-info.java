module abbtree {
    requires javafx.controls;
    requires javafx.fxml;

    opens abbtree to javafx.fxml;
    exports abbtree;
}
