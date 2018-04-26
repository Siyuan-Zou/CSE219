package dataprocessors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private XYChart.Series classifierLine = new XYChart.Series();
    private XYChart.Data p1 = new XYChart.Data();
    private XYChart.Data p2 = new XYChart.Data();
    

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        PropertyManager manager = applicationTemplate.manager;
        File file = new File(dataFilePath.toString());
        String out1 ="";
        String out2 ="";
        int count=0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st ="";
            while ((st = br.readLine()) != null){
                if(count < 10)
                    out1+=st+"\n";
                else
                    out2+=st+"\n";
                count++;
            }
            processor.clear();
            processor.processString(out1+out2);
            
            AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
            
            ui.showData();
            ui.showDetail(count, dataFilePath.toString(), processor.getDataLabels());
            ui.showAlgorithmType();
            
            ui.getTextArea().setText(out1);
            ui.getHidden().setText(out2);
            
        }catch (Exception e){
            if(processor.getDupeLine() != -1){
                ErrorDialog     ddialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                ddialog.show(manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                        manager.getPropertyValue(AppPropertyTypes.DUPLICATE_AT.name())+ processor.getErrorLines().get(0)+processor.getDupeName());
            }else{
                if(!processor.getErrorLines().isEmpty()){
                    ErrorDialog     edialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    edialog.show(manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                            manager.getPropertyValue(AppPropertyTypes.ERROR_AT.name())+ processor.getErrorLines().get(0));
                }
            }
        }
    }

    public void loadData(String dataString) {
        PropertyManager manager = applicationTemplate.manager;
        try {
            processor.processString(dataString);
        } catch (Exception e) {
            if(processor.getDupeLine() != -1){
                ErrorDialog     ddialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                ddialog.show(manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                        manager.getPropertyValue(AppPropertyTypes.DUPLICATE_AT.name())+ processor.getErrorLines().get(0)+processor.getDupeName());
                ((AppUI) applicationTemplate.getUIComponent()).setInvalidProperty(true);
            }else{
                if(!processor.getErrorLines().isEmpty()){
                    ErrorDialog     edialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    edialog.show(manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                            manager.getPropertyValue(AppPropertyTypes.ERROR_AT.name())+ processor.getErrorLines().get(0));
                    ((AppUI) applicationTemplate.getUIComponent()).setInvalidProperty(true);
                }
            }
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        String currentText = ((AppUI) applicationTemplate.getUIComponent()).getCurrentText();
        try(PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))){
            writer.write(currentText);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
        processor.addTooltip(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
    public void displayLine(int a, int b, int c){
        PropertyManager manager = applicationTemplate.manager;
        AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
        classifierLine.getData().clear();
        ui.getChart().getData().remove(classifierLine);
        
        if(!((AppUI) applicationTemplate.getUIComponent()).getChart().getData().isEmpty()){
            Map<String, Point2D> points = processor.getDataPoints();
            double lowestX = Double.MAX_VALUE;
            double highestX = Double. NEGATIVE_INFINITY;
            double lowestY = Double.MAX_VALUE;
            double highestY = Double. NEGATIVE_INFINITY;
            for (Map.Entry<String, Point2D> entry : points.entrySet()) {
                if(entry.getValue().getX() > highestX)
                    highestX = entry.getValue().getX();
                if(entry.getValue().getX() < lowestX)
                    lowestX = entry.getValue().getX();
                if(entry.getValue().getY() > highestY)
                    highestY = entry.getValue().getY();
                if(entry.getValue().getY() < lowestY)
                    lowestY = entry.getValue().getY();
            }
            classifierLine.setName(manager.getPropertyValue(AppPropertyTypes.CLASSIFIERLINE.name()));
            
            if(a==0){
                p1.setXValue(lowestX);
                p1.setYValue(-c/b);
                p2.setXValue(highestX);
                p2.setYValue(-c/b);
            }else{
                p1.setXValue(lowestX);
                p1.setYValue((-1*(a*lowestX + c))/b);
                p2.setXValue(highestX);
                p2.setYValue((-1*(a*highestX + c))/b);
            }
            classifierLine.getData().add(p1);
            classifierLine.getData().add(p2);
            ui.getChart().getData().add(classifierLine);
        
            p1.getNode().setId(manager.getPropertyValue(AppPropertyTypes.POINT.name()));
            p2.getNode().setId(manager.getPropertyValue(AppPropertyTypes.POINT.name()));
            classifierLine.getNode().setId(manager.getPropertyValue(AppPropertyTypes.CLASSIFIERLINE.name()));
        }
    }
    public Map<String, String> getLabels(){
        return processor.getDataLabels();
    }
    public Map<String, Point2D> getDataPoints(){
        return processor.getDataPoints();
    }
}
