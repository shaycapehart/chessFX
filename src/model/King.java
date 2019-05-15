package model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class King extends Piece {
  private Set<Pos> castlingPositions = new HashSet<>();

  public King(String color, Pos pos) {
    super(color, pos);
    setId(getColor().equals("W") ? "K" : "k"); // white is uppercase K, black is lower case k.
    moveDeltas =
        new HashSet<>(
            Arrays.asList(
                new Pos(1, 0),
                new Pos(-1, 0),
                new Pos(0, 1),
                new Pos(0, -1),
                new Pos(1, 1),
                new Pos(-1, -1),
                new Pos(-1, 1),
                new Pos(1, -1)));
  }

  @Override
  public Piece copy() {
    return new King(getColor(), getPos().copy());
  }

  /**
   * Get pieces who currently attack this king given the board status.s
   *
   * @param board
   * @return
   */
  public Set<Piece> getAttackersFromBoard(Board board, Pos target) {
    return board.getCurrentActivePieces().stream()
        .filter(p -> p.isAbleToAttack(board, target))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Pos> getAllDeltaMoves(Board board) {
    return moveDeltas.stream()
        .map(delta -> getPos().moveDelta(delta))
        .filter(pos -> pos != null && isEmptyOrEnemy(board, pos))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Pos> getValidMoves(Board board) {
    if (!getWhoseTurn().equals(getColor())) {
      return new HashSet<>();
    }
    Set<Pos> deltaMoves = getAllDeltaMoves(board);
    //remain the safe target position (king wont be checked)
    Set<Pos> validMoves =
        deltaMoves.stream()
            .filter(pos -> getAttackersFromBoard(simulateMove(board, pos), pos).isEmpty())
            .collect(Collectors.toSet());
    updateCastlingPositions(board);
    validMoves.addAll(castlingPositions);
    // If king under attack but doesn't have valid moves, then it fail
    return validMoves;
  }

  @Override
  public void move(Board board, Pos target, boolean isSimulate) {
    super.move(board, target, isSimulate);
    if (castlingPositions.contains(target)) {
      if (target.getCol() == 2) {
        Piece rook = board.getPiece(target.getRow(), 0);
        Pos pos = new Pos(target.getRow(), 3);
        board.setPiece(pos, rook);
        rook.setPos(pos);
        board.setPiece(target.getRow(), 0, null);
      } else {
        // col == 6
        Pos pos = new Pos(target.getRow(), 5);
        Piece rook = board.getPiece(target.getRow(), 7);
        board.setPiece(pos, rook);
        rook.setPos(pos);
        board.setPiece(target.getRow(), 7, null);
      }
    }
  }

  /**
   * occupy any pos in this list can save myKing. This is not for king move itself.
   *
   * @param board
   * @return
   */
  public Set<Pos> getClearCheckPos(Board board) {
    Set<Pos> positions = new HashSet<>();
    Set<Piece> attackers = getAttackersFromBoard(board, getPos());
    if (attackers.size() == 1) {
      Piece attacker = attackers.iterator().next(); //???
      Pos delta;
      if ("qbr".contains(attacker.getId().toLowerCase())) {
        int deltaX = Integer.compare(attacker.getPos().getRow(), getPos().getRow());
        int deltaY = Integer.compare(attacker.getPos().getCol(), getPos().getCol());
        delta = new Pos(deltaX, deltaY);
        positions.addAll(getMovesUntilOccupied(getPos(), delta, board));
      }
      // k, n, p only has its position as clear position
      positions.add(attacker.getPos());
    }
    return positions;
  }

  public void updateCastlingPositions(Board board) {
    Set<Pos> positions = new HashSet<>();
    Piece rightRook = board.getPiece(getPos().getRow(), getPos().getCol() + 3);
    Piece leftRook = board.getPiece(getPos().getRow(), getPos().getCol() - 4);
    if (!isNeverMoved()) {
      return;
    }
    if (isNeverMoved()
        && rightRook != null
        && rightRook.isNeverMoved()
        && checkBetweenEmpty(board, getPos(), rightRook.getPos())) {
      // make sure neither current position or all potential target positions are under attack check
      if (Arrays.asList(getPos(), getPos().moveDelta(0, 1), getPos().moveDelta(0, 2)).stream()
          .filter(p -> getAttackersFromBoard(simulateMove(board, p), p).size() > 0)
          .collect(Collectors.toSet()) //remains positions that have attackers.
          .isEmpty()) {
        positions.add(getPos().moveDelta(0, 2));
      }
    }

    if (isNeverMoved()
        && leftRook != null
        && leftRook.isNeverMoved()
        && checkBetweenEmpty(board, getPos(), leftRook.getPos())) {
      //cannot castling if any position under attack check
      if (Arrays.asList(
              getPos(),
              getPos().moveDelta(0, -1),
              getPos().moveDelta(0, -2),
              getPos().moveDelta(0, -3))
          .stream()
          .filter(p -> getAttackersFromBoard(simulateMove(board, p), p).size() > 0)
          .collect(Collectors.toSet())
          .isEmpty()) {
        positions.add(getPos().moveDelta(0, -3));
      }
    }
    castlingPositions = positions;
    if (castlingPositions.size() > 0) {
      System.out.println("***************************");
      System.out.println("Castling is available. " + castlingPositions);
    }
  }

  private boolean checkBetweenEmpty(Board board, Pos kingPos, Pos rookPos) {
    int moveDirection = kingPos.getCol() > rookPos.getCol() ? -1 : 1;
    for (int col = kingPos.getCol() + moveDirection;
        moveDirection > 0 ? col < rookPos.getCol() : col > rookPos.getCol();
        col = col + moveDirection) {
      if (board.getPiece(kingPos.getRow(), col) != null) {
        return false;
      }
    }
    return true;
  }
}
