package com.baihudie.desktop.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class MainStageController implements Initializable {

    @FXML
    private Button btnNew;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private Label testLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    protected void handleBtnNewAction(ActionEvent event) {
        testLabel.setText("new");
    }

    @FXML
    protected void handleBtnEditAction(ActionEvent event) {
        testLabel.setText("edit");
    }

    @FXML
    protected void handleBtnDeleteAction(ActionEvent event) {
        testLabel.setText("delete");
    }

    public void handleBtnCreateAction(ActionEvent actionEvent) {

        FlowPane pane = new FlowPane();

        Scene scene = new Scene(pane, 200, 100);

        Stage newStage = new Stage();

        newStage.setScene(scene);

        //指定 stage 的模式
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setTitle("Pop");
        newStage.showAndWait();

    }
}
