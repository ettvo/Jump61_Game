package jump61;

import static jump61.Side.*;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests of Boards.
 *  @author Evelyn Vo
 */

public class BoardTest {

    /* Testing Utilities */

    /** Checks that B conforms to the description given by CONTENTS.
     *  CONTENTS should be a sequence of groups of 4 items:
     *  r, c, n, s, where r and c are row and column number of a square of B,
     *  n is the number of spots that are supposed to be there and s is the
     *  color (RED or BLUE) of the square.  All squares not listed must
     *  be WHITE with one spot.  Raises an exception signaling a unit-test
     *  failure if B does not conform. */
    private void checkBoard(String msg, Board B, Object... contents) {
        for (int k = 0; k < contents.length; k += 4) {
            String M = String.format("%s at %d %d", msg, contents[k],
                    contents[k + 1]);
            assertEquals(M, (int) contents[k + 2],
                    B.get((int) contents[k],
                            (int) contents[k + 1]).getSpots());
            assertEquals(M, contents[k + 3],
                    B.get((int) contents[k],
                            (int) contents[k + 1]).getSide());
        }
        int c;
        c = 0;
        for (int i = B.size() * B.size() - 1; i >= 0; i -= 1) {
            assertTrue("bad white square #" + i,
                    (B.get(i).getSide() != WHITE)
                            || (B.get(i).getSpots() == 1));
            if (B.get(i).getSide() != WHITE) {
                c += 1;
            }
        }
        assertEquals("extra squares filled", contents.length / 4, c);
    }

    /** Switches the current player. */
    private void switchPlayer() {
        if (player.equals(RED)) {
            player = BLUE;
        } else {
            player = RED;
        }
    }

    /** Switches the current player and checks that whoseMove()
     * for Board B is equal to the current player.
     * @param B The board to be checked.
     */
    private void switchAndCheck(Board B) {
        switchPlayer();
        assertTrue(B.whoseMove().equals(player));
    }

    private static final String NL = System.getProperty("line.separator");

    private Side player;

    /* Valid Tests */

    @Test
    public void testSize() {
        Board B = new Board(5);
        assertEquals(5, B.size());
        assertEquals("bad length", 5, B.size());
        assertEquals(25, B.numPieces());
        ConstantBoard C = new ConstantBoard(B);
        assertEquals("bad length", 5, C.size());
        assertEquals(25, C.numPieces());
        Board D = new Board(C);
        assertEquals("bad length", 5, D.size());
        assertEquals(25, D.numPieces());
    }

    @Test
    public void testSet() {
        Board B = new Board(5);
        assertEquals(5, B.size());
        assertEquals(25, B.numPieces());
        B.set(2, 2, 1, RED);
        assertEquals("wrong number of spots", 1, B.get(2, 2).getSpots());
        assertEquals("wrong color", RED, B.get(2, 2).getSide());
        assertEquals("wrong count", 1, B.numOfSide(RED));
        assertEquals("wrong count", 0, B.numOfSide(BLUE));
        assertEquals("wrong count", 24, B.numOfSide(WHITE));
        assertEquals(5, B.size());
    }

