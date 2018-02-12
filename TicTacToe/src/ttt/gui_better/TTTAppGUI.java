package ttt.gui_better;

import ttt.engine.TTTEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TicTacToe game with a GUI interface.
 *
 * @author Eugene Stark
 * @version 20180211
 */
public class TTTAppGUI extends Application {

    private static final int BOARD_DIM = 3;

    private final TTTEngine engine;

    public TTTAppGUI() {
        engine = new TTTEngine(BOARD_DIM);
    }

    @Override
    public void start(Stage stage) {
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(new TTTBoard(this, engine));
        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.show();
    }

    public void alertIllegalMove() {
        // ???
    }

    public void alertGameOver() {
        // ???
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
