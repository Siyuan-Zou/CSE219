/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author Spirors
 */
public class RandomClusterer extends Clusterer {
    
    private static final Random RAND = new Random();
    private DataSet             dataset;
    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private ApplicationTemplate applicationTemplate;
    
    public RandomClusterer(DataSet dataset, int maxIterations, int updateInterval, boolean continuousRun, int numberOfClusters, ApplicationTemplate applicationTemplate){
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(continuousRun);
        this.applicationTemplate = applicationTemplate;
    }
    
    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    @Override
    public void run() {
        for (int iteration = 1; iteration <= maxIterations && tocontinue(); iteration++) {
            assignLabels();
            
            AppData data = ((AppData) applicationTemplate.getDataComponent());
            AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
            if(iteration%updateInterval == 0){ 
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}
                Platform.runLater(() -> data.changeLabels(dataset.getLabels()));
                
                System.out.println("this is " + iteration + " iteration");
            }
            if(iteration == maxIterations){
                ui.getRunButton().setDisable(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.setRunningState(false);
            }
        }
        for (int iteration = 1; iteration <= maxIterations && !tocontinue(); iteration++) {
            assignLabels();
            
            AppData data = ((AppData) applicationTemplate.getDataComponent());
            AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
            if(iteration%updateInterval == 0){ 
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}
                Platform.runLater(() -> {
                    ui.getRunButton().setDisable(false);
                    data.changeLabels(dataset.getLabels());
                });
                System.out.println("this is " + iteration + " iteration");
                synchronized(this){
                    try {
                        ui.getScreenShotButton().setDisable(false);
                        ui.getNewButton().setDisable(false);
                        ui.getLoadButton().setDisable(false);
                        this.wait();
                    } catch (InterruptedException ex) {}
                }
            }
            if(iteration == maxIterations){
                ui.getRunButton().setDisable(false);
                ui.setRClustererActive(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.setRunningState(false);
            }
        }
    }
    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            int randomLabel = RAND.nextInt(numberOfClusters);
            dataset.getLabels().put(instanceName, Integer.toString(randomLabel));
        });
    }
    
}
