module arbolabb {
    requires javafx.controls;
    requires javafx.fxml;

    opens arbolabb to javafx.fxml;
    exports arbolabb;
}
