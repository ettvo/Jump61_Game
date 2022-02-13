package jump61;

import static jump61.Side.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests of AI.
 *  @author Evelyn Vo */

public class AITest {

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

    private static final String NL = System.getProperty("line.separator");

    /** Tests that AI makes legal moves. */
    @Test
    public void legal() {

    }

    /** Tests that AI gives different heuristics depending on
     * the board. */
    @Test
    public void heuristics() {
        Board B = new Board(4);
    }

    /** Tests that AI does not change the original board. */
    @Test
    public void doesNotChangeOriginal() {

    }

    /** Tests that AI can play against itself in a 3x3 board. */
    @Test
    public void solo3x3() {

    }

    /** Tests that AI can play against itself in a 4x4 board. */
    @Test
    public void solo4x4() {

    }

    /** Tests that AI can play against itself in a 5x5 board. */
    @Test
    public void solo5x5() {

    }

    /** Tests that AI can play against itself in a 3x3 board. */
    @Test
    public void solo6x6() {

    }

    /** Finds a win in a minimal board (2x2). */
    @Test
    public void findWin00() {

    }

    /** Finds a win in a 3x3 board. */
    @Test
    public void findWin01() {

    }

    /** Finds a win in a 3x3 board. */
    @Test
    public void findWin02() {

    }

    /** Finds a win in a 6x6 board in which a forced win is
     * 1 move away. */
    @Test
    public void findWin03() {

    }

    /** Finds a win in a 6x6 board in which a forced win is
     * 2 moves away. */
    @Test
    public void findWin04() {

    }

    /** Finds a win in a 6x6 board in which a forced win is
     * 4 moves away. */
    @Test
    public void findWin05() {

    }

    /** Prevents a loss in a minimal board (2x2). */
    @Test
    public void preventLoss00() {

    }

    /** Prevents a loss in a 3x3 board. */
    @Test
    public void preventLoss01() {

    }

    /** Prevents a loss in a 3x3 board. */
    @Test
    public void preventLoss02() {

    }

    /** Prevents a loss in a 6x6 board in which a forced win is
     * 1 move away. The AI must survive at least 1 move. */
    @Test
    public void preventLoss03() {

    }

    /** Prevents a loss in a 6x6 board in which a forced win is
     * 2 moves away. The AI must survive at least 2 moves. */
    @Test
    public void preventLoss04() {

    }

    /** Prevents a loss in a 6x6 board in which a forced win is
     * 4 moves away. The AI must survive at least 4 moves. */
    @Test
    public void preventLoss05() {

    }

    /** Prevents a loss in a 6x6 board in which a forced win is
     * 5 moves away. The AI must survive at least 5 moves. */
    @Test
    public void preventLoss06() {

    }

    /** Prevents a loss in a 6x6 board in which a forced win is
     * 6 moves away. The AI must survive at least 6 moves. */
    @Test
    public void preventLoss07() {

    }

    /* Autograder Tests */

    /** Runs the 05-findforcedwin-1.in test. */
    @Test
    public void staff05() {

    }

    /** Runs the 06-playstaff1-1.in test. */
    @Test
    public void staff06a() {

    }

    /** Runs the 06-playstaff2-1.in test. */
    @Test
    public void staff06b() {

    }

    /* Testing Utilities */

    private void testFile(String path) throws FileNotFoundException {
        FileReader reader = new FileReader(new File(path));
        Scanner scanner = new Scanner(reader);
        ArrayList<String> input = new ArrayList<String>();
        for (int index = 0; scanner.hasNext(); index += 1) {
            input.add(scanner.nextLine());
        }
        String[] args = new String[input.size()];
        for (int index = 0; index < input.size(); index += 1) {
            args[index] = input.get(index);
        }
        Main.main(args);
    }

}
