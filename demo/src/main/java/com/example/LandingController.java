package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class LandingController {

    @FXML
    private void goToPrimary() throws IOException {
        App.setRoot("primary");
    }

    @FXML
    private void goToLogin() throws IOException {
        App.setRoot("login");
    }
}
