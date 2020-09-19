package com.demkom58.nchat.client.application;

import com.demkom58.nchat.client.controller.LoginController;
import com.demkom58.nchat.client.event.StageReadyEvent;
import com.demkom58.nchat.client.repository.StageRepository;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {
    private final FxWeaver weaver;
    private final StageRepository stageRepository;
    private final String windowTitle;

    public double xOffset;
    public double yOffset;

    @Autowired
    public PrimaryStageInitializer(FxWeaver weaver,
                                   StageRepository stageRepository,
                                   @Value("${ui.title:NChat}") String windowTitle) {
        this.weaver = weaver;
        this.stageRepository = stageRepository;
        this.windowTitle = windowTitle;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        final Stage stage = event.getStage();
        stage.setResizable(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(windowTitle);

        final Scene scene = new Scene(new VBox(), 400, 300);
        scene.setFill(Color.TRANSPARENT);

        scene.setOnMousePressed(e -> {
            xOffset = stage.getX() - e.getScreenX();
            yOffset = stage.getY() - e.getScreenY();
        });
        scene.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() + xOffset);
            stage.setY(e.getScreenY() + yOffset);
        });

        stage.setScene(scene);
        stageRepository.setStage(stage);

        try {
            weaver.loadController(LoginController.class).show();

            final FadeTransition transition = new FadeTransition();
            transition.setDuration(Duration.seconds(0.25));
            transition.setFromValue(0);
            transition.setToValue(100);
            transition.setNode(scene.getRoot());

            stage.show();
            transition.play();
        } catch (Exception e) {
            e.printStackTrace();
            stage.close();
        }
    }

}
