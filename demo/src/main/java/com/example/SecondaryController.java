package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}