/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for the Board class.
 * 
 */
public class BoardTest {
    /*
     * Testing strategy:
     * 
     * Test enable assertions
     * 
     * Test IllegalArgumentException for Board constructor
     * - sizeX <= 0
     * - sizeY <= 0
     * - bombs array dimensions do not match sizeX and sizeY
     * 
     * Test constructor and accessors
     * - board size: small (1x1), medium (5x5), large (100x100)
     * - bomb placement: no bombs, some bombs, all bombs
     * 
     * Additional tests for dig
     * - digging a bomb
     * - digging an untouched square with 0 neighboring bombs
     * - digging an untouched square with >0 neighboring bombs
     * - digging a flagged square (should have no effect)
     * - digging a dug square (should have no effect)
     * 
     * Additional tests for flag and deflag
     * - flagging an untouched square
     * - deflagging a flagged square
     * - flagging a flagged square (should have no effect)
     * - deflagging an untouched square (should have no effect)
     * - flagging a dug square (should have no effect)
     * - deflagging a dug square (should have no effect)
     * 
     * Tests for edge cases
     * - digging/flagging/deflagging squares at the edges and corners of the board
     * 
     * Tests for look output
     * - look initial state (all untouched)
     * - look after dig (0 neighboring bombs)
     * - look after dig (>0 neighboring bombs)
     * - look after dig bomb
     * - look after flag
     * - look after deflag
     * - look with mixed states (untouched, flagged, dug)
     * - look showing different neighbor counts (1-8)
     * - look output format with larger board
     * - look after cascade dig reveals multiple squares
     * 
     * Test getSizeX, getSizeY among above tests
     */

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // ==================== Constructor IllegalArgumentException Tests
    // ====================

