/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;

/**
 *
 * @author Spirors
 */
public class ConfigWindow extends Stage {
    
    private static ConfigWindow configWindow;
    
    private int iteration;
    private int updateInterval;
    private boolean continuousRun;
    private int cluster;
    
    public ConfigWindow(){}
    
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        PropertyManager manager     = PropertyManager.getManager();
        Button          closeButton = new Button(manager.getPropertyValue(PropertyTypes.CLOSE_LABEL.name()));
        Button          saveButton = new Button(manager.getPropertyValue(PropertyTypes.SAVE_WORK_TITLE.name()));
        VBox            configPane = new VBox();
        
        Text iterationT = new Text("iteration: ");
        Text updateIntervalT = new Text("updateInterval: ");
        Text continuousRunT = new Text("continuousRun: ");
        Text clusterT= new Text("cluster: ");
        
        TextField       iterationField = new TextField();
        TextField       updateIntervalField = new TextField();
        CheckBox        continuousRunBox = new CheckBox();
        TextField       clusterField = new TextField();
        
        HBox iterationPane = new HBox();
        iterationPane.getChildren().addAll(iterationT, iterationField);
        HBox updateIntervalPane = new HBox();
        updateIntervalPane.getChildren().addAll(updateIntervalT, updateIntervalField);
        HBox continuousRunPane = new HBox();
        continuousRunPane.getChildren().addAll(continuousRunT, continuousRunBox);
        HBox clusterPane = new HBox();
        clusterPane.getChildren().addAll(clusterT, clusterField);
        
        configPane.getChildren().addAll(iterationPane, updateIntervalPane, continuousRunPane, clusterPane);
        configPane.setPadding(new Insets(80, 60, 80, 60));
        configPane.setSpacing(20);

        closeButton.setOnAction(e -> this.close());
        saveButton.setOnAction(e->{
            iteration = Integer.parseInt(iterationField.getText());
            updateInterval = Integer.parseInt(updateIntervalField.getText());
            continuousRun = continuousRunBox.isSelected();
            cluster = Integer.parseInt(clusterField.getText());
        });
        iterationField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("[1-9][0-9]*")) { 
                iterationField.setText(newValue);
            }
            else{
                iterationField.setText("1");
            }
        });
        updateIntervalField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("[1-9][0-9]*")) { 
                iterationField.setText(newValue);
            }
            else{
                iterationField.setText("1");
            }
        });
        clusterField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("[1-9][0-9]*")) { 
                iterationField.setText(newValue);
            }
            else{
                iterationField.setText("1");
            }
        });
        this.setScene(new Scene(configPane));
    }
    public void show(String configTitle) {
        setTitle(configTitle);
        showAndWait();
    }
}
