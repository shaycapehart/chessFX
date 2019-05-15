package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the Piece Class. It is an abstract class from which all the actual pieces are inherited.
 * It defines all the function common to all the pieces The move() function an abstract function
 * that has to be overridden in all the inherited class It implements Cloneable interface as a copy
 * of the piece is required very often
 */
public abstract class Piece {

  protected static List<Step> trace = new ArrayList<>();
  protected static boolean ended = false;
  protected Set<Pos> moveDeltas;
  private String color; // W or B
  private String id = null; // prnbqk are black pieces and PRNBQK are white pieces
  private Pos pos;
  private King myKing;
  private boolean neverMoved;

  public Piece(String color, Pos pos) {
    if (!"WB".contains(color)) {
      System.out.println("Color must be either W or B");
      System.exit(1);
    }
    this.color = color;
    this.pos = pos;
    this.neverMoved = true;
  }

  public abstract Piece copy();

  public static boolean isOccupied(Pos move, Board board) {
    return (board.getPiece(move) != null);
  }

  public static boolean isEnded() {
    return ended;
  }

  public static void setEnded(boolean ended) {
    Piece.ended = ended;
  }

  public static List<Step> getTrace() {
    return trace;
  }

  public static Piece getLastMovePiece() {
    return trace.size() > 0 ? trace.get(trace.size() - 1).getPiece() : null;
  }

  public static Step getLastStep() {
    return trace.size() > 0 ? trace.get(trace.size() - 1) : null;
  }

  public static String getWhoseTurn() {
    String color = trace.isEmpty() ? "W" : trace.get(trace.size() - 1).getPiece().getOpositeColor();
    if (ended) {
      color = getLastMovePiece().getColor() + " win the game";
    }
    return color;
  }

  public static int getTotalRound() {
    return trace.size() / 2;
  }

  /**
   * Returns true if the target square is empty or has enemy piece.
   *
   * @param board
   * @param target
   * @return
   */
  public boolean isEmptyOrEnemy(Board board, Pos target) {
    Piece destination = board.getPiece(target);
    if ((destination == null || destination.getColor() != getColor())) {
      return true;
    }
    return false;
  }

  /**
   * get all valid moves given the current board status. The valid move shouldn't lead its king get
   * attacked after moving. In addition, valid move should clear attack if its king under attack
   * before move.
   *
   * @param board
   * @return
   */
  public Set<Pos> getValidMoves(Board board) {
    if (!getWhoseTurn().equals(getColor())) {
      return new HashSet<>();
    }
    Set<Pos> moves = getAllDeltaMoves(board);
    // current under attack check
    if (!getMyKing().getAttackersFromBoard(board, getMyKing().getPos()).isEmpty()) {
      moves.retainAll(getMyKing().getClearCheckPos(board));
    }

    // after move under attack check
    return moves.stream()
        .filter(pos -> myKing.getAttackersFromBoard(simulateMove(board, pos), myKing.getPos()).isEmpty())
        .collect(Collectors.toSet());
  }

  public Board simulateMove(Board board, Pos target) {
    Board copy = board.copy();
    Piece piece = this.copy();
    copy.setPiece(piece.getPos(), piece);
    piece.move(copy, target, true);
    return copy;
  }

  /**
   * Get all moves according to the move rule of this piece and current board status. It can move
   * that position, if that position is empty or enemy occupied and delta is valid.
   *
   * @param board
   * @return
   */
  public Set<Pos> getAllDeltaMoves(Board board) {
    Set<Pos> moves = new HashSet<>();
    for (Pos delta : moveDeltas) {
      moves.addAll(getMovesUntilOccupied(getPos(), delta, board));
    }
    return moves;
  }

  /**
   * Given the target piece, to check whether this piece able to attack it.
   *
   * @param board
   * @param pos
   * @return
   */
  public boolean isAbleToAttack(Board board, Pos pos) {
    return !getColor().equals(board.getPiece(pos).getColor()) && getAllDeltaMoves(board).contains(pos);
  }

  public boolean isNeverMoved() {
    return neverMoved;
  }

  /**
   * Move piece to the target position and set the original position as empty
   *
   * @param board
   * @param target
   * @param isSimulate
   */
  public void move(Board board, Pos target, boolean isSimulate) {
    neverMoved = false;

    Piece attackTarget = null;
    if (board.getPiece(target) != null) {
      attackTarget = board.getPiece(target);
    }
    board.setPiece(getPos(), null);
    board.setPiece(target, this);
    if (!isSimulate) {
      trace.add(new Step(getPos(), target, this, attackTarget));
    }
    pos = target; //update current position
    //if the opposite player cannot move any piece.
    if (!isSimulate && board.getCurrentActivePieces().stream().filter(p -> !p.getColor().equals(getColor())).noneMatch(p-> p.getValidMoves(board).size() > 0)) {
      System.out.println("--------------------------------");
      System.out.println(getColor() + " wins the game.");
      System.out.println("--------------------------------");
      ended = true;
    }
  }

  public Set<Pos> getMovesUntilOccupied(Pos start, Pos delta, Board board) {
    Set<Pos> moves = new HashSet<>();
    for (Pos move = start.moveDelta(delta); move != null; move = move.moveDelta(delta)) {
      if (isEmptyOrEnemy(board, move)) {
        moves.add(move);
      }
      if (Piece.isOccupied(move, board)) {
        break;
      }
    }
    return moves;
  }

  public String getId() {
    return id;
  }

  protected void setId(String id) {
    this.id = id;
  }

  public String getColor() {
    return color;
  }

  public String getOpositeColor() {
    return color.equals("B") ? "W" : "B";
  }

  public Pos getPos() {
    return pos;
  }

  public void setPos(Pos pos) {
    this.pos = pos;
  }

  public boolean isPawn() {
    return id.equalsIgnoreCase("p");
  }

  public boolean isQueen() {
    return id.equalsIgnoreCase("q");
  }

  public boolean isBishop() {
    return id.equalsIgnoreCase("b");
  }

  public boolean isRook() {
    return id.equalsIgnoreCase("r");
  }

  public boolean isKnight() {
    return id.equalsIgnoreCase("n");
  }

  public boolean isKing() {
    return id.equalsIgnoreCase("k");
  }

  public King getMyKing() {
    return myKing;
  }

  public void setMyKing(King king) {
    this.myKing = king;
  }

  @Override
  public String toString() {
    return getId();
  }
}
