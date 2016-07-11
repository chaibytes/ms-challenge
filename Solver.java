import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


/**
 * This class solves a given minesweeper problem.
 */
class Solver {
    private static final int HIGH_MINE_PROB_INT = 1000;
    private static final double UNASSIGNED_PROB = 1.1;
    private Minesweeper board;
    private int gridSize;
    private int snapshot[][];

    private double mineProb[][];
    private boolean isMine[][];
    private Random random;

    // This is an integer to allow multiple floating point probability values to map to
    // the same bucket.
    private int minMineProb = HIGH_MINE_PROB_INT;  // Probabilty value for cells in 'lowProbCells'.
    private LinkedList<IntPair> lowProbCells; // Stores the list of lowest probability cells.

    // Stores a cell picked at random for whom a probability could not be computed. This happens if
    // none of this neighbors are uncoverd and have a "# of mines" value.
    private IntPair randomProblessCell;
    private int numProbLessCells; // Number of such probability-less cells.

    Solver(Minesweeper board) {
        this.board = board;
        this.gridSize = board.getGridSize();
        snapshot = new int[gridSize][gridSize];
        mineProb = new double[gridSize][gridSize];
        isMine = new boolean[gridSize][gridSize];
        lowProbCells = new LinkedList<IntPair>();
        random = new Random(System.currentTimeMillis());
    }

    void solve(BufferedReader br, boolean allowConsoleOutput) {

        // extraCellsToProcess stores the additional cells to be processed as a byproduct of processing a cell.
        // This can happen if when processing a cell, we end up marking another cell as a mine. This
        // has potential implications for other neighboring cells since we have definite knowledge
        // of a particular cell being a mine.
        LinkedList<IntPair> extraCellsToProcess = new LinkedList<IntPair>();

        // toBeUncovered stores the cells that we can potentially uncover (that the solver considers
        // as not being mines). However, in a single move we uncover only 1 cell (assuming this is the
        // rule for any solver).
        LinkedList<IntPair> toBeUncovered = new LinkedList<IntPair>();
        boolean done = false;
        while (!done) {
            // Step 0: Get the current state of the uncovered cells from the board. Reset probabilty
            // matrix, clear prior state.
            board.updateUncoveredCellSnapshot(snapshot);
            resetProbabilityMatrix();
            extraCellsToProcess.clear();
            toBeUncovered.clear();

            // Step 1: Process all cells, including multiple iterations if a cell has side-effects
            // onother cells. When processing a cell, we look at the neighbors, how many of them can
            // be mines and how many are uncovered. If we know a cell has to be a mine, we mark it
            // so internally and use this information in subsequent processing.
           
            // We keep track of the "set" of lowest probability cells, so that we can choose among
            // this in case we don't have a definite candidate for a mine. We use 'minMineProb' to
            // keep track of the probability value and 'lowProbCells' to keep track of the set of
            // these cells.
            minMineProb = HIGH_MINE_PROB_INT;
            lowProbCells.clear();
            for (int i = 0; i < gridSize; i ++) {
                for (int j = 0; j < gridSize; j ++) {
                    if (snapshot[i][j] > 0) {
                        // The cell is uncovered and has mines among its neighbors.
                        processCell(i, j, extraCellsToProcess, toBeUncovered);
                        mineProb[i][j] = 0.0; // This cell is definetly not a mine.
                    }
                }
            }

            // We might have additional cells to process since marking a cell as mine has
            // side-effects on other neighboring cells.
            MinesUtil.println("Processing additional cells.");
            while (!extraCellsToProcess.isEmpty()) {
                IntPair cell = extraCellsToProcess.removeFirst();
                if (!board.isCovered(cell.x, cell.y)) {
                    processCell(cell.x, cell.y, extraCellsToProcess, toBeUncovered);
                }
            }

            // Step 2: Pick a random probability-less cell and compute its probability.
            int problessCellProb = randomProbabilityLessCell(board);
            if (problessCellProb < minMineProb) {
                MinesUtil.println("probless cell prob: " + problessCellProb  +
                        " minMineProb: " + minMineProb);
                lowProbCells.clear();
            }

            // Step 3: Make a move. Choose a cell to uncover if we have a clear choice. Otherwise
            // choose a cell at random (with the lowest probability of being a mine).
            IntPair move = null;
            if (toBeUncovered.isEmpty()) {
                if (lowProbCells.isEmpty() || problessCellProb < minMineProb) {
                    MinesUtil.println("Low prob cells empty ! Using probless cell.");
                    move = randomProblessCell;
                } else {
                    move = lowProbCells.removeFirst();
                }
                // Choose a random low prob one.
            } else {
                move = toBeUncovered.removeFirst();
            }

            if (move != null) {
                if (allowConsoleOutput) System.out.println("Decided a move, uncover: " + move.x + " , " + move.y);
                if (board.exposeCell(move.x, move.y)) {
                    MinesUtil.println("Game ended.");
                    if (board.hasUserWon()) {
                        System.out.println("Solver won !");
                    } else {
                        System.out.println("Solver lost !");
                    }
                    done = true;
                }
                if (allowConsoleOutput) board.printGrid();
            } else {
                MinesUtil.println("Null move.");
            }

            // Do cleanup.
            toBeUncovered.clear();
            lowProbCells.clear();
            extraCellsToProcess.clear();
        }
    }

