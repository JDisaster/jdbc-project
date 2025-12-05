package com.example;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField studentEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() throws IOException {
        String email = studentEmailField.getText();
        String password = passwordField.getText();

        if (!Database.login(email, password)) {
            messageLabel.setText("Error logging in, check your email or password");
            return;
        }

        // store logged-in ID
        String studentID = Database.getStudentId(email);
        App.setCurrentStudentId(studentID);
        System.out.println("DEBUG handleLogin -> studentID=" + studentID);

        App.setRoot("primary");
    }


    @FXML
    private void goToSignup() throws IOException {
        App.setRoot("signup");
    }
}
