package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static java.io.File.separator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private TextArea                     hidden;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private VBox                         leftPanel;
    private VBox                         algorithmTypePanel;
    private Button                       toggle;
    private boolean                      invalid; 
    private Text                         dataDetail;
    
    private boolean hasTwoLabels = true;

    public LineChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(separator,
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true);
        toolBar.getItems().add(scrnshotButton);
        newButton.setDisable(false);
        saveButton.setDisable(false);
        toolBar.getItems().remove(printButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions)(applicationTemplate.getActionComponent())).handleScreenshotRequest();
            } catch (IOException ex) {
                Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        hidden.clear();
        textArea.clear();
        chart.getData().clear();
    }

    public String getCurrentText() { return textArea.getText()+hidden.getText(); }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        NumberAxis      yAxis   = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        chart.getStylesheets().add(manager.getPropertyValue(AppPropertyTypes.CHART_STYLE_PATH.name()));
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalZeroLineVisible(false);
        chart.getXAxis().setOpacity(0);
        chart.getYAxis().setOpacity(0);

        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.85);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.85);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);
        
        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
    }
    public void showData(){
        PropertyManager manager = applicationTemplate.manager;
        saveButton.setDisable(true);
        leftPanel.getChildren().clear();
        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));
        
        textArea = new TextArea();
        textArea.setDisable(true);
        hidden = new TextArea();
        
        leftPanel.getChildren().addAll(leftPanelTitle, textArea);
        setTextAreaActions();
    }
    public void showToggle(){
        PropertyManager manager = applicationTemplate.manager;
        toggle = new Button("Done");
        
        leftPanel.getChildren().addAll(toggle);
        setToggleButtonActions();
    }
    public void showDetail(int lineCount, String dataFilePath, Map<String, String> labels){
        PropertyManager manager = applicationTemplate.manager;
        String s ="";
        int countLabel = 0;
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if(!s.contains(entry.getValue())){
                s+="-"+entry.getValue()+"\n";
                countLabel++;
            }
        }
        if(countLabel < 2)
            hasTwoLabels = false;
        
        dataDetail = new Text("There are "+lineCount + " instances with " + labels.size() + " label(s) loaded from: " +
                dataFilePath + "\n\n The Label(s) are: \n"+ s);
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()))-3;
        dataDetail.setFont(Font.font(fontname, fontsize));
        
        leftPanel.getChildren().add(dataDetail);
        dataDetail.wrappingWidthProperty().bind(leftPanel.widthProperty());
    }
    public void clearDetail(){
        leftPanel.getChildren().remove(dataDetail);
    }
    public void showAlgorithmType(){
        PropertyManager manager = applicationTemplate.manager;
        algorithmTypePanel = new VBox();
        Text algoTitle = new Text("Algorithm Types:");
        
        Button classification = new Button("Classification");
        Button clustering = new Button("Clustering");
        algorithmTypePanel.getChildren().addAll(algoTitle, classification, clustering);
        leftPanel.getChildren().add(algorithmTypePanel);
        if(hasTwoLabels == false)
            classification.setDisable(true);
    }
    public void clearAlgorithmType(){
        leftPanel.getChildren().remove(algorithmTypePanel);
    }

    private void setWorkspaceActions() {
//        setTextAreaActions();
//        setDisplayButtonActions();
//        readOnly.selectedProperty().addListener(new ChangeListener<Boolean>(){
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
//                textArea.setDisable(newValue);
//            }
//        });
    }
    private void setToggleButtonActions(){
        toggle.setOnAction(e -> {
            invalid = false;
            if(toggle.getText().equals("Done")){
                AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                dataComponent.clear();
                dataComponent.loadData(textArea.getText());
                if(!invalid){    
                    toggle.setText("Edit");
                    textArea.setDisable(true);
                    showDetail(getLineCount(), "User Input of Text Area", dataComponent.getLabels());
                    showAlgorithmType();
                }
            }
            else{
                toggle.setText("Done");
                textArea.setDisable(false);
                clearDetail();
                clearAlgorithmType();
            }
                
        });
    }
    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
        try {
            if (!newValue.equals(oldValue)) {
                if (!newValue.isEmpty()) {
                    ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                    if (newValue.charAt(newValue.length() - 1) == '\n')
                        hasNewText = true;
                        //newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        //newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println(newValue);
        }
        });
    }

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if (hasNewText) {
                try {
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    dataComponent.loadData(textArea.getText()+hidden.getText());
                    dataComponent.displayData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if(!chart.getData().isEmpty())
                scrnshotButton.setDisable(false);
            }
        });
    }
    public Button getSaveButton(){
        return saveButton;
    }
    public Button getScreenShotButton(){
        return scrnshotButton;
    }
    public TextArea getTextArea(){
        return textArea;
    }
    public TextArea getHidden(){
        return hidden;
    }
    public void setInvalidProperty(boolean b){
        invalid = b;
    }
    public int getLineCount(){
        return textArea.getText().split("\n").length;
    }
}
