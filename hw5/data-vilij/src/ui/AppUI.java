package ui;

import actions.AppActions;
import classification.RandomClassifier;
import data.DataSet;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private TextArea                     textArea;       // text area for new data input
    private TextArea                     hidden;       // text area for new data input
    private VBox                         leftPanel;
    private VBox                         algorithmTypePanel;
    private Button                       toggle;
    private boolean                      invalid; 
    private Text                         dataDetail;
    
    private boolean                      hasTwoLabels;
    private Button                       classification;
    private Button                       clustering;
    private Button                       runBtn;
    
    private boolean                      hasInitialized;
    private boolean                      hasInitialClasConfig;
    private boolean                      hasInitialClusConfig;
    
    private HBox clusteringPane;
    private HBox classificationPane;
    
    private RandomClassifier rc;
    
    private boolean active = false;
    private boolean running = false;
    
    private ArrayList<Integer> classifierConfigInputs;

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
        saveButton.setDisable(true);
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
        hasTwoLabels = true;
        String s ="";
        int countLabel = 0;
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if(!s.contains(entry.getValue())){
                s+="-"+entry.getValue()+"\n";
                if(!entry.getValue().equals("null"))
                    countLabel++;
            }
        }
        if(countLabel != 2)
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
        
        classification = new Button("Classification");
        clustering = new Button("Clustering");
        algorithmTypePanel.getChildren().addAll(algoTitle, classification, clustering);
        leftPanel.getChildren().add(algorithmTypePanel);
        if(hasTwoLabels == false)
            classification.setDisable(true);
        setClassificationActions();
        setClusteringActions();
    }
    public void clearAlgorithmType(){
        leftPanel.getChildren().remove(algorithmTypePanel);
    }
    public void showClassAlgorithm(){
        PropertyManager manager = applicationTemplate.manager;
        clearAlgorithmType();
        classificationPane = new HBox();
        classificationPane.setSpacing(10);
        RadioButton rb1 = new RadioButton();
        rb1.setText("Random Classification");
        
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        
        Image configIcon = new Image(iconsPath+"/config.png", 20, 20, true, true);
        ImageView configBtn = new ImageView(configIcon);
        
        classificationPane.getChildren().addAll(rb1, configBtn);
        leftPanel.getChildren().add(classificationPane);
        ConfigWindow classificationConfig  = ConfigWindow.getDialog();
        if(hasInitialized == false){
            classificationConfig.init(primaryStage);
            hasInitialized = true;
        }
        classificationConfig.hideCluster(true);
        configBtn.setOnMouseClicked(e-> {
            hasInitialClasConfig = true;
            classificationConfig.show("Config Window", "Classification Config");
            classifierConfigInputs = classificationConfig.getClassification();
        });
        showRunButton();
        rb1.selectedProperty().addListener(new ChangeListener<Boolean>(){
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
                if(hasInitialClasConfig == true)
                    runBtn.setVisible(newValue);
            }
        });
    }
    public void showClusAlgorithm(){
        PropertyManager manager = applicationTemplate.manager;
        clearAlgorithmType();
        clusteringPane = new HBox();
        clusteringPane.setSpacing(10);
        RadioButton rb1 = new RadioButton();
        rb1.setText("Random Clustering");
        
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        
        Image configIcon = new Image(iconsPath+"/config.png", 20, 20, true, true);
        ImageView configBtn = new ImageView(configIcon);
        
        clusteringPane.getChildren().addAll(rb1, configBtn);
        leftPanel.getChildren().add(clusteringPane);
        
        ConfigWindow clusterConfig = ConfigWindow.getDialog();
        if(hasInitialized == false){
            clusterConfig.init(primaryStage);
            hasInitialized = true;
        }
        clusterConfig.hideCluster(false);
        configBtn.setOnMouseClicked(e-> {
            hasInitialClusConfig = true;
            clusterConfig.show("Config Window", "Clustering Config");
        });
        showRunButton();
        rb1.selectedProperty().addListener(new ChangeListener<Boolean>(){
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
                if(hasInitialClusConfig == true)
                    runBtn.setVisible(newValue);
            }
        });
    }
    public void clearAlgorithm(){
        leftPanel.getChildren().remove(classificationPane);
        leftPanel.getChildren().remove(clusteringPane);
    }
    public void showRunButton(){
        runBtn = new Button("Run");
        runBtn.setVisible(false);
        leftPanel.getChildren().add(runBtn);
        setRunButtonActions();
    }
    public void clearRunButton(){
        leftPanel.getChildren().remove(runBtn);
    }
    
    private void setRunButtonActions(){
        runBtn.setOnAction(e-> {
            DataSet ds = new DataSet();
            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            ds.setLocations(dataComponent.getDataPoints());
            ds.setLabels(dataComponent.getLabels());
            
            int maxIteration = classifierConfigInputs.get(0);
            int updateInterval = classifierConfigInputs.get(1);
            boolean continuousRun;
            if(classifierConfigInputs.get(2) == 0)
                continuousRun = false;
            else
                continuousRun = true;
            if(continuousRun == true){
                rc = new RandomClassifier(ds, maxIteration, updateInterval, continuousRun, applicationTemplate);
                Thread classifier = new Thread(rc);
                classifier.start();
                runBtn.setDisable(true);
                scrnshotButton.setDisable(true);
                newButton.setDisable(true);
                loadButton.setDisable(true);
                running = true;
            }
            else{
                if(active == false){
                rc = new RandomClassifier(ds, maxIteration, updateInterval, continuousRun, applicationTemplate);
                    Thread classifier = new Thread(rc);
                    active = true;
                    classifier.start();
                }
                synchronized(rc){
                    scrnshotButton.setDisable(true);
                    newButton.setDisable(true);
                    loadButton.setDisable(true);
                    running=true;
                    rc.notify();
                    runBtn.setDisable(true);
                }
            }
        });
    }
    public void activateChartAndGraph(){
        chart.setHorizontalZeroLineVisible(true);
        chart.setVerticalZeroLineVisible(true);
        chart.getXAxis().setOpacity(100);
        chart.getYAxis().setOpacity(100);
        chart.setAnimated(false);
        try {
            chart.getData().clear();
            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            dataComponent.clear();
            dataComponent.loadData(textArea.getText()+hidden.getText());
            dataComponent.displayData();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    activateChartAndGraph();
                    active = false;
                }
            }
            else{
                toggle.setText("Done");
                textArea.setDisable(false);
                clearDetail();
                clearAlgorithmType();
                clearAlgorithm();
                clearRunButton();
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
                        saveButton.setDisable(false);
                    } else {
                        saveButton.setDisable(true);
                    }
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println(newValue);
        }
        });
    }
    private void setClassificationActions(){
        classification.setOnAction(e -> {
            showClassAlgorithm();
        });
    }
    private void setClusteringActions(){
        clustering.setOnAction(e -> {
            showClusAlgorithm();
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
    public Button getRunButton(){
        return runBtn;
    }
    public void setActive(boolean a){
        active =a;
    }
    public Button getLoadButton(){
        return loadButton;
    }
    public Button getNewButton(){
        return newButton;
    }
    public Button getToggleButton(){
        return toggle;
    }
    public void setRunningState(boolean a){
        running = a;
    }
    public boolean getRunningState(){
        return running;
    }
}
