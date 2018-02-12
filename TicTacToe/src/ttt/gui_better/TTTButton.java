package ttt.gui_better;

import ttt.engine.TTTEngine;
import javafx.scene.control.Button;

/**
 * Button for graphical Tic-Tac-Toe board.
 *
 * @author Eugene Stark
 * @version 20180211
 */
public class TTTButton extends Button {

    private static final int PREF_SIZE = 50;

    private final int row, col;
    private final TTTAppGUI gui;
    private final TTTEngine engine;

    public TTTButton(TTTAppGUI app, TTTEngine engine, int row, int col) {
        this.gui = app;
        this.engine = engine;
        this.row = row;
        this.col = col;
        this.setPrefWidth(PREF_SIZE);
        this.setPrefHeight(PREF_SIZE);

        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setListener();
    }

    private void setListener() {
        this.setOnAction(e -> {
            try {
                this.setText(engine.getPlayerToMove()
                        == TTTEngine.X_PLAYER ? "X" : "O");
                engine.makeMove(this.row, this.col);
                if (engine.gameOver()) {
                    this.gui.alertGameOver();
                }
            }catch(TTTEngine.IllegalMoveException x) {
                this.gui.alertIllegalMove();
            }
        });
    }
}
