/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package jump61;

/** An object that formats and sends messages and errors.
 *  @author P. N. Hilfinger
 */
class TextReporter implements Reporter {

    @Override
    public void announceWin(Side side) {
        msg("* %s wins.", side.toCapitalizedString());
    }

    @Override
    public void announceMove(int row, int col) {
        msg("* %d %d.", row, col);
    }

    @Override
    public void msg(String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }

    @Override
    public void err(String format, Object... args) {
        System.err.printf(format, args);
        System.err.println();
    }

}
