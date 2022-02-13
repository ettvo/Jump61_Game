package jump61;

/** Describes a source of input commands.  The possible text commands are as
 *  follows (parts of a command are separated by whitespace):
 *    - SIZE s:   Replace the current board with one that is s x s cells.
 *                Then start a new puzzle.  Requires that s be an
 *                integer numeral > 2.
 *    - NEW:      Start a new game with current parameters.
 *    - <row>:<col> Add a spot to the cell at (<row>, <col>).
 *    - UNDO:     Go back one move.
 *    - REDO:     Go forward one previously undone move.
 *    - SEED s:   Set a new random seed.
 *    - QUIT:     Exit the program.
 *  @author P. N. Hilfinger
 */
interface CommandSource {

    /** Returns one command string, trimmed of preceding and following
     *  whitespace and converted to upper case.  If the CommandSource
     *  prompts for input, use PROMPT, if not null, to do so. */
    String getCommand(String prompt);

}
