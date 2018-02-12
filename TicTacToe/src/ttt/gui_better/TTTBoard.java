package ttt.gui_better;

import ttt.engine.TTTEngine;
import javafx.scene.layout.GridPane;

/**
 * Graphical Tic-Tac-Toe board.
 *
 * @author Eugene Stark
 * @version 20180211
 */
public class TTTBoard extends GridPane {

    private final TTTAppGUI gui;
    private final TTTEngine engine;
    private final int dim;

    public TTTBoard(TTTAppGUI app, TTTEngine engine) {
        this.gui = app;
        this.engine = engine;
        this.dim = engine.getDim();
        setConstraints();
        addButtons();
    }

    private void setConstraints() {
        // ???
    }

    private void addButtons() {
        // ???
    }

}
