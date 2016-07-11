import java.util.LinkedList;
import java.util.Random;

/**
 * Minesweeper: This class implements the Minesweeper game (not a solver).
 */
class Minesweeper {
    // Constants.
    private static final String ROW_GUTTER = "    ";
    private static final String FIRST_ROW_GUTTER = ROW_GUTTER + " ";
    private static final String INTER_CELL_SPACING = "  ";

    // Instance variables.
    private final int gridSize;
    private final int numberOfMines;
    private final Cell[][] grid;
    private final Random random;
    private boolean isGameOver; // Whether the game is over.

    // did the user win (AI or human). Value valid only when isGameOver== true;
    private boolean userWon;
    private int numCoveredCells;

    Minesweeper(int gridSize, int numberOfMines) {
        this.gridSize = gridSize;
        this.numberOfMines = numberOfMines;
        grid = new Cell[gridSize][gridSize];
        random = new Random(System.currentTimeMillis()); // Use a random enuf seed.
        isGameOver = false;
        userWon = false;
        generateGrid();
        numCoveredCells = gridSize * gridSize;
    }

    int getGridSize() {
        return gridSize;
    }

    int getNumberOfMines() {
        return numberOfMines;
    }

    /**
     * Exposes the cell given by (x, y). Returns true if the game has ended.
     */
    boolean exposeCell(int x, int y) {
        Cell cell = grid[x][y];
        if (cell.isMine()) {
            cell.setUncovered();
            return checkGameOver(cell);
        }
        exposeCellInternal(cell);
        return checkGameOver(cell);
    }

    boolean isGameOver() {
        return isGameOver;
    }

    boolean hasUserWon() {
        return userWon;
    }

    void updateUncoveredCellSnapshot(int[][] snapshot) {
        for (int i = 0; i < gridSize; i ++) {
            for (int j = 0; j < gridSize; j ++) {
                if (!grid[i][j].isCovered()) {
                    snapshot[i][j] = grid[i][j].getNumNeighboringMines();
                } else {
                    snapshot[i][j] = -1; // Covered cell.
                }
            }
        }
    }

    boolean isCovered(int x, int y) {
        return grid[x][y].isCovered();
    }

    private boolean checkGameOver(Cell cell) {
        if (isGameOver) {
            return true;
        }
        if (cell.isMine()) {
            isGameOver = true;
            userWon = false;
            return isGameOver;
        }
        if (numCoveredCells == numberOfMines) {
            isGameOver = true;
            userWon = true;
        }
        return isGameOver;
    }

    private void exposeCellInternal(Cell cell) {
        LinkedList<Cell> list = new LinkedList<Cell>();
        list.add(cell);
        while (!list.isEmpty()) {
            Cell target = list.removeFirst();
            if (target.isCovered() && !target.isMine()) {
                target.setUncovered();
                numCoveredCells --;
                if (target.getNumNeighboringMines() == 0) {
                    int x = target.getX();
                    int y = target.getY();
                    // add neighbors.
                    addToExposeList(list, x - 1, y);
                    addToExposeList(list, x - 1, y - 1);
                    addToExposeList(list, x - 1, y + 1);
                    addToExposeList(list, x, y + 1);
                    addToExposeList(list, x, y - 1);
                    addToExposeList(list, x + 1, y - 1);
                    addToExposeList(list, x + 1, y);
                    addToExposeList(list, x + 1, y + 1);
                }
            }
        }
    }

    private void addToExposeList(LinkedList<Cell> list, int x, int y) {
        if (!isValid(x, y)) return;
        list.addFirst(grid[x][y]);
    }

    /**
     * Generates a grid by placing the mines randomly.
     */
    private void generateGrid() {
        for (int i = 0; i < gridSize; i ++) {
            for (int j = 0; j < gridSize; j ++) {
                grid[i][j] = new Cell(i, j);
            }
        }

        // Generate mines first.
        int numGenerated = 0;
        while (numGenerated < numberOfMines) {
            int x = random.nextInt(gridSize);
            int y = random.nextInt(gridSize);
            if (!grid[x][y].isMine()) {
                grid[x][y].setMine();
                updateNeighbors(x, y);
                numGenerated ++;
            } // else retry in the next iteration.
        }
    }

    void printGrid() {
        StringBuilder strBuilder = new StringBuilder(3 * gridSize);
        strBuilder.append(FIRST_ROW_GUTTER);  // space for the column numbers.
        for (int j = 0; j < gridSize; j ++) {
            addToStringBuilder(strBuilder, j);
        }
        System.out.println(strBuilder.toString());
        System.out.println();
        for (int i = 0; i < gridSize; i ++) {
            StringBuilder row = new StringBuilder(3 * gridSize);
            row.append(i);
            row.append(ROW_GUTTER);
            for (int j = 0; j < gridSize; j ++) {
                row.append(grid[i][j].printableChar());
                row.append("   ");
            }
            System.out.println(row.toString());
        }
    }

    void exposeAllCells() {
        for (int i = 0; i < gridSize; i ++) {
            for (int j = 0; j < gridSize; j ++) {
                grid[i][j].setUncovered();
            }
        }
        isGameOver = true;
        userWon = false;
    }

    /**
     * Updates the neighbor count given the cell at x, y is a mine.
     */
    private void updateNeighbors(int x, int y) {
        // The increaseCount will check if the new noes are actually valid.
        increaseCount(x-1, y);
        increaseCount(x-1, y-1);
        increaseCount(x-1, y+1);
        increaseCount(x, y-1);
        increaseCount(x, y+1);
        increaseCount(x+1, y+1);
        increaseCount(x+1, y);
        increaseCount(x+1, y-1);
    }

    private void increaseCount(int i, int j) {
        if (!isValid(i, j)) return;
        grid[i][j].incrementNeighborCount();
    }

    boolean isValid(int x, int y) {
        return  !(x < 0 || x >= gridSize || y < 0 || y >= gridSize);
    }

    private static void addToStringBuilder(StringBuilder builder, int value) {
        builder.append(value);
        builder.append(INTER_CELL_SPACING);
        if (value < 10 ) {
            builder.append(" ");
        }
    }
}
