package ui;

import actions.AppActions;
import static java.io.File.separator;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import dataprocessors.AppData;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import static settings.AppPropertyTypes.*;

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
    private String                       scrnshoticonPath; // Added path for scrnshotButton 
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display

    public ScatterChart<Number, Number> getChart() { return chart; }
    public TextArea getTextArea() { return textArea; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshoticonPath = String.join(separator, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        scrnshotButton = setToolbarButton(scrnshoticonPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        textArea = new TextArea();
        displayButton = new Button(manager.getPropertyValue(DISPLAY_BUTTON.name()));
        Text dataTitle = new Text(manager.getPropertyValue(DATA_TITLE.name()));
        HBox visPane = new HBox();
        VBox dataPane = new VBox();
        dataPane.getChildren().addAll(dataTitle, textArea, displayButton);
        dataTitle.setFont(new Font(24));
        dataPane.setMaxWidth(350.0);
        dataPane.setMaxHeight(300.0);
        dataPane.setPadding(new Insets(10, 10, 10, 10));
        
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new ScatterChart<>(xAxis,yAxis);
        chart.setPrefHeight(600);
        chart.setPrefWidth(700);
        chart.setTitle(manager.getPropertyValue(CHART_TITLE.name()));
        visPane.getChildren().addAll(dataPane, chart);
        appPane.getChildren().add(visPane);
    }

    private void setWorkspaceActions() {
        displayButton.setOnAction(e -> {
            try {
                ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
            } catch (Exception ex) {
                Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        textArea.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            if(t1.equals("")) {
                newButton.setDisable(true);
                saveButton.setDisable(true);
            }
            else {
                newButton.setDisable(false);
                saveButton.setDisable(false);
            }
            hasNewText = true;
            ((AppData) applicationTemplate.getDataComponent()).clear();
        });
    }
}