    @Test
    public void testMove() {
        Board B = new Board(6);
        assertEquals(6, B.size());
        assertEquals(36, B.numPieces());
        checkBoard("#0", B);
        player = RED;
        B.addSpot(RED, 1, 1);
        assertEquals(37, B.numPieces());
        switchAndCheck(B);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        switchAndCheck(B);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        switchAndCheck(B);
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
        B.undo();
        switchAndCheck(B);
        checkBoard("#2U", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.undo();
        switchAndCheck(B);
        checkBoard("#1U", B, 1, 1, 2, RED);
        B.undo();
        switchAndCheck(B);
        checkBoard("#0U", B);
        assertEquals(6, B.size());
        assertEquals(36, B.numPieces());
    }

    @Test
    public void gameSimple00() {
        Board B = new Board(2);
        assertEquals(2, B.size());
        assertEquals(4, B.numPieces());
        checkBoard("#0", B);
        player = RED;
        B.addSpot(BLUE, 1, 1);
        switchAndCheck(B);
        checkBoard("#1", B, 1, 1, 2, BLUE);
        B.addSpot(RED, 1, 2);
        switchAndCheck(B);
        checkBoard("#2", B, 1, 1, 2, BLUE, 1, 2, 2, RED);
        B.addSpot(BLUE, 2, 1);
        switchAndCheck(B);
        checkBoard("#3", B, 1, 1, 2, BLUE, 1, 2, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(BLUE, 1, 2);
        switchAndCheck(B);
        assertTrue(B.getWinner().equals(BLUE));
    }

    @Test
    public void gameBasic01() {
        Board B = new Board(4);
        assertEquals(4, B.size());
        assertEquals(16, B.numPieces());
        player = RED;
        B.set(1, 1, 2, RED);
        checkBoard("#0", B, 1, 1, 2, RED);
        B.set(2, 1, 2, BLUE);
        checkBoard("#1", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        assertEquals(4, B.size());
        B = new Board(5);
        checkBoard("#2", B);
        assertEquals(5, B.size());
        assertEquals(25, B.numPieces());

    }

    @Test
    public void gameSimp02() {
        Board B = new Board(3);
        assertEquals(3, B.size());
        assertEquals(9, B.numPieces());
        checkBoard("#0", B);
        assertEquals(3, B.size());
        player = RED;
        B.addSpot(RED, 1, 1);
        switchAndCheck(B);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 3);
        switchAndCheck(B);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 3, 2, BLUE);
        B.addSpot(RED, 2, 2);
        switchAndCheck(B);
        checkBoard("#3", B, 1, 1, 2, RED, 2, 3, 2, BLUE, 2, 2, 2, RED);
        B.addSpot(BLUE, 3, 3);
        switchAndCheck(B);
        checkBoard("#4", B, 1, 1, 2, RED, 2, 3, 2, BLUE, 2, 2, 2, RED,
                3, 3, 2, BLUE);
        B.addSpot(RED, 2, 2);
        switchAndCheck(B);
        checkBoard("#5", B, 1, 1, 2, RED, 2, 3, 2, BLUE, 2, 2, 3, RED,
                3, 3, 2, BLUE);
        B.addSpot(BLUE, 2, 3);
        switchAndCheck(B);
        checkBoard("#6", B, 1, 1, 2, RED, 2, 3, 3, BLUE, 2, 2, 3, RED,
                3, 3, 2, BLUE);
        B.addSpot(RED, 1, 3);
        switchAndCheck(B);
        checkBoard("#7", B, 1, 1, 2, RED, 2, 3, 3, BLUE, 2, 2, 3, RED,
                3, 3, 2, BLUE,
                1, 3, 2, RED);
        B.addSpot(BLUE, 2, 3);
        switchAndCheck(B);
        checkBoard("#8", B, 1, 1, 2, RED, 1, 2, 2, BLUE, 1, 3, 1, BLUE,
                2, 2, 4, BLUE, 2, 3, 3, BLUE,
                3, 2, 2, BLUE, 3, 3, 1, BLUE);
        B.addSpot(RED, 2, 1);
        switchAndCheck(B);
        checkBoard("#9", B, 1, 1, 2, RED, 1, 2, 2, BLUE,
                1, 3, 1, BLUE,
                2, 1, 2, RED, 2, 2, 4, BLUE, 2, 3, 3, BLUE,
                3, 2, 2, BLUE, 3, 3, 1, BLUE);
        B.addSpot(BLUE, 3, 1);
        switchAndCheck(B);
        B.addSpot(RED, 2, 1);
        switchAndCheck(B);
        B.addSpot(BLUE, 2, 2);
        switchAndCheck(B);
        assertTrue(B.getWinner().equals(BLUE));
        assertEquals(3, B.size());
    }
}
