module SaveShare.jfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires SaveShare.saveapp;

    opens com.alexhqi.jfx to javafx.fxml;
    exports com.alexhqi.jfx;
}
