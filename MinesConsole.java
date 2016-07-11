import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Class that processes console input.
 */
class MinesConsole {
    // Constants.
    private static final String GRID_SIZE_CMD = "--gridSize";
    private static final String NUM_MINES_CMD = "--mines";
    private static final String NO_CONSOLE_CMD = "--noconsole";

    private static final String EXPOSE_CELL_CMD = "e";
    private static final String QUIT_CELL_CMD = "quit";
    private static final String EXPOSE_ALL_CELL_CMD = "expose_all";
    private static final String SOLVE_CMD = "solve";

    private static class GameParams {
        final int gridSize;
        final int numberOfMines;
        final boolean useConsole; // Whether to use a console or not. 
        GameParams(int gridSize, int numberOfMines, boolean useConsole) {
            this.gridSize = gridSize;
            this.numberOfMines = numberOfMines;
            this.useConsole = useConsole;
        }
    }

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        GameParams params = setupGame(args);
        if (!params.useConsole) {
            // Batch mode processing.
            batchModeSolver(br, params.gridSize, params.numberOfMines);
            return;
        } 
        Minesweeper board = new Minesweeper(params.gridSize, params.numberOfMines);
        System.out.println("Created board with gridsize = " + board.getGridSize() +
                " and number of mines = " + board.getNumberOfMines());
        board.printGrid();
        System.out.println();
        System.out.println();
        processConsoleInput(br, board);
    }

    private static void processConsoleInput(BufferedReader br, Minesweeper board) {
        boolean done = false;
        while (!done) {
            System.out.println();
            System.out.println("e X Y  -- exposes cell at row X column Y");
            System.out.println("quit   -- quits the console, ending the game.");
            System.out.println("solve -- run the algorithmic solver on the current game.");
            System.out.print("\n \n>> ");
            try {
                String command = br.readLine();
                String[] commandArgs = command.split(" ");
                if (commandArgs[0].equals(EXPOSE_CELL_CMD)) {
                    if (exposeCell(commandArgs, board)) {
                        System.out.println("Game ended.");
                        if (board.hasUserWon()) {
                            System.out.println("You WON !");
                        } else {
                            System.out.println("You LOST !");
                        }
                        done = true;
                    }
                    board.printGrid();
                } else if (commandArgs[0].equals(QUIT_CELL_CMD)) {
                    done = true;
                    System.out.println("Quitting..");
                } else if (commandArgs[0].equals(EXPOSE_ALL_CELL_CMD)) {
                    board.exposeAllCells();
                    System.out.println("Exposing all cells. Game Over.");
                    board.printGrid();
                } else if (commandArgs[0].equals(SOLVE_CMD)) {
                    System.out.println("Running solver.");
                    Solver solver = new Solver(board);
                    solver.solve(br, true);
                    if (board.hasUserWon()) {
                        System.out.println("Solver won!");
                    } else {
                        System.out.println("Solver lost!");
                    }
                    done = true;
                }
            } catch (IOException e) {
                done = true;
                System.out.println("Error processing input. Aborting.");
            }
        }
    }

    private static boolean exposeCell(String[] args, Minesweeper board) {
        if (args.length < 3) {
            System.out.println("Invalid arguments for clear cell.");
            return false;
        }
        try {
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            return board.exposeCell(x, y); 
        } catch (NumberFormatException e) {
            System.out.println("Could not parse: " + args[1] + " and " + args[2]);
            return false;
        }
    }

    // Runs the solver in a batch mode to compute aggregate stats.
    private static void batchModeSolver(BufferedReader br, int gridSize, int numberOfMines) {
        long totalTime = 0;
        System.out.println("Grid size = " + gridSize + " number of mines: " + numberOfMines);
        Minesweeper board = new Minesweeper(gridSize, numberOfMines);
        Solver solver = new Solver(board);
        long prevTime = System.currentTimeMillis();
        solver.solve(br, false);
        totalTime = System.currentTimeMillis() - prevTime;
        String result;
        if (board.hasUserWon()) {
            result = "WON";
        } else {
            result = "LOST";
        }
        System.out.println("Result=" + result + ", time=" + totalTime);
    }

    private static GameParams setupGame(String[] args) {
        int gridSize = 10;
        int numberOfMines = 10;
        boolean useConsole = true;
        if (args == null || args.length == 0 || args.length < 2) {
            return new GameParams(gridSize, numberOfMines, true);
        }
        int index = 0;
        while (index < args.length) {
            if (args[index].equals(NO_CONSOLE_CMD)) {
                System.out.println("No console mode.");
                useConsole = false;
                index += 1; 
                continue;
            } else if (index >= args.length - 1) {
                System.out.println("Mis-matched argument: " + args[index]);
                break;
            } 
            if (args[index].equals(GRID_SIZE_CMD)) {
                gridSize = Integer.parseInt(args[index + 1]);
                System.out.println("Setting gridSize to: " + gridSize);
                index += 2;
            } else if (args[index].equals(NUM_MINES_CMD)) {
                numberOfMines = Integer.parseInt(args[index + 1]);
                System.out.println("Setting numberOfMines to: " + numberOfMines);
                index += 2;
            } else {
                System.out.println("Unknown argument: " + args[index]);
                index ++;
            }
        }
        return new GameParams(gridSize, numberOfMines, useConsole);
    }

}
