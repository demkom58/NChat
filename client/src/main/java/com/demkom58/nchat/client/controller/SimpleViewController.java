package com.demkom58.nchat.client.controller;

import com.demkom58.nchat.client.repository.StageRepository;
import com.demkom58.nchat.client.repository.User;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;
import net.rgielen.fxweaver.core.FxWeaver;

public abstract class SimpleViewController implements ViewController {
    protected final FxWeaver weaver;
    protected final StageRepository stageRepository;

    @FXML private Label helloLabel;
    @FXML private Label close;

    protected SimpleViewController(FxWeaver weaver, StageRepository stageRepository) {
        this.weaver = weaver;
        this.stageRepository = stageRepository;
    }

    @Override
    public void show() {
        final Stage stage = stageRepository.getStage();
        final Region value = weaver.loadView(getClass());
        stage.getScene().setRoot(value);

        // TODO: fix this crutch, it should work just with "stage.sizeToScene()"
        stage.setWidth(value.getPrefWidth());
        stage.setHeight(value.getPrefHeight());

        final String name = weaver.getBean(User.class).getName();
        helloLabel.setText("HELLO " + name + "!");
    }

    @FXML
    @SneakyThrows
    public void onClose(MouseEvent event) {
        EventHandler<ActionEvent> handler = h -> {
            try {
                weaver.getBean(Application.class).stop();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        };

        FadeTransition transition = new FadeTransition();
        transition.setDuration(Duration.seconds(0.25));
        transition.setFromValue(100);
        transition.setToValue(0);
        transition.setNode(stageRepository.getStage().getScene().getRoot());
        transition.setOnFinished(handler);
        transition.play();
    }

}
