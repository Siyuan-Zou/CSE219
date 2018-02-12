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
        // ???
    }

    private void setListener() {
        // ???
    }

}
