/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package jump61;

/** An object that formats and sends messages and errors.
 *  @author P. N. Hilfinger
 */
interface Reporter {

    /** Display an announcement that SIDE has won. */
    void announceWin(Side side);

    /** Report a move to ROW COL. */
    void announceMove(int row, int col);

    /** Display a message indicated by FORMAT and ARGS, which have
     *  the same meaning as in String.format. */
    void msg(String format, Object... args);

    /** Report an error as specified by FORMAT and ARGS, which have
     *  the same meaning as in String.format. */
    void err(String format, Object... args);

}

