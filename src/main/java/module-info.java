module com.timer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    
    exports com.timer;
    exports com.timer.controller;
    exports com.timer.model;
    exports com.timer.service;
    
    opens com.timer.controller to javafx.fxml;
}
