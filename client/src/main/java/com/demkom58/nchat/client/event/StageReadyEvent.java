package com.demkom58.nchat.client.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class StageReadyEvent extends ApplicationEvent {
    private final Stage stage;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param stage the object on which the event initially occurred or with
     *              which the event is associated (never {@code null})
     */
    public StageReadyEvent(Stage stage) {
        super(stage);
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
