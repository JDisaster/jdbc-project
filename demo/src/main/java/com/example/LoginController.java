package com.example;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField studentIDField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() throws IOException {
        String studentID = studentIDField.getText();
        String password = passwordField.getText();

        // temp bypass login for testing
        App.setCurrentStudentId(studentID); 
        App.setRoot("primary");
    }

    @FXML
    private void goToSignup() throws IOException {
        App.setRoot("signup");
    }
}
