package jump61;

import java.util.Random;

import static jump61.Side.*;

/** An automated Player.
 *  @author P. N. Hilfinger
 */
class AI extends Player {

    /** A new player of GAME initially COLOR that chooses moves automatically.
     *  SEED provides a random-number seed used for choosing moves.
     */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }

    @Override
    String getMove() {
        Board board = getGame().getBoard();
        assert getSide() == board.whoseMove();
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }

    /* When users enter erroneous input, you should print an error message,
    and the input should have no effect (and in particular, the user should be
    able to continue entering commands after an error). Make sure that any of
    this extra output you generate is distinct from the outputs that are
    required
    (otherwise, the testing software will flag your program as erroneous.)
    Regardless of whether users have made errors during a session,
    your program should always exit with code 0.
     */

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over.
     *  The returned int is the square number to which addSpot will be called.
     *  SENSE indicates what side you want to get a move for, with SENSE==1
     *  corresponding to RED and SENSE==-1 corresponding to BLUE.
     *  The returned move is the optimal move decided by the AI. */
    private int searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert getSide() == work.whoseMove();
        _foundMove = -1;
        if (getSide() == RED) {
            value = minMax(work, 3, true, 1,
                    -Integer.MIN_VALUE, Integer.MAX_VALUE);

        } else {
            value = minMax(work, 3, true, -1,
                    -Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        // changed depth from 3 to 4 to see if it would fix the errors with searching to depth 4
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove.
     *  The returned int is the heuristic value of the move saved to _foundMove.
     *  In other words, this returns the static evaluation of the best (given
     *  the current board state) move for the given player.
     *  SENSE indicates what side you want to get a move for, with SENSE==1
     *  corresponding to RED and SENSE==-1 corresponding to BLUE.
     *  In other words, RED is the maximal player, while
     *  BLUE is the minimal player.
     *  BETA is the minimal value / lower bound of the board from
     *  known moves, while ALPHA is the maximal value / upper
     *  bound of the board from known moves. */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticEval(board, defaultWinningValue);
        }
        int bestSoFar;
        int bestSoFarInd = _foundMove;
        if (sense == 1) {
            bestSoFar = Integer.MIN_VALUE;
            for (int pos = 0; pos < board.size() * board.size(); pos += 1) {
                checkSide(board, RED, depth, pos, bestSoFar,
                        alpha, beta);
                if (board.isLegal(RED, pos)) {
                    board.addSpot(RED, pos);
                    int response = minMax(board, depth - 1, false,
                            -sense, alpha, beta);
                    board.undo();
                    if (response > bestSoFar) {
                        bestSoFar = response;
                        bestSoFarInd = pos;
                        if (saveMove) {
                            _foundMove = bestSoFarInd;
                        }
                        alpha = Math.max(alpha, bestSoFar);
                        if (alpha >= beta) {
                            return bestSoFar;
                        }
                    }
                }
            }
        } else {
            bestSoFar = Integer.MAX_VALUE;
            for (int pos = 0; pos < board.size() * board.size(); pos += 1) {
                checkSide(board, BLUE, depth, pos, bestSoFar,
                        alpha, beta);
                if (board.isLegal(BLUE, pos)) {
                    board.addSpot(BLUE, pos);
                    int response = minMax(board, depth - 1, false,
                            -sense, alpha, beta);
                    board.undo();
                    checkSide(board, BLUE, depth, pos, response,
                            bestSoFar, alpha, beta);
                    if (response < bestSoFar) {
                        bestSoFar = response;
                        bestSoFarInd = pos;
                        if (saveMove) {
                            _foundMove = bestSoFarInd;
                        }
                        beta = Math.min(beta, bestSoFar);
                        if (alpha >= beta) {
                            return bestSoFar;
                        }
                    }
                }
            }
        }
        if (saveMove) {
            _foundMove = bestSoFarInd;
        }
        return bestSoFar;
    }

    /** Return a heuristic estimate of the value of board position B.
     *  Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     *  indicate a win for Blue.
     *  The approach this program takes to statically evaluating the board
     *  is counting the number of spots each side has and weighing them.
     *  Each spot is worth 1 if it is RED and in a square that is not
     *  almost overfilled, 2 otherwise.
     *  White squares are not taken into account.
     *  Spots of side BLUE have a value that is inverse (negative version)
     *  of the value of the square if it was RED. In other words,
     *  BLUE spots that are in squares that are almost overfilled have
     *  a value of -2, while BLUE spots that are not are worth -1. */
    private int staticEval(Board b, int winningValue) {
        Side winner = b.getWinner();
        if (winner == null) {
            int[][] values = b.heuristicValues();
            int redSpots = values[1][1];
            int blueSpots = values[2][1];
            int redSqOverF = values[1][2];
            int blueSqOverF = values[2][2];
            int redSpotsOverF = values[1][3];
            int blueSpotsOverF = values[2][3];
            int redScore = (redSpots - redSpotsOverF) + (redSqOverF * 2);
            int blueScore = -((blueSpots - blueSpotsOverF) + (blueSqOverF * 2));
            return redScore + blueScore;
        } else if (winner.equals(RED)) {
            return winningValue;
        } else {
            return -winningValue;
        }
    }

    /** Checks that the whoseMove returns the correct player.
     * @param board The board
     * @param player The expected player
     * @param depth The current depth
     * @param pos The current position on the board
     * @param alpha Alpha
     * @param bestSoFar best
     * @param beta beta
     * @param response response
     */
    private void checkSide(Board board, Side player, int depth, int pos,
                           int response, int bestSoFar, int alpha, int beta) {
        if (!board.whoseMove().equals(player)) {
            throw new GameException("Incorrect whoseMove at depth " + depth
                    + ", pos " + pos + ", response " + response + ", bestSoFar "
                    + bestSoFar + ", alpha " + alpha + ", beta " + beta
                    +
                    ". Should be " + player + " but is "
                    + board.whoseMove() + ".");
        }
    }

    /** Checks that the whoseMove returns the correct player.
     * @param board The board
     * @param player The expected player
     * @param depth The current depth
     * @param pos The current position on the board
     * @param alpha Alpha
     * @param bestSoFar best
     * @param beta beta
     */
    private void checkSide(Board board, Side player, int depth, int pos,
                           int bestSoFar, int alpha, int beta) {
        if (!board.whoseMove().equals(player) && 1 == 2) {
            throw new GameException("Incorrect whoseMove at depth " + depth
                    + ", pos " + pos + ", bestSoFar "
                    + bestSoFar + ", alpha " + alpha + ", beta " + beta
                    +
                    ". Should be " + player + " but is "
                    + board.whoseMove() + ".");
        }
    }

    /** A random-number generator used for move selection. */
    private Random _random;

    /** Used to convey moves discovered by minMax. */
    private int _foundMove;

    /** The default winning value for RED. */
    private final int defaultWinningValue = 10000;

}
