package com.demkom58.nchat.client.event;

import org.springframework.context.ApplicationEvent;

public class ShutdownEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ShutdownEvent(Object source) {
        super(source);
    }
}
