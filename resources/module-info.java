module com.music {
        requires javafx.controls;
        requires javafx.fxml;

        requires org.controlsfx.controls;
        requires java.desktop;
        requires javafx.media;

        opens com.music to javafx.fxml;
        exports com.music;
}