    /**
     * Processes a single cell (x, y):
     * Looks at neighbors and the # of cells that are covered among them. Also looks at the # of
     * cells that we have previously marked at mines. Uses this information to compute cells that
     * can either be marked as a mine or uncovered in the next move.
     * Also computes any cells that might have a side-effect as a result of marking a cell as a
     * mine.
     */
    private void processCell(int x, int y, List<IntPair> toBeProcessed, List<IntPair> toBeUncovered) {
        int numNeighborMines = snapshot[x][y];

        // numCovered counts the # of neighbors of (x, y) that are still covered (unexposed).
        int numCovered = 0;
        // numMarkedAsMines counts the # of uncovered neighbors that we have marked as mines.
        int numMarkedAsMines = 0;

        // List of all (valid) neighbors.
        LinkedList<IntPair> neighbors = new LinkedList<IntPair>();

        // List of cells that are not marked as mines but are covered.
        LinkedList<IntPair> availableCells = new LinkedList<IntPair>();
        MinesUtil.computeNeighborList(x, y, gridSize, neighbors);
        MinesUtil.println("Cell(x = " + x + ", y = " + y + " has " + neighbors.size() + " neighbors.");

        // First among the neighbors look at the # of cells that are covered and # marked as mines.
        for (IntPair pair : neighbors) {
            int covered = isCovered(pair.x, pair.y);
            int markedAsMine = isMarkedAsMine(pair.x, pair.y);
            if (covered == 0 && markedAsMine == 1) {
                // This should not happen.
                MinesUtil.println("Unmarking mine as cell got uncovered:" + pair.x +
                        ", " + pair.y);
                // unmark cell as mine.
                markedAsMine = 0;
            }
            numCovered += covered;
            numMarkedAsMines += markedAsMine;
            if (markedAsMine == 0 && covered == 1) {
                availableCells.add(pair);
            }
            if (markedAsMine == 1) {
                MinesUtil.println("Have " + pair.x + ", " + pair.y + " as a mine.");
            }
        }
        MinesUtil.println("processCell(" + x + ", " + y +
                "),  numNeighborMines = " + numNeighborMines +
                ", numCovered = " + numCovered +
                " numMarkedAsMines = " + numMarkedAsMines +
                " availableCells.size = " + availableCells.size());

        // If we have all the mines account for, we can uncover the remaining cells.
        if (numNeighborMines  == numMarkedAsMines) {
            if (availableCells.size() > 0) {
                MinesUtil.println("Marking " + availableCells.size() + " cells as uncovered.");
                for (IntPair pair : availableCells) {
                    MinesUtil.println("Cell: " + pair.x + " , " + pair.y);
                }
                toBeUncovered.addAll(availableCells);
            }
            availableCells.clear();
        } else if (numNeighborMines == numCovered) {
            // Mark all available cells as mines since this is a direct match..
            MinesUtil.println("Marking all available cells as mines: " + availableCells.size());
            for (IntPair pair : availableCells) {
                MinesUtil.println("Cell: " + pair.x + " , " + pair.y);
                markAsMine(pair.x, pair.y);
                numMarkedAsMines ++;
                // toBeProcessed.add(pair);
                MinesUtil.computeNeighborList(pair.x, pair.y, gridSize, toBeProcessed);
            }
            availableCells.clear();
        }

        // If there are any remaining cells that are covered but are neither mines nor candidates
        // for being uncovered, we compute a probability of these being mines. This is useful if we
        // have to later pick a cell at random. We update the 'lowProbCells' list with such cells
        // always maintaining the set of cells with the lowest probability.
        int remainingMines = numNeighborMines - numMarkedAsMines;
        if (remainingMines > 0 && availableCells.size() > 0) {
            MinesUtil.println("remainingMines = " + remainingMines + " availableCells.size = " + availableCells.size());
            double prob = (double) remainingMines / (double) availableCells.size();
            MinesUtil.println("Prob of being a mine (among neighbors) = " + prob);
            for (IntPair pair : availableCells) {
                if (mineProb[pair.x][pair.y] > 1.0) { // if it is uninitialized.
                    mineProb[pair.x][pair.y] = prob;
                } else {
                    mineProb[pair.x][pair.y] = Math.max(mineProb[pair.x][pair.y], prob); 
                }
                updateLowProbCells(pair, mineProb[pair.x][pair.y]);
            }
        }
    }

