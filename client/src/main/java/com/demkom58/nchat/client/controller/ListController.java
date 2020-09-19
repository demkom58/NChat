package com.demkom58.nchat.client.controller;

import com.demkom58.nchat.client.repository.ServersRepository;
import com.demkom58.nchat.client.repository.StageRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.MouseEvent;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/assets/list.fxml")
public class ListController extends SimpleViewController {

    @FXML
    private ListView<String> ipList;
    @FXML
    private Label selectButton;
    @FXML
    private Label deleteButton;

    private final ServersRepository serversRepository;

    @Autowired
    public ListController(FxWeaver weaver, StageRepository stageRepository, ServersRepository serversRepository) {
        super(weaver, stageRepository);
        this.serversRepository = serversRepository;
    }

    /**
     * Select IP button pressed.
     */
    @FXML
    public void onSelect(MouseEvent event) {
        final MultipleSelectionModel<String> selectionModel = ipList.getSelectionModel();
        final ObservableList<String> list = selectionModel.getSelectedItems();

        final LoginController loginController = weaver.loadController(LoginController.class);
        if (list.isEmpty()) {
            loginController.show();
            return;
        }

        loginController.setIp(selectionModel.getSelectedItems().get(0));
        loginController.show();
    }

    /**
     * Remove IP button pressed.
     */
    @FXML
    public void onRemove(MouseEvent event) {
        ipList.getItems().remove(ipList.getSelectionModel().getSelectedItems().get(0));
        ipList.refresh();
        serversRepository.saveAllServers(ipList.getItems());
    }

    public ListView<String> getIpList() {
        return ipList;
    }

    public Label getSelectButton() {
        return selectButton;
    }

    public Label getDeleteButton() {
        return deleteButton;
    }

    @Override
    public void show() {
        super.show();

        ObservableList<String> ips = ipList.getItems();
        if (ips == null)
            ipList.setItems(ips = FXCollections.observableArrayList());
        else
            ips.clear();

        ips.addAll(serversRepository.loadAllServer());
    }

}
