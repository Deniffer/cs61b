package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;
    private int stepCount;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;
    private  static final int NO_AVAILABLE_ROW = -1;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
        this.stepCount = 0;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.
        // for all cols in board
        this.board.setViewingPerspective(side);
        for(int col=0;col < size();col++) {
            if(iterateCol(col)) {
                changed =true;
            }
        }

        checkGameOver();
        this.board.setViewingPerspective(Side.NORTH);
        if (changed) {
            setChanged();
        }
        return changed;
    }

    private Tile getNearbyTile(int col,int currentRow) {
        Tile nearbyTile = null;
        for (int row = currentRow - 1; row >= 0; row--) {
             nearbyTile = board.tile(col,row);
            if (nearbyTile != null) {
                break;
            }
        }
        return nearbyTile;
    }

    private boolean iterateCol(int col) {
        /**
         *  c,r
         *  0,3  1,3  2,3  3,3
         *  0,2  1,2  2,2  3,2
         *  0,1  1,1  2,1  3,1
         *  0,0  1,0  2,0  3,0
         */

        Board board = this.board;
        boolean changed = false;
        boolean [] merged = new boolean[size()];
        // start from second one
        for (int row = board.size() - 2; row >= 0; row --) {
            Tile tile = board.tile(col, row);
            if (tile == null) {
                continue;
            }
            int siblingTileRow = findNearbySiblingTileRow(col,row);
            if (siblingTileRow != NO_AVAILABLE_ROW && equalTile(tile,board.tile(col,siblingTileRow)) && !merged[siblingTileRow]) {
                boolean needUpdateScore = board.move(col, siblingTileRow, tile);
                merged[siblingTileRow] = true;
                if (needUpdateScore) {
                    this.score += tile.value() * 2;
                }
                System.out.println(String.format("%d. merge and place to %s siblingTile: , mergeTile %s:", this.stepCount, tile, tile.next()));
                changed = true;
            } else if (hasAvailableRow(col, row)) {
                int desiredRowValue = getDesireRowValue(col, row);
                board.move(col,desiredRowValue,tile);
                changed = true;
                System.out.println(String.format("%d. from %s move to %s",this.stepCount,tile,tile.next()));
            }
            this.stepCount++;
        }
        return  changed;
    }

    private int findNearbySiblingTileRow(int col, int row) {
        Tile siblingTile = null;
        Board board = this.board;
        if (row == size() -1) {
            return  NO_AVAILABLE_ROW;
        }
        for (row++;row < size();row++) {
            siblingTile = board.tile(col, row);
            // siblingTile Not merged yet!
            if (siblingTile != null) {
                return  row;
            }
        }
        return  NO_AVAILABLE_ROW;
    }

    private boolean hasAvailableRow(int col, int currentRow) {
        int availableRowValue = getDesireRowValue(col, currentRow);
        System.out.println(String.format("availableRowValue : %d, %s",availableRowValue,this.board.tile(col,currentRow)));
        return  availableRowValue != NO_AVAILABLE_ROW;
    }
    private int getDesireRowValue (int col, int currentRow) {
        int availableRowValue = size() - 1;
        for (;availableRowValue > currentRow; availableRowValue --) {
            if (this.board.tile(col,availableRowValue) == null) {
                return  availableRowValue;
            }
        }
        return  NO_AVAILABLE_ROW;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        for (int i = 0; i < b.size() ; i++) {
            for (int j = 0; j < b.size(); j++) {
                if (b.tile(i,j) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                if (b.tile(i,j) != null && b.tile(i,j).value() == MAX_PIECE) {
                    return  true;
                }
            }
        }
        return false;
    }

    private static  boolean equalTile(Tile tile, Tile otherTile) {
        if(tile == null || otherTile == null) {
            return false;
        }
        return  tile.value() == otherTile.value();
    }

    private static boolean validSibling(int col, int row, int size) {
        return col > 0 && row > 0 && col < size && row < size;
    }

    private  static boolean hasSameValueSibling(int row, int col, Board b) {
        Tile currentTile = b.tile(col,row);
        Tile leftTile = validSibling(col-1,row, b.size()) ? b.tile(col-1,row) : null;
        Tile rightTile = validSibling(col+1,row, b.size()) ? b.tile(col+1,row) : null;
        Tile bottomTile = validSibling(col, row-1, b.size()) ? b.tile(col,row-1) : null;
        Tile topTile = validSibling(col,row+1, b.size()) ? b.tile(col,row+1) : null;
        if (equalTile(currentTile,leftTile) || equalTile(currentTile,rightTile) || equalTile(currentTile,bottomTile) || equalTile(currentTile,topTile)) {
            return true;
        }
        return false;
    }
    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        if (emptySpaceExists(b)) {
            return true;
        }

        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                if (hasSameValueSibling(i,j,b)) {
                    return true;
                }
            }
        }

        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model???s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
