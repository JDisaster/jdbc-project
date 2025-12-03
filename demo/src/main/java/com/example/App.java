package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    // store the currently logged-in student ID
    private static String currentStudentId;

    public static void setCurrentStudentId(String id) {
        currentStudentId = id;
    }

    public static String getCurrentStudentId() {
        return currentStudentId;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // change "landing" to whatever your first screen is (e.g., "login")
        scene = new Scene(loadFXML("landing"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
