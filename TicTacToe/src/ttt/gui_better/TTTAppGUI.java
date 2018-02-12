package ttt.gui_better;

import ttt.engine.TTTEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
        Exception e = engine.new IllegalMoveException();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Illegal Move");
        alert.setHeaderText("Move is illegal");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public void alertGameOver() {
            int winner = engine.getWinner();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText("Game is over");
            alert.setContentText("The game is over: "
                + (winner == TTTEngine.NO_PLAYER ? "Tie game"
                : engine.playerName(winner) + " has won."));
                    alert.showAndWait();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