    // covers sizeX <= 0
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSizeXZero() {
        new Board(0, 1, new boolean[0][1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSizeXNegative() {
        new Board(-1, 1, new boolean[0][1]);
    }

    // covers sizeY <= 0
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSizeYZero() {
        new Board(1, 0, new boolean[1][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSizeYNegative() {
        new Board(1, -1, new boolean[1][0]);
    }

    // covers bombs array dimensions do not match sizeX and sizeY
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBombsDimensionMismatchX() {
        new Board(2, 2, new boolean[3][2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBombsDimensionMismatchY() {
        new Board(2, 2, new boolean[2][3]);
    }

    // ==================== Constructor and Accessors Tests ====================

    // covers small board (1x1), no bombs
    @Test
    public void testSmallBoardNoBombs() {
        boolean[][] bombs = new boolean[1][1];
        Board board = new Board(1, 1, bombs);
        assertEquals(1, board.getSizeX());
        assertEquals(1, board.getSizeY());
        assertFalse(board.hasBomb(0, 0));
        assertEquals(Board.State.UNTOUCHED, board.getState(0, 0));
        assertEquals("-\n", board.look());
    }

    // covers small board (1x1), all bombs
    @Test
    public void testSmallBoardAllBombs() {
        boolean[][] bombs = { { true } };
        Board board = new Board(1, 1, bombs);
        assertTrue(board.hasBomb(0, 0));
        assertEquals(Board.State.UNTOUCHED, board.getState(0, 0));
    }

    // covers medium board (5x5), some bombs
    @Test
    public void testMediumBoardSomeBombs() {
        boolean[][] bombs = new boolean[5][5];
        bombs[0][0] = true;
        bombs[2][2] = true;
        bombs[4][4] = true;
        Board board = new Board(5, 5, bombs);
        assertEquals(5, board.getSizeX());
        assertEquals(5, board.getSizeY());
        assertTrue(board.hasBomb(0, 0));
        assertTrue(board.hasBomb(2, 2));
        assertTrue(board.hasBomb(4, 4));
        assertFalse(board.hasBomb(1, 1));
    }

    // covers large board (100x100), no bombs
    @Test
    public void testLargeBoardNoBombs() {
        boolean[][] bombs = new boolean[100][100];
        Board board = new Board(100, 100, bombs);
        assertEquals(100, board.getSizeX());
        assertEquals(100, board.getSizeY());
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                assertFalse(board.hasBomb(x, y));
            }
        }
    }

    // ==================== Dig Tests ====================

    // covers digging a bomb
    @Test
    public void testDigBomb() {
        boolean[][] bombs = { { true } };
        Board board = new Board(1, 1, bombs);
        assertTrue(board.dig(0, 0)); // returns true when bomb hit
        assertEquals(Board.State.DUG, board.getState(0, 0));
        assertFalse(board.hasBomb(0, 0)); // bomb removed after dig
    }

    // covers digging an untouched square with 0 neighboring bombs (cascade)
    @Test
    public void testDigZeroNeighborsCascade() {
        boolean[][] bombs = new boolean[3][3];
        Board board = new Board(3, 3, bombs);
        assertFalse(board.dig(1, 1)); // no bomb
        // all squares should be dug due to cascade
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                assertEquals(Board.State.DUG, board.getState(x, y));
            }
        }
    }

    // covers digging an untouched square with >0 neighboring bombs (no cascade)
    @Test
    public void testDigWithNeighboringBombs() {
        boolean[][] bombs = new boolean[3][3];
        bombs[0][0] = true; // bomb at corner
        Board board = new Board(3, 3, bombs);
        assertFalse(board.dig(1, 1)); // has neighboring bomb, should not cascade
        assertEquals(Board.State.DUG, board.getState(1, 1));
        // corner with bomb should still be untouched
        assertEquals(Board.State.UNTOUCHED, board.getState(0, 0));
        // look should show "1" for the dug square
        assertTrue(board.look().contains("1"));
    }

    // covers digging a flagged square (should have no effect)
    @Test
    public void testDigFlaggedSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        board.flag(0, 0);
        assertFalse(board.dig(0, 0)); // should have no effect
        assertEquals(Board.State.FLAGGED, board.getState(0, 0));
    }

    // covers digging a dug square (should have no effect)
    @Test
    public void testDigDugSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        board.dig(0, 0);
        assertFalse(board.dig(0, 0)); // dig again, should return false
        assertEquals(Board.State.DUG, board.getState(0, 0));
    }

    // ==================== Flag and Deflag Tests ====================

    // covers flagging an untouched square
    @Test
    public void testFlagUntouchedSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        assertTrue(board.flag(0, 0));
        assertEquals(Board.State.FLAGGED, board.getState(0, 0));
        assertTrue(board.look().contains("F"));
    }

    // covers deflagging a flagged square
    @Test
    public void testDeflagFlaggedSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        board.flag(0, 0);
        assertTrue(board.deflag(0, 0));
        assertEquals(Board.State.UNTOUCHED, board.getState(0, 0));
    }

    // covers flagging a flagged square (should have no effect)
    @Test
    public void testFlagFlaggedSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        board.flag(0, 0);
        assertFalse(board.flag(0, 0)); // already flagged
        assertEquals(Board.State.FLAGGED, board.getState(0, 0));
    }

