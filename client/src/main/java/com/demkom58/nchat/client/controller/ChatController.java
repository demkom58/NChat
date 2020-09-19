package com.demkom58.nchat.client.controller;

import com.demkom58.nchat.client.network.ClientMessenger;
import com.demkom58.nchat.client.repository.StageRepository;
import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/assets/chat.fxml")
public class ChatController extends SimpleViewController {

    @FXML private ListView<String> messagesView;
    @FXML private Label sendButton;
    @FXML private Label backButton;
    @FXML private TextArea messageArea;

    private final ClientMessenger messenger;

    @Autowired
    public ChatController(FxWeaver weaver, StageRepository stageRepository, ClientMessenger messenger) {
        super(weaver, stageRepository);
        this.messenger = messenger;
    }

    /**
     * Return on Login page.
     */
    @FXML
    public void onBack(MouseEvent event) {
        messenger.sendPacket(new ADisconnectPacket("Returned in main menu."));
        messenger.close();

        weaver.loadController(LoginController.class).show();
    }

    /**
     * On Send button pressed.
     */
    @FXML
    public void onSend(MouseEvent event) {
        sendMessage();
    }

    /**
     * On Enter pressed (Send & New line).
     */
    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER) && !event.isShiftDown()) {
            sendMessage();
            return;
        }

        int pos = getMessageArea().getCaretPosition();
        String text = getMessageArea().getText();
        if (text.length() > Environment.MAX_MESSAGE_LENGTH) return;

        if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
            String start = getMessageArea().getText(0, pos);
            String end = getMessageArea().getText(pos, getMessageArea().getLength());
            String completed = start + "\n" + end;
            getMessageArea().setText(completed);
            getMessageArea().positionCaret(pos + 1);
        }
    }

    /**
     * Message length limitation.
     */
    @FXML
    public void onKeyTyped(KeyEvent event) {
        final int lengthLimit = 1000;
        final int pos = getMessageArea().getCaretPosition();

        Platform.runLater(() -> {
            String nextText = getMessageArea().getText();
            if (nextText.length() < lengthLimit + 1) return;
            getMessageArea().setText(nextText.substring(0, lengthLimit));
            getMessageArea().positionCaret(pos);
        });
    }

    /**
     * Check message & send message to server.
     */
    private void sendMessage() {
        String message = getMessageArea().getText();
        if (message.length() < 1 || message.replace(" ", "").length() < 1)
            return;

        messenger.sendMessage(message.trim());
        Platform.runLater(() -> getMessageArea().clear());
    }

    public ListView<String> getMessagesView() {
        return messagesView;
    }

    public Label getSendButton() {
        return sendButton;
    }

    public Label getBackButton() {
        return backButton;
    }

    public TextArea getMessageArea() {
        return messageArea;
    }

}
