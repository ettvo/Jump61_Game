package jump61;

import static jump61.Side.*;
import static jump61.GameException.error;
import static jump61.Utils.*;

/** Main logic for playing (a) game(s) of Jump61.
 *  @author Evelyn Vo
 */
class Game {

    /** Name of resource containing help message. */
    private static final String HELP = "jump61/Help.txt";

    /** A list of all commands. */
    private static final String[] COMMAND_NAMES = {
        "auto", "board", "clear", "dump", "help", "manual",
        "new", "q", "quiet", "quit",
        "seed", "set", "size", "start", "verbose",
    };

    /** A new Game that takes command/move input from INP, logs
     *  commands if LOGGING, displays the board using VIEW, and uses REPORTER
     *  for messages to the user and error messages. SEED is intended to
     *  seed a random number generator, if one is used in an AI.
     */
    Game(CommandSource inp, View view, Reporter reporter, boolean logging) {
        _inp = inp;
        _view = view;
        _reporter = reporter;
        _logging = logging;
        _seed = (long) (Math.random() * Long.MAX_VALUE);

        _board = new Board(Defaults.BOARD_SIZE);
        _readonlyBoard = new ConstantBoard(_board);
        _board.setNotifier((b) -> _view.update(b));
    }

    /** Returns a readonly view of the game board.  This board remains valid
     *  throughout the session. */
    Board getBoard() {
        return _readonlyBoard;
    }

    /** Return true iff the current game is not over. */
    boolean gameInProgress() {
        return _board.getWinner() == null;
    }

    /** Play a session of Jump61.  This may include multiple games,
     *  and proceeds until the user exits.  Returns an exit code: 0 is
     *  normal; any positive quantity indicates an error.  */
    int play() {
        boolean winnerAnnounced;

        System.out.println("Welcome to " + Defaults.VERSION);
        _board.clear(Defaults.BOARD_SIZE);
        setManual(RED);
        setAuto(BLUE);
        _exit = -1;
        winnerAnnounced = false;
        while (_exit < 0) {
            String cmnd;
            _view.update(_board);
            if (_board.getWinner() == null) {
                winnerAnnounced = false;
                try {
                    executeCommand(getPlayer(_board.whoseMove()).getMove());
                } catch (GameException e) {
                    reportError(e.getMessage());
                }
            } else if (!gameInProgress()) {
                if (!winnerAnnounced) {
                    _reporter.announceWin(_board.getWinner());
                    winnerAnnounced = true;
                }
                executeCommand(getCommand());
            }
        }
        return _exit;
    }

    /** Return a suggested prompt for command input. */
    private String prompt() {
        if (gameInProgress()) {
            return String.format("%s> ", _board.whoseMove());
        } else {
            return "+> ";
        }
    }

    /** Return a command from the current source. */
    String getCommand() {
        String cmnd = _inp.getCommand(prompt());
        if (cmnd == null) {
            return "quit";
        } else {
            return cmnd;
        }
    }

    /** Add a spot to R C, if legal to do so. */
    void makeMove(int r, int c) {
        assert _board.isLegal(_board.whoseMove(), r, c);
        _board.addSpot(_board.whoseMove(), r, c);
        if (_verbose) {
            printBoard();
        }
    }

    /** Add a spot to square #N, if legal to do so. */
    void makeMove(int n) {
        assert _board.isLegal(_board.whoseMove(), n);
        _board.addSpot(_board.whoseMove(), n);
        if (_verbose) {
            printBoard();
        }
    }

    /** Return the side denoted by COLOR (which must be lower case).  */
    Side toSide(String color) {
        switch (color) {
        case "red": case "r":
            return RED;
        case "blue": case "b":
            return BLUE;
        default:
            throw error("invalid side color: %s", color);
        }
    }

    /** Report a move by PLAYER to ROW COL. */
    void reportMove(int row, int col) {
        _reporter.announceMove(row, col);
    }

    /** Send a message to the user as determined by FORMAT and ARGS, which
     *  are interpreted as for String.format or PrintWriter.printf. */
    void message(String format, Object... args) {
        _reporter.msg(format, args);
    }

    /** Send announcement of winner to my user output. */
    private void announceWinner() {
        _reporter.msg("%s wins.", _board.getWinner().toCapitalizedString());
    }

    /** Make the player of COLOR an AI for subsequent moves. */
    private void setAuto(Side color) {
        setPlayer(color, new AI(this, color, _seed));
        _seed += 1;
    }

    /** Make the player of COLOR take manual input from the user for
     *  subsequent moves. */
    private void setManual(Side color) {
        setPlayer(color, new HumanPlayer(this, color));
    }

    /** Return the Player playing COLOR. */
    private Player getPlayer(Side color) {
        return _players[color.ordinal()];
    }

    /** Set getPlayer(COLOR) to PLAYER. */
    private void setPlayer(Side color, Player player) {
        _players[color.ordinal()] = player;
    }

    /** Clear the board to its initial state. */
    void clear() {
        _board.clear(_board.size());
    }

    /** Print the current board using standard board-dump format. */
    private void dump() {
        _reporter.msg(_board.toString());
    }

