package com.demkom58.nchat.client.repository;

import javafx.stage.Stage;
import org.springframework.stereotype.Service;

@Service
public class StageRepository {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
