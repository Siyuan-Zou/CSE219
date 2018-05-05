/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet       dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private final AtomicBoolean continuousRun;
    private ApplicationTemplate applicationTemplate;


    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, boolean continuousRun, int numberOfClusters, ApplicationTemplate applicationTemplate) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(false);
        this.continuousRun = new AtomicBoolean(continuousRun); 
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
        initializeCentroids();
        for (int iteration = 1; iteration <= maxIterations && tocontinue() && continuousRun.get(); iteration++) {
            assignLabels();
            recomputeCentroids();
            
            AppData data = ((AppData) applicationTemplate.getDataComponent());
            AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
            if(iteration%updateInterval == 0){ 
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}
                Platform.runLater(() -> data.changeLabels(dataset.getLabels()));
            }
            if(iteration == maxIterations){
                ui.getRunButton().setDisable(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.setRunningState(false);
            }
            if(!tocontinue()){
                ui.getRunButton().setDisable(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.setRunningState(false);
            }
        }
        for (int iteration = 1; iteration <= maxIterations && tocontinue() && !(continuousRun.get()); iteration++) {
            assignLabels();
            recomputeCentroids();
            
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
                System.out.println("this is" + iteration + "iteration");
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
                ui.setKClustererActive(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.setRunningState(false);
            }
            if(!tocontinue()){
                ui.getRunButton().setDisable(false);
                ui.setKClustererActive(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.setRunningState(false);
            }
        }
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                i = (++i % instanceNames.size());
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                                 .entrySet()
                                 .stream()
                                 .filter(entry -> i == Integer.parseInt(entry.getValue()))
                                 .map(entry -> dataset.getLocations().get(entry.getKey()))
                                 .reduce(new Point2D(0, 0), (p, q) -> {
                                     clusterSize.incrementAndGet();
                                     return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                                 });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }
    
}