    /** Print a board with row/column numbers. */
    private void printBoard() {
        _reporter.msg(_board.toDisplayString());
    }

    /** Print a help message. */
    private void help() {
        printHelpResource(HELP, System.out);
    }

    /** Seed the random-number generator with SEED. */
    private void setSeed(long seed) {
        _seed = seed;
    }

    /** Place SPOTS spots on square R:C and color the square red or
     *  blue depending on whether COLOR is "r" or "b".  If SPOTS is
     *  0, clears the square, ignoring COLOR.  SPOTS must be less than
     *  the number of neighbors of square R, C. */
    private void setSpots(int r, int c, int spots, String color) {
        if (_board.exists(r, c) && spots >= 0
            && spots <= _board.neighbors(r, c)) {
            _board.set(r, c, spots, toSide(color));
        } else {
            throw error("invalid request to put %d spots on square %d %d",
                        spots, r, c);
        }
    }

    /** Stop any current game and set the board to an empty N x N board
     *  with numMoves() == 0.  Requires 2 <= N <= 10. */
    private void setSize(int n) {
        log("size %d", n);
        if (n < 2 || n > 10) {
            throw error("size must be between 2 and 10");
        }
        _board.clear(n);
    }

    /** Return the full, lower-case command name that uniquely fits
     *  COMMAND.  COMMAND may be any prefix of a valid command name,
     *  as long as that name is unique.  If the name is not unique or
     *  no command name matches, returns COMMAND in lower case. */
    private String canonicalizeCommand(String command) {
        if (command.length() == 0) {
            return  "";
        } else if (command.startsWith("#")) {
            return "#";
        }

        String fullName;
        fullName = null;
        for (String name : COMMAND_NAMES) {
            if (name.equals(command)) {
                return command;
            }
            if (name.startsWith(command)) {
                if (fullName != null) {
                    throw error("%s is not a unique command abbreviation",
                                command);
                }
                fullName = name;
            }
        }
        if (fullName == null) {
            return command;
        } else {
            return fullName;
        }
    }

    /** Execute command CMND.  Throws GameException on errors. */
    private void executeCommand(String cmnd) {
        String[] parts = cmnd.trim().toLowerCase().split("\\s+");
        log(cmnd);
        try {
            switch (canonicalizeCommand(parts[0])) {
            case "#": case "":
                break;
            case "auto":
                setAuto(toSide(parts[1]));
                break;
            case "board":
                printBoard();
                break;
            case "dump":
                dump();
                break;
            case "help":
                help();
                break;
            case "manual":
                setManual(toSide(parts[1]));
                break;
            case "new":
                clear();
                break;
            case "quiet":
                _verbose = false;
                break;
            case "quit": case "q":
                _exit = 0;
                break;
            case "seed":
                setSeed(toLong(parts[1]));
                break;
            case "set":
                setSpots(toInt(parts[1]), toInt(parts[2]), toInt(parts[3]),
                         parts[4]);
                break;
            case "size":
                setSize(toInt(parts[1]));
                break;
            case "verbose":
                _verbose = true;
                break;
            default:
                makeMove(Integer.parseInt(parts[0]),
                         Integer.parseInt(parts[1]));
                break;
            }
        } catch (NumberFormatException excp) {
            reportError("Bad number in: %s", cmnd);
        } catch (ArrayIndexOutOfBoundsException excp) {
            reportError("Argument(s) missing: %s", cmnd);
        } catch (GameException excp) {
            reportError(excp.getMessage());
        }
    }

    /** Print a message on the logging stream, if any, appending a newline.
     *  The arguments FORMAT and ARGS have the same meaning as for
     *  String.format. */
    private void log(String format, Object... args) {
        if (_logging) {
            System.out.printf(format + "%n", args);
        }
    }

    /** Send an error message to the user formed from arguments FORMAT
     *  and ARGS, whose meanings are as for printf. */
    void reportError(String format, Object... args) {
        _reporter.err(format, args);
        if (Main.strict()) {
            _exit = 1;
        }
    }

    /** Returns command input for the current game. */
    private final CommandSource _inp;
    /** Outlet for responses to the user. */
    private final Reporter _reporter;

    /** The board on which I record all moves. */
    private final Board _board;
    /** A readonly view of _board. */
    private final Board _readonlyBoard;
    /** Displayer of boards. */
    private View _view;
    /** True iff we are logging commands. */
    private boolean _logging;

    /** True iff we should print the board after each move. */
    private boolean _verbose;
    /** Current pseudo-random number seed.  Provided as an argument to AIs
     *  that use a random element in their choices.  Incremented for each
     *  AI to which it is supplied.
     */
    private long _seed;
    /** When set to a non-negative value, indicates that play should terminate
     *  at the earliest possible point, returning _exit.  When negative,
     *  indicates that the session is not over. */
    private int _exit;

    /** Current players, indexed by color (RED, BLUE). */
    private final Player[] _players = new Player[Side.values().length];

   /** Used to return a move entered from the console.  Allocated
     *  here to avoid allocations. */
    private final int[] _move = new int[2];
}
