package model;

import java.util.HashSet;
import java.util.Set;

/** This is the Pawn Class inherited from the piece */
public class Pawn extends Piece {

  public Pawn(String color, Pos pos) {
    super(color, pos);
    setId(getColor().equals("W") ? "P" : "p"); // white is uppercase P, black is p.)
  }

  @Override
  public Piece copy() {
    return new Pawn(getColor(), getPos().copy());
  }

  @Override
  public Set<Pos> getAllDeltaMoves(Board board) {
    Set<Pos> moves = new HashSet<>();
    int direction = getColor().equals("W") ? -1 : 1;
    int initialRow = getColor().equals("W") ? 6 : 1;
    // Pawn move north/south
    Pos move = getPos().moveDelta(direction, 0);
    if (move != null && board.getPiece(move) == null) {
      moves.add(move);
    }

    // Pawn move two rows north/south
    move = getPos().moveDelta(direction * 2, 0);
    if (getPos().getRow() == initialRow
        && move != null
        && board.getPiece(move) == null
        && board.getPiece(getPos().moveDelta(direction, 0)) == null) {
      moves.add(move);
    }

    // Diagonal move
    move = getPos().moveDelta(direction, 1);
    if (move != null
        && board.getPiece(move) != null
        && !board.getPiece(move).getColor().equals(getColor())) {
      moves.add(move);
    }
    move = getPos().moveDelta(direction, -1);
    if (move != null
        && board.getPiece(move) != null
        && !board.getPiece(move).getColor().equals(getColor())) {
      moves.add(move);
    }

    // En passant,
    // 3 conditions: 1) last move is enemy pawn, 2) last move is two steps, 3) on the same row
    Piece lastPiece = getLastMovePiece();
    if (isEnPassantAvailable()) {
      moves.add(
          new Pos(
              getPos().getRow() + direction, lastPiece.getPos().getCol()));
    }
    return moves;
  }

  private boolean isPromotionAvailable() {
    int endRow = getColor().equals("W") ? 0 : 7;
    boolean available = (getPos().getRow() == endRow);
    return available;
  }

  /**
   * Last move is enemy pawn's first move and it's two rows move. The enemy pawn is now on the same
   * row and adjacent col with current pawn
   *
   * @return
   */
  public boolean isEnPassantAvailable() {
    Piece lastPiece = getLastMovePiece();
    Step lastStep = getLastStep();
    boolean available =
        lastPiece != null
            && !lastPiece.getColor().equals(getColor())
            && lastPiece.isPawn()
            && Math.abs(lastStep.getFrom().getRow() - lastStep.getTo().getRow()) == 2
            && getPos().getRow() == lastPiece.getPos().getRow()
            && Math.abs(getPos().getCol() - lastPiece.getPos().getCol()) == 1;
    return available;
  }

  @Override
  public void move(Board board, Pos target, boolean isSimulate) {
    Piece attacked = null;
    // this target move is to eat the enPassant
    if (isEnPassantAvailable() && target.getCol() == getLastStep().getTo().getCol()) {
      if (!isSimulate) {
        System.out.println("***************************");
        System.out.println("Pawn eat the en passant with its position " + getPos());
      }
      attacked = getLastMovePiece();
    }
    super.move(board, target, isSimulate);
    // Promote the paw as queen
    if (isPromotionAvailable()) {
      if (!isSimulate) {
        System.out.println("***************************");
        System.out.println("Promote paw as queen at " + getPos());
      }
      Piece promotedQueen = new Queen(getColor(), target);
      promotedQueen.setMyKing(getMyKing());
      board.setPiece(target, promotedQueen);
    }
    if (attacked != null) {
      board.setPiece(attacked.getPos(), null);
      if (!isSimulate) {
        trace.get(trace.size() - 1).setAttacked(attacked);
      }
    }
  }
}
