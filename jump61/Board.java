package jump61;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Formatter;

import java.util.function.Consumer;

import static jump61.Side.*;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author Evelyn Vo
 */
class Board {

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        _notifier = NOP;
    }

    /** An N x N board in initial configuration. */
    Board(int N) {
        this();
        if (N < 2 || N > 10) {
            throw new GameException("Invalid board size.");
        }
        _size = N;
        _board = new Square[N][N];
        for (int row = 0; row < N; row += 1) {
            for (int col = 0; col < N; col += 1) {
                _board[row][col] = Square.INITIAL;
            }
        }
        _history = new ArrayList<Board>();
        markUndo();
        _readonlyBoard = new ConstantBoard(this);
        _numRed = 0;
        _numBlue = 0;
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        _size = board0.size();
        _board = new Square[_size][_size];
        _history = new ArrayList<Board>();
        Square[][] boardToCopy = new Square[_size][_size];
        for (int row = 1; row <= board0.size(); row += 1) {
            for (int col = 1; col <= board0.size(); col += 1) {
                Square origPos = board0.get(sqNum(row, col));
                this._board[row - 1][col - 1] = Square.square(origPos.getSide(),
                        origPos.getSpots());
                boardToCopy[row - 1][col - 1] = Square.square(origPos.getSide(),
                        origPos.getSpots());
            }
        }
        Board copy = new Board();
        copy._size = _size;
        copy._board = boardToCopy;
        _readonlyBoard = new ConstantBoard(this);
        _history.add(copy);
        _notifier = board0._notifier;
        setNumOfSide();
    }

    /** Sets the number of RED squares and the number
     * of BLUE squares initially. */
    void setNumOfSide() {
        _numRed = _numBlue = 0;
        for (int i = 0; i < size() * size(); i += 1) {
            Side curr = get(i).getSide();
            if (curr.equals(RED)) {
                _numRed += 1;
            } else if (curr.equals(BLUE)) {
                _numBlue += 1;
            }
        }
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /** (Re)initialize me to a cleared board with N squares on a side.
     * Clears the undo history and sets the number of moves to 0. */
    void clear(int N) {
        announce();
        Board newBoard = new Board(N);
        copy(newBoard);
    }

    /** Copy the contents of BOARD into me.
     * Clears my history and number of moves. */
    void copy(Board board) {
        _size = board.size();
        _board = new Square[_size][_size];
        for (int row = 1; row <= board.size(); row += 1) {
            for (int col = 1; col <= board.size(); col += 1) {
                Square origPos = board.get(sqNum(row, col));
                _board[row - 1][col - 1] = Square.square(origPos.getSide(),
                        origPos.getSpots());
            }
        }
        setNumOfSide();
        _history = new ArrayList<Board>();
        markUndo();
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history. Assumes BOARD and I have the same size. */
    private void internalCopy(Board board) {
        assert size() == board.size();
        for (int row = 1; row <= board.size(); row += 1) {
            for (int col = 1; col <= board.size(); col += 1) {
                Square origPos = board.get(sqNum(row, col));
                _board[row - 1][col - 1] = Square.square(origPos.getSide(),
                        origPos.getSpots());
            }
        }
        setNumOfSide();
        setNotifier(board._notifier);
        markUndo();
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return _size;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        if (!exists(n)) {
            throw new GameException("Invalid square number at get.");
        }
        int col = col(n) - 1;
        int row = row(n) - 1;
        return _board[row][col];
    }

    /** Returns the total number of spots on the board. */
    int numPieces() {
        int numSpots = 0;
        for (int i = 0; i < size() * size(); i += 1) {
            numSpots += get(i).getSpots();
        }
        if (numSpots < size() * size()) {
            throw new GameException("Every square must have at least 1 spot.");
        }
        return numSpots;
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        Side curr = ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
        return curr;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        if (!exists(n)) {
            return false;
        }
        Side curr = get(n).getSide();
        boolean validColor = curr.equals(player) || curr.equals(WHITE);
        boolean isCurrentPlayer = isLegal(player);
        return isCurrentPlayer && validColor;
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        if (whoseMove().equals(player)) {
            return true;
        }
        return false;
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        if (_numRed == size() * size()) {
            return RED;
        } else if (_numBlue == size() * size()) {
            return BLUE;
        }
        return null;
    }

    /** Returns the total number of spots on the board for a given side.
     * @param side The side that is being checked. */
    int numPieces(Side side) {
        int numSpots = 0;
        for (int i = 0; i < size() * size(); i += 1) {
            Square curr = get(i);
            if (curr.getSide().equals(side)) {
                numSpots += curr.getSpots();
            }
        }
        return numSpots;
    }

    /** Returns an integer array containing values useful for
     * static evaluation.
     * ret[0] corresponds to values for WHITE.
     * ret[1] corresponds to values for RED.
     * ret[2] corresponds to values for BLUE.
     * ret[i][0] is the number of Squares of SIDE,
     * ret[i][1] is the number of spots of SIDE,
     * ret[i][2] is the number of squares such that
     * the squares are almost overfilled,
     * and ret[i][3] is the number of spots of SIDE
     * that come from squares that are almost overfilled. */
    int[][] heuristicValues() {
        int[][] ret = new int[3][4];
        boolean almostFilled = false;
        int player;
        for (int i = 0; i < size() * size(); i += 1) {
            Square curr = get(i);
            Side currSide = curr.getSide();
            int currSpots = curr.getSpots();
            if (neighbors(i) == currSpots) {
                almostFilled = true;
            }
            if (currSide.equals(WHITE)) {
                player = 0;
            } else if (currSide.equals(RED)) {
                player = 1;
            } else {
                player = 2;
            }
            ret[player][0] += 1;
            ret[player][1] += currSpots;
            if (almostFilled) {
                ret[player][2] += 1;
                ret[player][3] = currSpots;
            }
        }
        return ret;
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        if (side.equals(WHITE)) {
            return size() * size() - _numRed - _numBlue;
        } else if (side.equals(RED)) {
            return _numRed;
        } else {
            return _numBlue;
        }
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        addSpot(player, sqNum(r, c));
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        Side prev = whoseMove();
        simpleAdd(player, n, 1);
        if (getWinner() == null) {
            Integer N = n;
            while (N != null && getWinner() == null) {
                jump(N);
                N = _workQueue.poll();
            }
            markUndo();
        }
        Side curr = whoseMove();
        if (prev.equals(curr) && getWinner() == null) {
            String s = "testing";
        }
        announce();
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white). Does not announce changes. */
    private void internalSet(int n, int num, Side player) {
        if (!exists(n)) {
            throw new GameException("Invalid square number at internalSet.");
        } else if (num < 0) {
            throw new GameException("Cannot have negative spots.");
        }
        if (num > 0) {
            updateNum(get(n), player);
            _board[n / size()][n % size()] = Square.square(player, num);
        } else {
            updateNum(get(n), WHITE);
            _board[row(n)][col(n)] = Square.square(WHITE, num);
        }
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared (i.e. cannot undo before the beginning of the current
     *  history, in the case that the current board is created from another
     *  board using Board(Board) as that does not carry over the old history),
     *  or the construction of this Board (cannot undo before the game began).
     *  Can be done multiple times (ex: can undo 5 times if there are 6 states,
     *  including the current one, in the history). */
    void undo() {
        if (_history.size() == 0) {
            throw new GameException("May have invalid history.");
        } else if (_history.size() >= 2) {
            Side beforeUndo = whoseMove();
            Square[][] stateBeforeUndo = new Square[_size][_size];
            stateBeforeUndo = _board;
            int changeHist = _history.size() - 1;
            Board state = _history.get(changeHist);
            if (equals(state)) {
                changeHist -= 1;
                internalCopy(_history.get(changeHist));
            } else {
                internalCopy(state);
            }
            ArrayList<Board> newHist = new ArrayList<Board>();
            for (int i = 0; i <= changeHist; i += 1) {
                newHist.add(_history.get(i));
            }
            _history = newHist;
            if (beforeUndo.equals(whoseMove())) {
                String s = "stop here";
            }
        }
    }

    /** Record the beginning of a move in the undo history.
     * In other words, marks the first move after undoing to a point.
     * Allows for the program to know when to undo an undo. */
    private void markUndo() {
        _history.add(new Board(this));
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /** Used in jump to keep track of squares needing processing.  Allocated
     *  here to cut down on allocations. */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /** Do all jumping on this board, assuming that initially, S is the only
     *  square that might be over-full. */
    private void jump(int S) {
        if (!exists(S)) {
            throw new GameException("Invalid square at jump.");
        }
        if (get(S).getSpots() > neighbors(S)) {
            int row = S / size();
            int col = S % size();
            Side player = get(S).getSide();
            int[] positions;
            int left = S - 1;
            int right = S + 1;
            int top = S - size();
            int bottom = S + size();
            if (row == 0) {
                if (col == 0) {
                    positions = new int[] {right, bottom};
                } else if (col == size() - 1) {
                    positions = new int[] {left, bottom};
                } else {
                    positions = new int[] {left, right, bottom};
                }
            } else if (row == size() - 1) {
                if (col == 0) {
                    positions = new int[] {top, right};
                } else if (col == size() - 1) {
                    positions = new int[] {top, left};
                } else {
                    positions = new int[] {top, left, right};
                }
            } else {
                if (col == 0) {
                    positions = new int[] {top, right, bottom};
                } else if (col == size() - 1) {
                    positions = new int[] {top, left, bottom};
                } else {
                    positions = new int[] {top, left, right, bottom};
                }
            }
            internalSet(S, 1, player);
            for (int pos: positions) {
                _workQueue.add(pos);
                simpleAdd(player, pos, 1);
            }
        }
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int index = 0; index < size() * size(); index += 1) {
            if (index % size() == 0) {
                out.format("   ");
            }
            Square curr = get(index);
            char side = curr.getSide().toString().charAt(0);
            if (side == 'w') {
                side = '-';
            }
            out.format(" %1$d%2$c", curr.getSpots(), side);
            if (index % size() == size() - 1) {
                out.format("%n");
            }
        }
        out.format("===");
        String ret = out.toString();
        out.close();
        return ret;
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    /** Updates the number of tiles of each color on the board
     * if the square is of type OLD and becomes side CURR.
     * Should be called only in internalSet as this method
     * does not check whether the current square is already a
     * given color. */
    private void updateNum(Square old, Side curr) {
        Side oldSide = old.getSide();
        if (!oldSide.equals(curr)) {
            if (curr.equals(RED)) {
                _numRed += 1;
                if (oldSide.equals(BLUE)) {
                    _numBlue -= 1;
                }
            } else if (curr.equals(BLUE)) {
                _numBlue += 1;
                if (oldSide.equals(RED)) {
                    _numRed -= 1;
                }
            } else if (curr.equals(WHITE)) {
                if (oldSide.equals(RED)) {
                    _numRed -= 1;
                } else if (oldSide.equals(BLUE)) {
                    _numBlue -= 1;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            if (B.size() != size()) {
                return false;
            } else {
                for (int i = 0; i < size() * size(); i += 1) {
                    Square a = B.get(i);
                    Square b = get(i);
                    boolean sameSpots = a.getSpots() == b.getSpots();
                    boolean sameSide = a.getSide().equals(b.getSide());
                    if (!sameSpots || !sameSide) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        return numPieces();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }

    /** Returns all the squares in a given board that would
     * to which it is legal for the player to add a spot.
     * @param player The player that is being checked. */
    public ArrayList<Square> getLegalSides(Side player) {
        ArrayList<Square> squares = new ArrayList<Square>();
        for (int i = 0; i < size() * size(); i += 1) {
            Square curr = get(i);
            Side side = curr.getSide();
            if (side.equals(player) || side.equals(WHITE)) {
                squares.add(curr);
            }
        }
        return squares;
    }

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board. */
    private Consumer<Board> _notifier;

    /** The current board. */
    private Square[][] _board;

    /** The size of the board. In other words, the length/width. */
    private int _size;

    /** The history of the game. */
    private ArrayList<Board> _history;

    /** The number of red tiles in the game. */
    private int _numRed;

    /** The number of blue tiles in the game. */
    private int _numBlue;
}
