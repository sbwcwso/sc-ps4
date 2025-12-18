/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

/**
 * Represents a Minesweeper board with a grid of squares.
 * Each square can be in one of three states: untouched, flagged, or dug.
 * Each square may or may not contain a bomb.
 * 
 */
public class Board {
    /*
     * Abstraction function:
     * AF(board, sizeX, sizeY) = a Minesweeper board with width `sizeX` and
     * height `sizeY`, where `board[x][y]` represents the square at
     * coordinates (x, y). For each square:
     * - `board[x][y].state` is one of UNTOUCHED, FLAGGED, or DUG
     * - `board[x][y].hasBomb` indicates whether a bomb is present
     *
     * Representation invariant:
     * - board != null
     * - board.length == sizeX
     * - for all x: board[x] != null && board[x].length == sizeY
     * - for all x,y: board[x][y] != null
     * - sizeX > 0 && sizeY > 0
     *
     * Safety from rep exposure:
     * - All mutable fields are private and never returned directly to callers.
     * - `State` is an enum (immutable) so it is safe to return from accessors.
     * - No method exposes references to the `Square` objects or the internal
     * `board` array; callers only receive primitive/immutable values.
     *
     * Thread safety:
     * - This class is NOT thread-safe. Concurrent access to mutating methods
     * (`dig`, `flag`, `deflag`) may result in races and inconsistent state.
     * - To make it thread-safe (Problem 3), protect mutable state either by
     * synchronizing public mutators/accessors or by using explicit locks
     * (e.g. `ReentrantLock`) and documenting the chosen concurrency policy.
     */
    private final int sizeX;
    private final int sizeY;
    private final Square[][] board;

    /**
     * Represents a single square on the board.
     */
    private static class Square {
        private State state;
        private boolean hasBomb;
        private int neighborBombs;

        public Square(boolean hasBomb) {
            this.state = State.UNTOUCHED;
            this.hasBomb = hasBomb;
            this.neighborBombs = 0;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public boolean hasBomb() {
            return hasBomb;
        }

        public void removeBomb() {
            this.hasBomb = false;
        }

        public int getNeighborBombs() {
            return neighborBombs;
        }

        public void incNeighborBombs() {
            neighborBombs++;
        }

        public void decNeighborBombs() {
            neighborBombs--;
        }
    }

    /**
     * The possible states of a square.
     */
    public enum State {
        UNTOUCHED, FLAGGED, DUG
    }

    /**
     * Creates a new board with the given dimensions and bomb placements.
     * 
     * @param sizeX width of the board
     * @param sizeY height of the board
     * @param bombs 2D array where bombs[x][y] is true if square (x,y) has a bomb
     * @throws IllegalArgumentException if sizeX or sizeY are non-positive, or if
     *                                  bombs array dimensions do not match sizeX
     *                                  and sizeY
     */
    public Board(int sizeX, int sizeY, boolean[][] bombs) {
        if (sizeX <= 0 || sizeY <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive");
        }
        if (bombs.length != sizeX) {
            throw new IllegalArgumentException("Bombs array width does not match sizeX");
        }
        for (int x = 0; x < sizeX; x++) {
            if (bombs[x].length != sizeY) {
                throw new IllegalArgumentException("Bombs array height does not match sizeY");
            }
        }
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.board = new Square[sizeX][sizeY];
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                board[x][y] = new Square(bombs[x][y]);
            }
        }
        // initialize neighbor counts
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (board[x][y].hasBomb()) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0)
                                continue;
                            int nx = x + dx, ny = y + dy;
                            if (isValidCoordinate(nx, ny)) {
                                board[nx][ny].incNeighborBombs();
                            }
                        }
                    }
                }
            }
        }
        checkRep();
    }

    /**
     * Checks the representation invariant.
     */
    private void checkRep() {
        assert board != null;
        assert board.length == sizeX;
        for (int x = 0; x < sizeX; x++) {
            assert board[x] != null;
            assert board[x].length == sizeY;
            for (int y = 0; y < sizeY; y++) {
                assert board[x][y] != null;
            }
        }
        assert sizeX > 0 && sizeY > 0;
    }

    /**
     * Returns the width of the board.
     */
    public int getSizeX() {
        return sizeX;
    }

    /**
     * Returns the height of the board.
     */
    public int getSizeY() {
        return sizeY;
    }

    /**
     * Returns the state of the square at (x, y).
     */
    public State getState(int x, int y) {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        return board[x][y].getState();
    }

    /**
     * Returns true if the square at (x, y) has a bomb.
     */
    public boolean hasBomb(int x, int y) {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        return board[x][y].hasBomb();
    }

    /**
     * Digs the square at (x, y) according to Minesweeper rules.
     * Returns true if a bomb was hit (BOOM), false otherwise.
     */
    public boolean dig(int x, int y) {
        if (!isValidCoordinate(x, y) || getState(x, y) != State.UNTOUCHED) {
            return false; // Do nothing
        }

        Square square = board[x][y];
        square.setState(State.DUG);

        if (square.hasBomb()) {
            removeBombAt(x, y); // Remove the bomb and update neighbor counts
            return true; // BOOM
        }

        // If no neighboring bombs, recursively dig neighbors
        if (countNeighborBombs(x, y) == 0) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    int nx = x + dx, ny = y + dy;
                    if (isValidCoordinate(nx, ny) && getState(nx, ny) == State.UNTOUCHED) {
                        dig(nx, ny); // Recursive dig
                    }
                }
            }
        }

        return false;
    }

    /**
     * Remove a bomb at (x,y) and decrement neighbor counts for its neighbors.
     */
    private void removeBombAt(int x, int y) {
        if (!isValidCoordinate(x, y))
            return;
        Square s = board[x][y];
        if (!s.hasBomb())
            return;
        s.removeBomb();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0)
                    continue;
                int nx = x + dx, ny = y + dy;
                if (isValidCoordinate(nx, ny)) {
                    board[nx][ny].decNeighborBombs();
                }
            }
        }
    }

    /**
     * Flags the square at (x, y).
     * Returns true if successful, false if invalid coordinates or not untouched.
     */
    public boolean flag(int x, int y) {
        if (!isValidCoordinate(x, y) || getState(x, y) != State.UNTOUCHED) {
            return false;
        }
        board[x][y].setState(State.FLAGGED);
        return true;
    }

    /**
     * Deflags the square at (x, y).
     * Returns true if successful, false if invalid coordinates or not flagged.
     */
    public boolean deflag(int x, int y) {
        if (!isValidCoordinate(x, y) || getState(x, y) != State.FLAGGED) {
            return false;
        }
        board[x][y].setState(State.UNTOUCHED);
        return true;
    }

    /**
     * Returns a string representation of the board for the LOOK command.
     */
    public String look() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                sb.append(getDisplayChar(x, y));
                if (x < sizeX - 1)
                    sb.append(" ");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Returns the character to display for the square at (x, y).
     */
    private String getDisplayChar(int x, int y) {
        State state = getState(x, y);
        switch (state) {
            case UNTOUCHED:
                return "-";
            case FLAGGED:
                return "F";
            case DUG:
                int count = countNeighborBombs(x, y);
                return count == 0 ? " " : String.valueOf(count);
        }
        return "?"; // Should not happen
    }

    /**
     * Counts the number of neighboring squares that have bombs.
     */
    private int countNeighborBombs(int x, int y) {
        if (!isValidCoordinate(x, y))
            throw new IllegalArgumentException("Invalid coordinates");
        return board[x][y].getNeighborBombs();
    }

    /**
     * Checks if (x, y) is a valid coordinate.
     */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY;
    }
}
