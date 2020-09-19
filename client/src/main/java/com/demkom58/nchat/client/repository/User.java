package com.demkom58.nchat.client.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class User {

    private String name = "GUEST";
    private int messagesSent = 0;

    public void setName(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public int getMessagesSent() {
        return this.messagesSent;
    }

    public void addSentMessage() {
        this.messagesSent += 1;
    }

}
