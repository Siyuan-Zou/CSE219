package ttt.engine;


/**
 * Class whose objects represent the state of a Tic-Tac-Toe game.
 *
 * @author Eugene Stark
 * @version 20180210
 */
public class TTTEngine {

    public static final int NO_PLAYER = 0;
    public static final int X_PLAYER = 1;
    public static final int O_PLAYER = 2;

    private final int dim;
    private final int[][] board;
    private int playerToMove;

    private int moveCount;

    /**
     * Initialize a Tic-Tac-Toe game of a given board dimension.
     * 
     * @param dim The board dimension.
     */
    public TTTEngine(int dim) {
        this.dim = dim;
        playerToMove = X_PLAYER;
        board = new int[dim][dim];
    }

    /**
     * Get the board dimension of this Tic-Tac-Toe game.
     * 
     * @return the board dimension.
     */
    public int getDim() {
        return dim;
    }

    /**
     * Get the player who is currently on the move.
     * 
     * @return an integer that is either X_PLAYER or O_PLAYER,
     * depending on which player is on the move.
     */
    public int getPlayerToMove() {
        return playerToMove;
    }
    
    /**
     * Make a move by placing the current player's token on a specified
     * square of the board.
     * 
     * @param row  Row number (0-based) of the selected square.
     * @param col Column number (0-based) of the selected column.
     * @throws ttt.engine.TTTEngine.IllegalMoveException in case the selected
     * move is not legal.
     */
    public void makeMove(int row, int col)
            throws IllegalMoveException {
        if (gameOver() || board[row][col] != NO_PLAYER) {
            throw new IllegalMoveException();
        }
        board[row][col] = playerToMove;
        playerToMove = playerToMove == X_PLAYER ? O_PLAYER : X_PLAYER;
        moveCount++;
    }

    /**
     * Query whether or not the game is over.
     * 
     * @return true if the game is over, false otherwise.
     */
    public boolean gameOver() {
        return moveCount >= dim * dim || getWinner() != NO_PLAYER;
    }

    /**
     * Get the printable name of a player.
     * 
     * @param player  Either X_PLAYER or O_PLAYER.
     * @return  A string corresponding to the specified player.
     */
    public String playerName(int player) {
        if (player == X_PLAYER) {
            return "X";
        } else if (player == O_PLAYER) {
            return "O";
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (board[i][j] == NO_PLAYER) {
                    result += " ";
                } else {
                    result = result + (board[i][j] == X_PLAYER ? "X" : "O");
                }
                if (j < dim - 1) {
                    result = result + "|";
                }
            }
            result = result + "\n";
            if (i < dim - 1) {
                result = result + "-----\n";
            }
        }
        return result;
    }

    /**
     * Figure out who has won the game.
     *
     * @return the mark of the player who has won, or BLANK if no one has won.
     */
    public char getWinner() {
        char who;

        // First check for three in a row.
        for (int row = 0; row < dim; row++) {
            who = checkDirection(row, 0, 0, 1);
            if (who != NO_PLAYER) {
                return (who);
            }
        }

        // Next check for three in a column.
        for (int col = 0; col < dim; col++) {
            who = checkDirection(0, col, 1, 0);
            if (who != NO_PLAYER) {
                return (who);
            }
        }

        // Finally, check the diagonals.
        who = checkDirection(0, 0, 1, 1);
        if (who != NO_PLAYER) {
            return (who);
        }
        who = checkDirection(dim - 1, 0, -1, 1);
        if (who != NO_PLAYER) {
            return (who);
        }

        return NO_PLAYER;
    }

    /**
     * Internal method used to check for a winner in a single direction.
     *
     * @param startRow The starting row position.
     * @param startCol The starting column position.
     * @param rowInc The increment to apply to the row position.
     * @param colInc The increment to apply to the column position.
     * @return If X has won, return X, and if O has won, return O, otherwise
     * return BLANK.
     */
    private char checkDirection(int startRow, int startCol, int rowInc, int colInc) {
        int sum = calcSum(startRow, startCol, rowInc, colInc);
        if (sum / (dim + 1) >= dim) {
            return X_PLAYER;
        } else if (sum % (dim + 1) >= dim) {
            return O_PLAYER;
        }
        return NO_PLAYER;
    }

    /**
     * Internal method used to check for three-in a row, column, or diagonal.
     * From a specified starting row and column, traverse in a specified
     * direction, accumulating a sum. Each time X is encountered, BOARD_SIZE+1
     * is added. Each time O is encountered, 1 is added. The resulting sum has
     * the property that sum/(BOARD_SIZE+1) is the number of X's encountered,
     * and sum%(BOARD_SIZE+1) is the number of O's encountered.
     *
     * @param startRow The starting row position.
     * @param startCol The starting column position.
     * @param rowInc The increment to apply to the row position.
     * @param colInc The increment to apply to the column position.
     * @return An integer "sum" having the property that sum/(BOARD_SIZE+1) is
     * the number of X's seen and sum%(BOARD_SIZE+1) is the number of O's seen.
     */
    private int calcSum(int startRow, int startCol, int rowInc, int colInc) {
        int sum = 0;
        for (int row = startRow, col = startCol;
                row >= 0 && row < dim && col >= 0 && col < dim;
                row += rowInc, col += colInc) {
            int who = board[row][col];
            if (who == X_PLAYER) {
                sum += dim + 1;
            } else if (who == O_PLAYER) {
                sum += 1;
            }
        }
        return (sum);
    }

    public class IllegalMoveException extends Exception {
    }
}
