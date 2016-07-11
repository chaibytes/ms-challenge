import java.util.List;

/**
 * Collection of utility functions.
 */
class MinesUtil {
    private static final boolean DBG = false;

    static void computeNeighborList(int x, int y, int gridSize, List<IntPair> neighborList) {
        if (!isValid(x, y, gridSize)) return;
        addValidNodeToList(x - 1, y - 1, gridSize, neighborList);
        addValidNodeToList(x - 1, y, gridSize, neighborList);
        addValidNodeToList(x - 1, y + 1, gridSize, neighborList);
        addValidNodeToList(x, y - 1, gridSize, neighborList);
        addValidNodeToList(x, y + 1, gridSize, neighborList);
        addValidNodeToList(x + 1, y - 1, gridSize, neighborList);
        addValidNodeToList(x + 1, y, gridSize, neighborList);
        addValidNodeToList(x + 1, y + 1, gridSize, neighborList);
    }

    static boolean isValid(int x, int y, int gridSize) {
        return  !(x < 0 || x >= gridSize || y < 0 || y >= gridSize);
    }

    static void addValidNodeToList(int x, int y, int gridSize, List<IntPair> neighborList) {
        if (isValid(x, y, gridSize)) neighborList.add(new IntPair(x, y));
    }

    static void println(String message) {
        if (DBG) System.out.println(message);
    }

    static void print(String message) {
        if (DBG) System.out.print(message);
    }
}