    private void updateLowProbCells(IntPair cell, double prob) {
        int probInt = (int) (prob * 1000.0);
        if (probInt < minMineProb) {
            MinesUtil.println("Creating new low prob cell set :" + probInt +
                    " x = " + cell.x + " y = " + cell.y);
            lowProbCells.clear();
            lowProbCells.add(cell);
            minMineProb = probInt;
        } else if (probInt == minMineProb) {
            MinesUtil.println("Adding low prob cell:" + cell.x + ", " + cell.y);
            lowProbCells.add(cell);
        } else {
            MinesUtil.println("Ignoring high prob cell:" + probInt);
        }
    }

    // Resets the values in the probability matrix to "unassigned" values, namely,
    // UNASSIGNED_PROB.
    private void resetProbabilityMatrix() {
        for (int i = 0; i < gridSize; i ++) {
            for (int j = 0; j < gridSize; j ++) {
                mineProb[i][j] = UNASSIGNED_PROB;
            }
        }
    }

    private int randomProbabilityLessCell(Minesweeper board) {
        numProbLessCells = 0; // Cells which do not have a prob computed.
        randomProblessCell = null;
        double sumOfCellsWithProb = 0.0;
        int numMarkedAsMines = 0;
        for (int i = 0; i < gridSize; i ++) {
            for (int j = 0; j < gridSize; j ++) {
                if (isMine[i][j]) {
                    sumOfCellsWithProb += 1.0;
                    numMarkedAsMines ++;
                } else if (snapshot[i][j] == -1) {
                    if (mineProb[i][j] < 1.0) {
                        sumOfCellsWithProb += mineProb[i][j];
                    } else {
                        // A cell for which we could not directly compute a probability.
                        numProbLessCells ++;
                        // Keep a cell chosen uniformly at random from such cells.
                        if (randomProblessCell == null) {
                            randomProblessCell = new IntPair(i, j);
                        } else if (shouldReplace(numProbLessCells)) {
                            randomProblessCell = new IntPair(i, j);
                        }
                    } 
                }
            }
        }
        MinesUtil.println("Number we have marked as mines: " + numMarkedAsMines);
        MinesUtil.println("Number of cells without prob:" + numProbLessCells);
        double remainingMines = (double) board.getNumberOfMines() - sumOfCellsWithProb;
        MinesUtil.println("Remaining mines:" + remainingMines); 
        remainingMines = Math.max(remainingMines, 1.0); // Lower bound to 1.0
        return (int) (1000.0 / (double) numProbLessCells);
    }

    /**
     * Returns 1, if cell x, y is covered, 0 otherwise.
     */
    private int isCovered(int x, int y) {
        if (!board.isValid(x, y)) return 0;
        if (board.isCovered(x, y)) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Returns 1, if cell x, y is marked as a mine, 0 otherwise.
     */
    private int isMarkedAsMine(int x, int y) {
        return (isMine[x][y] ? 1 : 0);
    }

    /**
     * Marks a given cell as a (potential) mine internally for the solver.
     */
    private void markAsMine(int x, int y) {
        isMine[x][y] = true;
    }

    // An online algorithm to choose a node uniformly at random with prob 1/N as N increases.
    // Returns true, if the previous selection (for N-1) should be replaced with the newly seen
    // node, false otherwise.
    private boolean shouldReplace(int n) {
        return random.nextInt(n) > n - 2;
    }

    private void userInterrupt(BufferedReader br) {
        MinesUtil.print("\nSolver >> ");
        try {
            String command = br.readLine();
        } catch (IOException e) {
            MinesUtil.println("Ignoring input exception.");
        }
    }

}

