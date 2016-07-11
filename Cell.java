

/**
 * Represents a single cell in the Minesweeper game.
 */
class Cell {
    private static final char COVERED_CHAR = 'X';
    private static final char MINE_CHAR = 'M';
    private static final char NOMINE_CHAR = '.';

    private boolean isMine; // Does this cell hold a mine.
    private boolean isCovered;  // Whether this cell has been uncovered.
    private int numNeighboringMines;  // # of mines in neighboring cells.
    private final int x;
    private final int y;

    /**
     * Initialize this cell.
     * Input parameter indicates if this cell is a mine.
     */
    Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.isMine = false;
        this.isCovered = true;
        this.numNeighboringMines = 0;
    }

    /**
     * Uncovers a cell.
     */
    void setUncovered() {
        isCovered = false;
    }

    boolean isCovered() {
        return isCovered;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    int getNumNeighboringMines() {
        return numNeighboringMines;
    }

    /**
     * Mark this cell as a mine.
     */
    void setMine() {
        isMine = true;
    }

    boolean isMine() {
        return isMine;
    }

    void incrementNeighborCount() {
        numNeighboringMines ++;
    }

    char printableChar() {
        if (isCovered) {
            return COVERED_CHAR;
        }
        if (isMine) {
            return MINE_CHAR;
        }
        if (numNeighboringMines == 0) {
            return NOMINE_CHAR;
        }
        return Character.forDigit(numNeighboringMines, 10);
    }
}
