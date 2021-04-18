module SaveShare.jfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires org.apache.logging.log4j;
    requires org.eclipse.jgit;

    opens com.alexhqi.saveshare to javafx.fxml;
    exports com.alexhqi.saveshare;
    exports com.alexhqi.saveshare.service.git to com.fasterxml.jackson.databind;
    exports com.alexhqi.saveshare.core to com.fasterxml.jackson.databind;
}
