/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;

/**
 *
 * @author Spirors
 */
public class ConfigWindow extends Stage implements Dialog{
    
    private static ConfigWindow configWindow;
    
    private TextField iterationField;
    private TextField updateIntervalField;
    private CheckBox continuousRunBox;
    private TextField clusterField;
    
    private ArrayList<Integer> classification;
    private ArrayList<Integer> clustering;
    
    private boolean hideCluster;
    
    private VBox configPane;
    private Text error;
    
    private Label windowTitle = new Label();
    
    private ConfigWindow(){}
    
    public static ConfigWindow getDialog() {
        configWindow = new ConfigWindow();
        return configWindow;
    }
    
    private void setWinTitle(String message) {
        this.windowTitle.setText(message);
    }
    public ArrayList<Integer> getClassification(){
        return classification;
    }
    public ArrayList<Integer> getClustering(){
        return clustering;
    }
    
    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        PropertyManager manager     = PropertyManager.getManager();
        Button          saveButton = new Button(manager.getPropertyValue(PropertyTypes.SAVE_WORK_TITLE.name()));
        configPane = new VBox();
        
        configPane.setAlignment(Pos.CENTER);
        configPane.getChildren().add(windowTitle);
        
        Text iterationT = new Text("Max. Iteration: ");
        Text updateIntervalT = new Text("Update Interval: ");
        Text continuousRunT = new Text("Continuous Run? ");
        Text clusterT= new Text("Clusters: ");
        
        setDefaultValue();
        
        iterationField = new TextField();
        updateIntervalField = new TextField();
        continuousRunBox = new CheckBox();
        clusterField = new TextField();
        
        HBox iterationPane = new HBox();
        iterationPane.getChildren().addAll(iterationT, iterationField);
        HBox updateIntervalPane = new HBox();
        updateIntervalPane.getChildren().addAll(updateIntervalT, updateIntervalField);
        HBox continuousRunPane = new HBox();
        continuousRunPane.getChildren().addAll(continuousRunT, continuousRunBox);
        if(hideCluster == false){
            HBox clusterPane = new HBox();
            clusterPane.getChildren().addAll(clusterT, clusterField);
            configPane.getChildren().addAll(iterationPane, updateIntervalPane, clusterPane, continuousRunPane);
        }else
            configPane.getChildren().addAll(iterationPane, updateIntervalPane, continuousRunPane);
        
        configPane.setPadding(new Insets(40, 60, 40, 60));
        configPane.setSpacing(20);

        saveButton.setOnAction(e->{
            try {
                hideError();
                int iteration = Integer.parseInt(iterationField.getText());
                int updateInterval = Integer.parseInt(updateIntervalField.getText());
                int continuousRun;
                if(continuousRunBox.isSelected() == true)
                    continuousRun = 1;
                else
                    continuousRun = 0;
                if(hideCluster == false){
                    int cluster = Integer.parseInt(clusterField.getText());
                    clustering.set(0, iteration);
                    clustering.set(1, updateInterval);
                    clustering.set(2, continuousRun);
                    clustering.set(3, cluster);
                }
                else{
                    classification.set(0, iteration);
                    classification.set(1, updateInterval);
                    classification.set(2, continuousRun);
                }
                this.close();
            }catch(Exception ex){
                showError();
            }
        });
        configPane.getChildren().add(saveButton);
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
                updateIntervalField.setText(newValue);
            }
            else{
                updateIntervalField.setText("1");
            }
        });
        clusterField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("[1-9][0-9]*")) { 
                clusterField.setText(newValue);
            }
            else{
                clusterField.setText("1");
            }
        });
        this.setScene(new Scene(configPane));
    }
    
    @Override
    public void show(String configTitle, String windowTitle) {
        setTitle(configTitle);
        setWinTitle(windowTitle);
        if(hideCluster == true){
            iterationField.setText(Integer.toString(classification.get(0)));
            updateIntervalField.setText(Integer.toString(classification.get(1)));
            if(classification.get(2) == 1)
                continuousRunBox.setSelected(true);
            else
                continuousRunBox.setSelected(false);
        }else{
            iterationField.setText(Integer.toString(clustering.get(0)));
            updateIntervalField.setText(Integer.toString(clustering.get(1)));
            if(clustering.get(2) == 1)
                continuousRunBox.setSelected(true);
            else
                continuousRunBox.setSelected(false);
            clusterField.setText(Integer.toString(clustering.get(3)));
        }
        showAndWait();
    }
    public void hideCluster(boolean a){
        hideCluster = a;
    }
    private void showError(){
        error = new Text();
        error.setText("Please Fill in the Text Field Accordingily!");
        error.setFill(Color.RED);
        configPane.getChildren().add(error);
    }
    private void hideError(){
        configPane.getChildren().remove(error);
    }
    private void setDefaultValue(){
        classification = new ArrayList<Integer>();
        classification.add(1);
        classification.add(1);
        classification.add(0);
        
        clustering = new ArrayList<Integer>();
        clustering.add(1);
        clustering.add(1);
        clustering.add(0);
        clustering.add(1);
    }
    public void setClassification(ArrayList<Integer> classification){
        if(!(classification == null))
            if(!classification.isEmpty())
                this.classification = classification;
    }
    public void setClustering(ArrayList<Integer> clustering){
        if(!(clustering == null))
            if(!clustering.isEmpty())
                this.clustering = clustering;
    }
}