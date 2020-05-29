package com.demkom58.nchat.client.gui;

import com.demkom58.divine.gui.GuiController;
import com.demkom58.nchat.client.Client;
import com.demkom58.nchat.client.network.ClientMessenger;
import com.demkom58.nchat.client.network.User;
import com.demkom58.nchat.client.util.DataIP;
import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.handler.PacketEncoder;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.regex.Pattern;

public class LoginController extends NGuiController {

    @FXML private TextField ipField;
    @FXML private TextField loginField;
    @FXML private Label joinButton;
    @FXML private Label listButton;

    private Client client;

    /**
     * IP list button pressed.
     */
    @FXML
    public void onList(MouseEvent event) {
        final ListController controller = getGuiManager().getController(ListController.class);

        if (controller == null)
            throw new NullPointerException("List controller not found!");

        final ObservableList<String> ips = controller.getIpList().getItems();
        if (ips != null) {
            ips.clear();
            ips.addAll(DataIP.loadIPList());
        }

        getGuiManager().setGui(controller);
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

        client.setUser(new User(nick));
        updateGuiNick(nick);

        ChatController chatController = Objects.requireNonNull(getGuiManager().getController(ChatController.class));
        chatController.getMessagesView().getItems().clear();
        chatController.getMessagesView().refresh();
        getGuiManager().setGui(chatController);

        String fullIp = ipField.getText().isEmpty() ? Environment.STANDARD_IP : ipField.getText();
        String[] split = fullIp.split(Pattern.quote(":"));
        InetSocketAddress serverAddress = new InetSocketAddress(
                split[0],
                split.length < 2 ? Environment.PORT : Integer.parseInt(split[1])
        );

        DataIP.saveIP(fullIp);

        try {
            new Thread(() -> {
                ClientMessenger.setup(client, serverAddress);
                try {
                    ClientMessenger.getClientMessenger().run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public TextField getIpField() {
        return ipField;
    }

    public void updateGuiNick(String nick) {
        final String greeting = "HELLO " + nick.toUpperCase();

        getGuiManager().getGuiMap().values().forEach(e -> {
            final GuiController controller = e.getThird();

            if (controller instanceof NGuiController)
                ((NGuiController) controller).getHelloLabel().setText(greeting);

        });
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
