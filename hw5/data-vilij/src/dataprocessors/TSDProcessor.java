package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }
    public static class DupelicateException extends Exception {
        
        private static final String NAME_ERROR_MSG = "There is dupe at";

        public DupelicateException(int a) {
            super(String.format(NAME_ERROR_MSG, a));
        }
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private ArrayList<Integer> errorLines = new ArrayList<Integer>();
    private int dupeLine = -1;
    private String dupeName;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        AtomicInteger dupeLineNum = new AtomicInteger(0);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                    String   name  = checkedname(list.get(0));
                    int n = dupeLineNum.incrementAndGet();
                    if(checkDupe(name)){
                        dupeName = name;
                        dupeLine = n;
                        throw new DupelicateException(n);
                    }
                    String   label = list.get(1);
                    String[] pair  = list.get(2).split(",");
                    Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                    dataLabels.put(name, label);
                    dataPoints.put(name, point);
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                      errorLines.add(dataLabels.size()+1);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
        dupeLine = -1;
        dupeName ="";
        errorLines.clear(); 
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
    public Map<String, Point2D> getDataPoints(){
        return dataPoints;
    }
    public Map<String, String> getDataLabels(){
        return dataLabels;
    }
    public void addTooltip(XYChart<Number, Number>chart){
        for(XYChart.Series<Number, Number> s : chart.getData()){
            for(XYChart.Data d : s.getData()){
                Tooltip.install(d.getNode(), new Tooltip(s.getName()));
                
                d.getNode().setOnMouseEntered(e -> {
                    d.getNode().setCursor(Cursor.CROSSHAIR);
                });
                
                d.getNode().setOnMouseExited(e -> {
                    d.getNode().setCursor(Cursor.DEFAULT);  
                });
            }
        }
    }
    public boolean checkDupe(String name){
        for (Map.Entry<String, String> entry : dataLabels.entrySet()) {
            if (name.equals(entry.getKey())){
                return true;
            }
        }
        return false;
    }
    public ArrayList<Integer> getErrorLines(){
        return errorLines;
    }
    public int getDupeLine(){
        return dupeLine;
    }
    public String getDupeName(){
        return dupeName;
    }
}
