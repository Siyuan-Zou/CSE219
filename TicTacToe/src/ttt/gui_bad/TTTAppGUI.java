package ttt.gui_bad;

import ttt.engine.TTTEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * TicTacToe game with a GUI interface.
 *
 * @author Eugene Stark
 * @version 20180210
 */
public class TTTAppGUI extends Application {

    private static final int BOARD_DIM = 3;
    private static final int BUTTON_SIZE = 50;

    private final TTTEngine engine;

    public TTTAppGUI() {
        engine = new TTTEngine(BOARD_DIM);
    }

    @Override
    public void start(Stage stage) {
        BorderPane mainPane = new BorderPane();

        GridPane grid = new GridPane();
        mainPane.setCenter(grid);
/*
        for (int i = 0; i < BOARD_DIM; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            grid.getRowConstraints().add(rc);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }
*/
        for (int i = 0; i < BOARD_DIM; i++) {
            for (int j = 0; j < BOARD_DIM; j++) {
                Button btn = new Button("");
                btn.setPrefWidth(BUTTON_SIZE);
                btn.setPrefHeight(BUTTON_SIZE);
/*
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setMaxHeight(Double.MAX_VALUE);
 */
                final int ii = i;
                final int jj = j;
                btn.setOnAction(e -> {
                    try {
                        btn.setText(engine.getPlayerToMove()
                                == TTTEngine.X_PLAYER ? "X" : "O");
                        engine.makeMove(ii, jj);
                        if (engine.gameOver()) {
                            int winner = engine.getWinner();
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Game Over");
                            alert.setHeaderText("Game is over");
                            alert.setContentText("The game is over: "
                                    + (winner == TTTEngine.NO_PLAYER ? "Tie game"
                                            : engine.playerName(winner) + " has won."));
                            alert.showAndWait();
                        }
                    } catch (TTTEngine.IllegalMoveException x) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Illegal Move");
                        alert.setHeaderText("Move is illegal");
                        alert.setContentText(x.getMessage());
                        alert.showAndWait();
                    }
                });
                grid.add(btn, j, i);
            }
        }

        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
