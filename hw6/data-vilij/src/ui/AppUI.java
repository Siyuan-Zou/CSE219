package ui;

import actions.AppActions;
import algorithms.Algorithm;
import algorithms.Classifier;
import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import java.io.File;
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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
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
    
    private boolean                      hasInitialClasConfig;
    private boolean                      hasInitialRandomClusConfig;
    private boolean                      hasInitialKMeansClusConfig;
    
    private boolean randomClassifier;
    private boolean randomClusterer;
    private boolean kMeansClusterer;
    
    private HBox randomClusteringPane;
    private HBox kMeansClusteringPane;
    private HBox classificationPane;
    
    private boolean classifierActive = false;
    private boolean rClustererActive = false;
    private boolean kClustererActive = false;
    private boolean running = false;
    
    private ArrayList<Integer> classifierConfigInputs;
    private ArrayList<Integer> randomClustererConfigInputs;
    private ArrayList<Integer> kMeansConfigInputs;
    
    private int countLabels;
    
    private ArrayList<String> algorithmNames = new ArrayList<String>();

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
    
    public void getAlgorithmsFiles(){
        File folder = new File("data-vilij/src/classification");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                algorithmNames.add("classification." + listOfFiles[i].getName().replaceAll(".java", ""));
            }
        }
        File folder2 = new File("data-vilij/src/clustering");
        File[] listOfFiles2 = folder2.listFiles();
        for (int i = 0; i < listOfFiles2.length; i++) {
            if (listOfFiles2[i].isFile()) {
                algorithmNames.add("clustering." + listOfFiles2[i].getName().replaceAll(".java", ""));
            }
        }
    }

    private void layout() {
        getAlgorithmsFiles();
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        NumberAxis      yAxis   = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);
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
        
        setInitialConfig();
    }
    private void setInitialConfig(){
        classifierConfigInputs = new ArrayList<Integer>();
        classifierConfigInputs.add(1);
        classifierConfigInputs.add(1);
        classifierConfigInputs.add(0);
        
        randomClustererConfigInputs = new ArrayList<Integer>();
        randomClustererConfigInputs.add(1);
        randomClustererConfigInputs.add(1);
        randomClustererConfigInputs.add(0);
        randomClustererConfigInputs.add(1);
        
        kMeansConfigInputs = new ArrayList<Integer>();
        kMeansConfigInputs.add(1);
        kMeansConfigInputs.add(1);
        kMeansConfigInputs.add(0);
        kMeansConfigInputs.add(1);
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
        countLabels = 0;
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if(!entry.getValue().equals("null")){
                if(!s.contains("-"+entry.getValue()+"\n")){
                   s+="-"+entry.getValue()+"\n";
                   countLabels++;
                }
            }
        }
        if(countLabels != 2)
            hasTwoLabels = false;
        
        dataDetail = new Text("There are "+lineCount + " instances with " + countLabels + " label(s) loaded from: " +
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
        rb1.setText("RandomClassifier");
        
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        
        Image configIcon = new Image(iconsPath+"/config.png", 20, 20, true, true);
        ImageView configBtn = new ImageView(configIcon);
        
        classificationPane.getChildren().addAll(rb1, configBtn);
        leftPanel.getChildren().add(classificationPane);
        ConfigWindow classificationConfig  = ConfigWindow.getDialog();
        classificationConfig.hideCluster(true);
        
        classificationConfig.init(primaryStage);
        classificationConfig.setClassification(classifierConfigInputs);
        
        showRunButton();
        
        configBtn.setOnMouseClicked(e-> {
            hasInitialClasConfig = true;
            classificationConfig.show("Config Window", "RandomClassifier Config");
            if(rb1.isSelected()){
                runBtn.setVisible(true);
                try {
                    setRunButtonActions();
                } catch (Exception ex) {
                    Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            classifierConfigInputs = classificationConfig.getClassification();
        });
        rb1.selectedProperty().addListener(new ChangeListener<Boolean>(){
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
                if(hasInitialClasConfig == true)
                    runBtn.setVisible(newValue);
                try {
                    setRunButtonActions();
                } catch (Exception ex) {
                    Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                randomClassifier = true;
                randomClusterer = false;
                kMeansClusterer = false;
            }
        });
    }
    public void showClusAlgorithm(){
        PropertyManager manager = applicationTemplate.manager;
        clearAlgorithmType();
        randomClusteringPane = new HBox();
        randomClusteringPane.setSpacing(10);
        kMeansClusteringPane = new HBox();
        kMeansClusteringPane.setSpacing(10);
        
        final ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton();
        rb1.setText("RandomClusterer");
        rb1.setToggleGroup(group);
        RadioButton rb2 = new RadioButton();
        rb2.setText("KMeansClusterer");
        rb2.setToggleGroup(group);
        
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        
        Image configIcon = new Image(iconsPath+"/config.png", 20, 20, true, true);
        ImageView randomConfigBtn = new ImageView(configIcon);
        ImageView kMeansConfigBtn = new ImageView(configIcon);
        
        randomClusteringPane.getChildren().addAll(rb1, randomConfigBtn);
        kMeansClusteringPane.getChildren().addAll(rb2, kMeansConfigBtn);
        leftPanel.getChildren().addAll(randomClusteringPane, kMeansClusteringPane);
        
        ConfigWindow randomClusterConfig = ConfigWindow.getDialog();
        ConfigWindow kMeansClusterConfig = ConfigWindow.getDialog();
        
        randomClusterConfig.hideCluster(false);
        kMeansClusterConfig.hideCluster(false);
        
        randomClusterConfig.init(primaryStage);
        randomClusterConfig.setClustering(randomClustererConfigInputs);
        kMeansClusterConfig.init(primaryStage);
        kMeansClusterConfig.setClustering(kMeansConfigInputs);
        
        showRunButton();
        
        randomConfigBtn.setOnMouseClicked(e-> {
            hasInitialRandomClusConfig = true;
            randomClusterConfig.show("Config Window", "RandomClusterer Config");
            if(rb1.isSelected()){
                runBtn.setVisible(true);
                try {
                    setRunButtonActions();
                } catch (Exception ex) {
                    Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            randomClustererConfigInputs = randomClusterConfig.getClustering();
        });
        kMeansConfigBtn.setOnMouseClicked(e-> {
            hasInitialKMeansClusConfig = true;
            kMeansClusterConfig.show("Config Window", "KmeansClusterer Config");
            if(rb2.isSelected()){
                runBtn.setVisible(true);
                try {
                    setRunButtonActions();
                } catch (Exception ex) {
                    Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            kMeansConfigInputs = kMeansClusterConfig.getClustering();
        });
        
        rb1.selectedProperty().addListener(new ChangeListener<Boolean>(){
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
                if(hasInitialRandomClusConfig == true)
                    runBtn.setVisible(newValue);
                try {
                    setRunButtonActions();
                } catch (Exception ex) {
                    Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                randomClassifier = false;
                randomClusterer = true;
                kMeansClusterer = false;
            }
        });
        rb2.selectedProperty().addListener(new ChangeListener<Boolean>(){
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
                if(hasInitialKMeansClusConfig == true)
                    runBtn.setVisible(newValue);
                try {
                    setRunButtonActions();
                } catch (Exception ex) {
                    Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                randomClassifier = false;
                randomClusterer = false;
                kMeansClusterer = true;
            }
        });
    }
    public void clearAlgorithm(){
        leftPanel.getChildren().remove(classificationPane);
        leftPanel.getChildren().removeAll(randomClusteringPane, kMeansClusteringPane);
    }
    public void showRunButton(){
        runBtn = new Button("Run");
        runBtn.setVisible(false);
        leftPanel.getChildren().add(runBtn);
    }
    public void clearRunButton(){
        leftPanel.getChildren().remove(runBtn);
    }
    
    private void setRunButtonActions() throws Exception{
        DataSet ds = new DataSet();
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
        ds.setLocations(dataComponent.getDataPoints());
        ds.setLabels(dataComponent.getLabels());
        
        int maxIterationClassifier = classifierConfigInputs.get(0);
        int updateIntervalClassifier = classifierConfigInputs.get(1);
        boolean continuousRunClassifier;
        if(classifierConfigInputs.get(2) == 0)
            continuousRunClassifier = false;
        else
            continuousRunClassifier = true;

        Class randClassifier = Class.forName(algorithmNames.get(0));
        Constructor randclassifierCons = randClassifier.getConstructor(DataSet.class, int.class, int.class, boolean.class, ApplicationTemplate.class);
        Classifier rc = (Classifier) randclassifierCons.newInstance(ds, 
                                                                  maxIterationClassifier, 
                                                                  updateIntervalClassifier, 
                                                                  continuousRunClassifier, 
                                                                  applicationTemplate);
        
        int maxIterationRClusterer = randomClustererConfigInputs.get(0);
        int updateIntervalRClusterer = randomClustererConfigInputs.get(1);
        boolean continuousRunRClusterer;
        if(randomClustererConfigInputs.get(2) == 0)
            continuousRunRClusterer = false;
        else
            continuousRunRClusterer = true;
        int rClusters = randomClustererConfigInputs.get(3);
        if(rClusters > countLabels){
            rClusters = countLabels;
            randomClustererConfigInputs.set(3, countLabels);
            ErrorDialog edialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            edialog.show("Warning Dialog", 
                                "The amount of cluster enter is greater than the amount of labels in data, "
                                        + "the application has set the cluster to the amount of labels in the data.");
        }
        Class randClusterer = Class.forName(algorithmNames.get(2));
        Constructor randClustererCons = randClusterer.getConstructor(DataSet.class, int.class, int.class, boolean.class, int.class, ApplicationTemplate.class);
        Clusterer rCluster = (Clusterer) randClustererCons.newInstance(ds, 
                                                                       maxIterationRClusterer, 
                                                                       updateIntervalRClusterer, 
                                                                       continuousRunRClusterer, 
                                                                       rClusters, 
                                                                       applicationTemplate);
        
        int maxIteration = kMeansConfigInputs.get(0);
        int updateInterval = kMeansConfigInputs.get(1);
        boolean continuousRun;
        if(kMeansConfigInputs.get(2) == 0)
            continuousRun = false;
        else
            continuousRun = true;
        int clusters = kMeansConfigInputs.get(3);
        if(clusters > countLabels){
            clusters = countLabels;
            kMeansConfigInputs.set(3, countLabels);
            ErrorDialog edialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            edialog.show("Warning Dialog", 
                                "The amount of cluster enter is greater than the amount of labels in data, "
                                        + "the application has set the cluster to the amount of labels in the data.");
        }
        Class kClusterer = Class.forName(algorithmNames.get(1));
        Constructor kClustererCons = kClusterer.getConstructor(DataSet.class, int.class, int.class, boolean.class, int.class, ApplicationTemplate.class);
        Clusterer kCluster = (Clusterer) kClustererCons.newInstance(ds, 
                                                                    maxIteration, 
                                                                    updateInterval, 
                                                                    continuousRun, 
                                                                    clusters, 
                                                                    applicationTemplate);

        runBtn.setOnAction(e-> {
            if(randomClassifier == true){
                chart.setAnimated(false);
                if(continuousRunClassifier == true){
                    Thread classifier = new Thread(rc);
                    classifier.start();
                    runBtn.setDisable(true);
                    scrnshotButton.setDisable(true);
                    newButton.setDisable(true);
                    loadButton.setDisable(true);
                    running = true;
                }
                else{
                    if(classifierActive == false){
                        Thread classifier = new Thread(rc);
                        classifierActive = true;
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
            }else if(randomClusterer == true){
                chart.setAnimated(true);
                if(continuousRunRClusterer == true){
                    Thread randomClusterer = new Thread(rCluster);
                    randomClusterer.start();
                    runBtn.setDisable(true);
                    scrnshotButton.setDisable(true);
                    newButton.setDisable(true);
                    loadButton.setDisable(true);
                    running = true;
                }else{
                    if(rClustererActive == false){
                        Thread randomClusterer = new Thread(rCluster);
                        rClustererActive = true;
                        randomClusterer.start();
                    }
                    synchronized(rCluster){
                        scrnshotButton.setDisable(true);
                        newButton.setDisable(true);
                        loadButton.setDisable(true);
                        running=true;
                        rCluster.notify();
                        runBtn.setDisable(true);
                    }
                }
            }else if(kMeansClusterer == true){
                chart.setAnimated(true);
                if(continuousRun == true){
                    Thread kMeans = new Thread(kCluster);
                    kMeans.start();
                    runBtn.setDisable(true);
                    scrnshotButton.setDisable(true);
                    newButton.setDisable(true);
                    loadButton.setDisable(true);
                    running = true;
                }else{
                    if(kClustererActive == false){
                        Thread kMeans = new Thread(kCluster);
                        kClustererActive = true;
                        kMeans.start();
                    }
                    synchronized(kCluster){
                        scrnshotButton.setDisable(true);
                        newButton.setDisable(true);
                        loadButton.setDisable(true);
                        running=true;
                        kCluster.notify();
                        runBtn.setDisable(true);
                    }
                }
            }
        });
    }
    public void activateChartAndGraph(){
        chart.setHorizontalZeroLineVisible(true);
        chart.setVerticalZeroLineVisible(true);
        chart.getXAxis().setOpacity(100);
        chart.getYAxis().setOpacity(100);
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
                    classifierActive = false;
                    rClustererActive = false;
                    kClustererActive = false;
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
    public void setActive(){
        classifierActive = false;
        rClustererActive = false;
        kClustererActive = false;
    }
    public void setClassifierActive(boolean a){
        classifierActive = a;
    }
    public void setRClustererActive(boolean a){
        rClustererActive = a;
    }
    public void setKClustererActive(boolean a){
        kClustererActive = a;
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