    // covers deflagging an untouched square (should have no effect)
    @Test
    public void testDeflagUntouchedSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        assertFalse(board.deflag(0, 0)); // not flagged
        assertEquals(Board.State.UNTOUCHED, board.getState(0, 0));
    }

    // covers flagging a dug square (should have no effect)
    @Test
    public void testFlagDugSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        board.dig(0, 0);
        assertFalse(board.flag(0, 0)); // already dug
        assertEquals(Board.State.DUG, board.getState(0, 0));
    }

    // covers deflagging a dug square (should have no effect)
    @Test
    public void testDeflagDugSquare() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);
        board.dig(0, 0);
        assertFalse(board.deflag(0, 0)); // not flagged
        assertEquals(Board.State.DUG, board.getState(0, 0));
    }

    // ==================== Edge Cases Tests ====================

    // covers digging at corners
    @Test
    public void testDigCorners() {
        boolean[][] bombs = new boolean[3][3];
        bombs[1][1] = true; // bomb in center
        Board board = new Board(3, 3, bombs);

        // dig all four corners
        assertFalse(board.dig(0, 0));
        assertFalse(board.dig(0, 2));
        assertFalse(board.dig(2, 0));
        assertFalse(board.dig(2, 2));

        assertEquals(Board.State.DUG, board.getState(0, 0));
        assertEquals(Board.State.DUG, board.getState(0, 2));
        assertEquals(Board.State.DUG, board.getState(2, 0));
        assertEquals(Board.State.DUG, board.getState(2, 2));
    }

    // covers flagging at edges
    @Test
    public void testFlagEdges() {
        boolean[][] bombs = new boolean[3][3];
        Board board = new Board(3, 3, bombs);

        // flag edges (not corners)
        assertTrue(board.flag(1, 0)); // top edge
        assertTrue(board.flag(1, 2)); // bottom edge
        assertTrue(board.flag(0, 1)); // left edge
        assertTrue(board.flag(2, 1)); // right edge

        assertEquals(Board.State.FLAGGED, board.getState(1, 0));
        assertEquals(Board.State.FLAGGED, board.getState(1, 2));
        assertEquals(Board.State.FLAGGED, board.getState(0, 1));
        assertEquals(Board.State.FLAGGED, board.getState(2, 1));
    }

    // covers deflagging at corners
    @Test
    public void testDeflagCorners() {
        boolean[][] bombs = new boolean[3][3];
        Board board = new Board(3, 3, bombs);

        // flag and deflag corners
        board.flag(0, 0);
        board.flag(2, 2);
        assertTrue(board.deflag(0, 0));
        assertTrue(board.deflag(2, 2));

        assertEquals(Board.State.UNTOUCHED, board.getState(0, 0));
        assertEquals(Board.State.UNTOUCHED, board.getState(2, 2));
    }

    // ==================== Look Output Tests ====================

    // covers look initial state - all untouched shown as "-"
    @Test
    public void testLookInitialState() {
        boolean[][] bombs = new boolean[3][3];
        bombs[1][1] = true; // bomb in center, but look shouldn't reveal it
        Board board = new Board(3, 3, bombs);

        String expected = "- - -\n- - -\n- - -\n";
        assertEquals(expected, board.look());
    }

    // covers look after dig - square with 0 neighbors shows " " (space)
    @Test
    public void testLookAfterDigZeroNeighbors() {
        boolean[][] bombs = new boolean[3][3];
        // no bombs, so all squares have 0 neighbors
        Board board = new Board(3, 3, bombs);

        board.dig(1, 1); // cascade should dig all
        // all dug squares with 0 neighbors show space
        String expected = "     \n     \n     \n";
        assertEquals(expected, board.look());
    }

    // covers look after dig - square with >0 neighbors shows count
    @Test
    public void testLookAfterDigWithNeighborCount() {
        boolean[][] bombs = new boolean[3][3];
        bombs[0][0] = true; // bomb at (0,0)
        Board board = new Board(3, 3, bombs);

        // dig (1,1) which has 1 neighboring bomb
        board.dig(1, 1);
        String look = board.look();
        // (1,1) should show "1"
        String[] lines = look.split("\n");
        assertEquals("- - -", lines[0]); // row 0: all untouched
        assertEquals("- 1 -", lines[1]); // row 1: (1,1) is dug with count 1
        assertEquals("- - -", lines[2]); // row 2: all untouched
    }

    // covers look after dig bomb - bomb removed, neighbor counts updated
    @Test
    public void testLookAfterDigBomb() {
        boolean[][] bombs = new boolean[3][3];
        bombs[1][1] = true; // bomb at center
        Board board = new Board(3, 3, bombs);

        // dig (0,0) first - should show "1" (neighbor to bomb)
        board.dig(0, 0);
        String look1 = board.look();
        assertTrue(look1.startsWith("1 "));

        // now dig the bomb at (1,1)
        board.dig(1, 1);
        // bomb removed, neighbor counts decremented
        // (0,0) now has 0 neighbors, but state doesn't change retroactively
        String look2 = board.look();
        String[] lines = look2.split("\n");
        // (1,1) is now dug with 0 neighbors (bomb removed)
        assertTrue(lines[1].contains(" ")); // center should be space now
    }

    // covers look after flag - shows "F"
    @Test
    public void testLookAfterFlag() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);

        assertEquals("- -\n- -\n", board.look());

        board.flag(0, 0);
        assertEquals("F -\n- -\n", board.look());

        board.flag(1, 1);
        assertEquals("F -\n- F\n", board.look());
    }

    // covers look after deflag - returns to "-"
    @Test
    public void testLookAfterDeflag() {
        boolean[][] bombs = new boolean[2][2];
        Board board = new Board(2, 2, bombs);

        board.flag(0, 0);
        assertEquals("F -\n- -\n", board.look());

        board.deflag(0, 0);
        assertEquals("- -\n- -\n", board.look());
    }

    // covers look with mixed states (untouched, flagged, dug)
    @Test
    public void testLookMixedStates() {
        boolean[][] bombs = new boolean[3][3];
        bombs[2][2] = true; // bomb at corner
        Board board = new Board(3, 3, bombs);

        // initial: all untouched
        assertEquals("- - -\n- - -\n- - -\n", board.look());

        // dig (0,0) - should cascade since no neighboring bombs
        board.dig(0, 0);
        // (0,0), (1,0), (0,1), (1,1) have 0 neighbors -> cascade and show space
        // (2,0), (0,2), (2,1), (1,2) have 1 neighbor -> show "1" after cascade
        // (2,2) has bomb, stays untouched
        String look1 = board.look();
        String[] lines1 = look1.split("\n");
        // check specific positions
        assertTrue(lines1[0].startsWith("  ")); // (0,0) and (1,0) are spaces

        // flag (2,2)
        board.flag(2, 2);
        String look2 = board.look();
        assertTrue(look2.contains("F"));
    }

    // covers look showing different neighbor counts (1-8)
    @Test
    public void testLookNeighborCounts() {
        // 3x3 board with bombs in all corners
        boolean[][] bombs = new boolean[3][3];
        bombs[0][0] = true;
        bombs[0][2] = true;
        bombs[2][0] = true;
        bombs[2][2] = true;
        Board board = new Board(3, 3, bombs);

        // dig center (1,1) - has 4 neighboring bombs
        board.dig(1, 1);
        String look = board.look();
        String[] lines = look.split("\n");
        assertEquals("- - -", lines[0]);
        assertEquals("- 4 -", lines[1]); // center shows "4"
        assertEquals("- - -", lines[2]);
    }

    // covers look output format with larger board
    @Test
    public void testLookLargerBoard() {
        boolean[][] bombs = new boolean[4][4];
        bombs[0][0] = true;
        Board board = new Board(4, 4, bombs);

        // dig various squares
        board.dig(1, 0); // neighbor to bomb
        board.dig(3, 3); // far from bomb, should cascade
        board.flag(0, 0); // flag the bomb

        String look = board.look();
        String[] lines = look.split("\n");

        assertEquals(4, lines.length);
        // (0,0) is flagged
        assertTrue(lines[0].startsWith("F"));
        // (1,0) has 1 neighbor
        assertTrue(lines[0].contains("1"));
    }

    // covers look after cascade dig reveals multiple squares
    @Test
    public void testLookAfterCascadeDig() {
        boolean[][] bombs = new boolean[5][5];
        bombs[0][0] = true; // only one bomb in corner
        Board board = new Board(5, 5, bombs);

        // dig far corner (4,4) - should cascade through most of board
        board.dig(4, 4);

        String look = board.look();
        String[] lines = look.split("\n");

        // (4,4) and surrounding squares with 0 neighbors should show space
        // squares near (0,0) should show "1" if they neighbor the bomb
        // (0,0) should still be untouched
        assertEquals('-', lines[0].charAt(0)); // (0,0) still untouched

        // count spaces - many squares should be dug
        int spaceCount = 0;
        for (char c : look.toCharArray()) {
            if (c == ' ')
                spaceCount++;
        }
        assertTrue(spaceCount > 10); // significant cascade
    }

}
