
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TaskDemo extends Application {

    private ListView<Node> taskView;
    private int taskID;

    // "Bank balance" (credits - debits) should always be 0.
    private int credits, debits;

    // Lock to synchronize access to bank balance.
    ReentrantLock lock = new ReentrantLock();

    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Launch");
        btn.setOnAction(e -> {
            Task t = new Task() {
                @Override
                protected Object call() throws Exception {
                    doTask(this);
                    return null;
                }
            };
            new Thread(t, "Task " + (++taskID)).start();
        });
        taskView = new ListView<>();
        taskView.setPrefWidth(300);
        taskView.setPrefHeight(500);
        VBox root = new VBox();
        root.getChildren().add(btn);
        root.getChildren().add(taskView);
        Scene scene = new Scene(root);
        primaryStage.setTitle("Task Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void doTask(Task task) {
        String myName = Thread.currentThread().getName();
        Label myLabel = new Label(myName + " active");
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> task.cancel());
        FlowPane myPane = new FlowPane();
        ProgressIndicator progress = new ProgressIndicator(0);
        ObservableList<Node> children = myPane.getChildren();
        children.add(cancelButton);
        children.add(progress);
        children.add(myLabel);
        Platform.runLater(() -> {
            taskView.getItems().add(myPane);
        });
        try {
            long time = (long) (Math.random() * 20000);
            for (long t = 0; t < time; t += 1) {
                try {
                    // Perform a "banking transaction".
                    if (Math.random() >= 0.5) {
                        credits++;
                        debits++;
                    } else {
                        credits--;
                        debits--;
                    }
                    int balance = credits - debits;
                    if (balance != 0) {
                        System.out.println("Nonzero balance " + balance + "!");
                    }
                } finally {
                    if(lock.isHeldByCurrentThread())
                        lock.unlock();
                }
                long tt = t;
                double pct = (double) tt / time;
                Thread.sleep(1);
            }
        } catch (InterruptedException x) {
            // EMPTY EXCEPTION HANDLERS ARE OFTEN A BAD IDEA!
        }
        taskView.getItems().remove(myPane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
