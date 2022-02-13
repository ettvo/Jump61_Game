package jump61;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static jump61.Utils.*;

/** A Player that gets its moves from manual input.
 *  @author Evelyn Vo
 */
class HumanPlayer extends Player {

    /** A new player initially playing COLOR taking manual input of
     *  moves from GAME's input source. */
    HumanPlayer(Game game, Side color) {
        super(game, color);
    }

    /** Syntax of a move.  Groups capture row and column. */
    private static final Pattern MOVE_PATN =
        Pattern.compile("(\\d+)\\s+(\\d+)\\b");

    @Override
    String getMove() {
        Game game = getGame();
        Board board = getBoard();
        while (true) {
            String cmnd = game.getCommand();
            Matcher m = MOVE_PATN.matcher(cmnd);
            if (!m.matches() || board.isLegal(getSide(), toInt(m.group(1)),
                                              toInt(m.group(2)))) {
                return cmnd;
            }
            game.reportError("invalid move: %s", cmnd);
        }
    }

}
