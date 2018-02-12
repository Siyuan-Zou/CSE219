package ttt.ci;


import ttt.engine.TTTEngine;
import java.util.Scanner;

/**
 * TicTacToe game with a command interface.
 *
 * @author Eugene Stark
 * @version 20180211
 */
public class TTTAppCI {

    private static final int BOARD_DIM = 3;

    private final TTTEngine engine;
    private final Scanner scanner;

    public TTTAppCI() {
        engine = new TTTEngine(BOARD_DIM);
        scanner = new Scanner(System.in);
    }

    private void mainLoop() {
        while (!engine.gameOver()) {
            System.out.println(engine.toString());
            System.out.print(engine.playerName(engine.getPlayerToMove())
                    + " to move (enter row, col): ");
            String input = scanner.nextLine().trim();
            if (input == null) {
                return;
            }
            if ("".equals(input)) {
                continue;
            }
            String[] fields = input.split("\\s*,\\s*");
            if (fields.length != 2) {
                System.out.println("Unrecognized move");
                continue;
            }
            try {
                int row = Integer.parseInt(fields[0]);
                int col = Integer.parseInt(fields[1]);
                if (row < 1 || row > BOARD_DIM || col < 0 || col > BOARD_DIM) {
                    throw new NumberFormatException();
                }
                engine.makeMove(row - 1, col - 1);
                if (engine.gameOver()) {
                    int winner = engine.getWinner();
                    System.out.println(engine.toString());
                    System.out.println("The game is over: "
                            + (winner == TTTEngine.NO_PLAYER ? "Tie game"
                                    : engine.playerName(winner) + " has won."));
                    break;
                }
            } catch (NumberFormatException x) {
                System.out.println("Row and column must be numbers 1.." + BOARD_DIM);
            } catch (TTTEngine.IllegalMoveException x) {
                System.out.println("That square is taken, choose another.");
            }
        }
    }

    public static void main(String[] args) {
        new TTTAppCI().mainLoop();
    }

}
