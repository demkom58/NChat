package com.demkom58.nchat.client.controller;

import com.demkom58.nchat.client.network.ClientMessenger;
import com.demkom58.nchat.client.repository.ServersRepository;
import com.demkom58.nchat.client.repository.StageRepository;
import com.demkom58.nchat.client.repository.User;
import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.handler.PacketEncoder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

@Component
@FxmlView("/assets/login.fxml")
public class LoginController extends SimpleViewController {

    @FXML private TextField ipField;
    @FXML private TextField loginField;
    @FXML private Label joinButton;
    @FXML private Label listButton;

    private final ClientMessenger messenger;
    private final ServersRepository serversRepository;
    private final User user; // TODO: user service here

    @Getter
    @Setter
    private String ip = "127.0.0.1";

    @Autowired
    public LoginController(FxWeaver weaver, StageRepository stageRepository, ClientMessenger messenger, ServersRepository serversRepository, User user) {
        super(weaver, stageRepository);
        this.messenger = messenger;
        this.serversRepository = serversRepository;
        this.user = user;
    }

    @Override
    public void show() {
        super.show();
        ipField.setText(ip);
    }

    /**
     * IP list button pressed.
     */
    @FXML
    public void onList(MouseEvent event) {
        weaver.loadController(ListController.class).show();
    }

    /**
     * Join button pressed.
     */
    @FXML
    public void onJoin(MouseEvent event) {
        login();
    }

    /**
     * On Enter pressed (Login)
     */
    @FXML
    public void onEnter(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER))
            login();
    }

    private void login() {
        String nick = loginField.getText();
        String voidName = nick.replace(" ", "");
        String frameSymbol = PacketEncoder.getFrameSymbol();

        if (nick.contains(frameSymbol))
            loginField.setText(nick.replace(frameSymbol, ""));

        if (nick.length() == 0 || nick.length() > 16 || voidName.length() == 0)
            return;

        user.setName(nick);

        ChatController chatController = weaver.loadController(ChatController.class);
        chatController.getMessagesView().getItems().clear();
        chatController.getMessagesView().refresh();
        chatController.show();

        String fullIp = ipField.getText().isEmpty() ? Environment.STANDARD_IP : ipField.getText();
        String[] split = fullIp.split(Pattern.quote(":"));
        InetSocketAddress serverAddress = new InetSocketAddress(
                split[0],
                split.length < 2 ? Environment.PORT : Integer.parseInt(split[1])
        );

        serversRepository.saveServer(fullIp);

        final Thread thread = new Thread(() -> {
            try {
                messenger.run(serverAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public Label getListButton() {
        return listButton;
    }

    public Label getJoinButton() {
        return joinButton;
    }

    public TextField getLoginField() {
        return loginField;
    }

}
