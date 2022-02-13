package jump61;

import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import static jump61.Utils.*;

import ucb.util.CommandArgs;

/** The jump61 game.
 * @author P. N. Hilfinger
 */
public class Main {

    /** Location of usage message resource. */
    static final String USAGE = "jump61/Usage.txt";

    /** Play jump61.  ARGS0 may consist of the single string
     *  '--display' to indicate that the game is played using a GUI. Prints
     *  a usage message if the arguments are wrong. */
    public static void main(String[] args0) {
        CommandArgs args =
            new CommandArgs("--display{0,1} --strict{0,1} --version{0,1}"
                            + " --debug=(\\d+){0,1} --log --=(.*){0,}", args0);

        if (!args.ok()) {
            usage();
            return;
        }

        if (args.contains("--version")) {
            System.err.printf("Version %s%n", Defaults.VERSION);
            System.exit(0);
        }

        _strict = args.contains("--strict");
        boolean log = args.contains("--log");
        if (args.contains("--debug")) {
            Utils.setMessageLevel(args.getInt("--debug"));
        }

        Game game;
        if (args.contains("--display")) {
            Display display = new Display("Jump61");
            game = new Game(display, display, display, log);
            game.play();
        } else {
            TextSource source;
            ArrayList<Reader> inReaders = new ArrayList<>();
            if (args.get("--").isEmpty()) {
                inReaders.add(new InputStreamReader(System.in));
            } else {
                for (String name : args.get("--")) {
                    if (name.equals("-")) {
                        inReaders.add(new InputStreamReader(System.in));
                    } else {
                        try {
                            inReaders.add(new FileReader(name));
                        } catch (IOException excp) {
                            System.err.printf("Could not open %s", name);
                            System.exit(1);
                        }
                    }
                }
            }
            game = new Game(new TextSource(inReaders), (b) -> { },
                    new TextReporter(), log);
            System.exit(game.play());
        }
    }

    /** Return true if in strict mode, where user errors are not allowed and
     *  cause error exit from the program. */
    static boolean strict() {
        return _strict;
    }

    /** Print usage message. */
    private static void usage() {
        printHelpResource(USAGE, System.err);
    }

    /** True if we are to run in strict mode. */
    private static boolean _strict;
    /** True if we should log moves and commands. */
    private static boolean _log;

}
