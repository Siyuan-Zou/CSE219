package classification;

import algorithms.Classifier;
import data.DataSet;
import dataprocessors.AppData;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    private ApplicationTemplate applicationTemplate;
    private boolean proceed = false;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue,
                            ApplicationTemplate applicationTemplate) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void run() {
        for (int i = 1; i <= maxIterations && tocontinue(); i++) {
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            
            AppData data = ((AppData) applicationTemplate.getDataComponent());
            AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
            if(i%updateInterval == 0){ 
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}
                Platform.runLater(() -> data.displayLine(xCoefficient, yCoefficient, constant));
            }
            if(i == maxIterations){
                ui.getRunButton().setDisable(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.getToggleButton().setDisable(false);
            }
            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i); //
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                System.out.println("the 5 percent hit");
                ui.getRunButton().setDisable(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.getToggleButton().setDisable(false);
                break;
            }
        }
        for (int i = 1; i <= maxIterations && tocontinue() == false; i++) {
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);
            
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            
            AppData data = ((AppData) applicationTemplate.getDataComponent());
            AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
            if(i%updateInterval == 0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}
                Platform.runLater(() -> {
                    ui.getRunButton().setDisable(false);
                    data.displayLine(xCoefficient, yCoefficient, constant);
                });
                System.out.printf("Iteration number %d: ", i); //
                flush();
                
                synchronized(this){
                    try {
                        ui.getScreenShotButton().setDisable(false);
                        ui.getNewButton().setDisable(false);
                        ui.getLoadButton().setDisable(false);
                        ui.getToggleButton().setDisable(false);
                        this.wait();
                    } catch (InterruptedException ex) {}
                    
                }
            }
            if(i == maxIterations){
                ui.getRunButton().setDisable(false);
                ui.setActive(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.getToggleButton().setDisable(false);
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                System.out.println("the 5 percent hit");
                ui.getRunButton().setDisable(false);
                ui.setActive(false);
                ui.getScreenShotButton().setDisable(false);
                ui.getNewButton().setDisable(false);
                ui.getLoadButton().setDisable(false);
                ui.getToggleButton().setDisable(false);
                break;
            }
        }
    }
    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        //DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
        //RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        //classifier.run(); // no multithreading yet
    }
}