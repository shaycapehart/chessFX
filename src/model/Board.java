package model;

import java.util.HashSet;
import java.util.Set;

public class Board {
  private Piece[][] squares;

  public Board(Piece[][] squares) {
    this.squares = squares;
  }

  public static boolean isValidPosition(int x, int y) {
    return x >= 0 && x < 8 && y >= 0 && y < 8;
  }

  public static boolean isValidPosition(Pos pos) {
    return pos != null && isValidPosition(pos.getRow(), pos.getCol());
  }

  public Piece getPiece(int x, int y) {
    if (isValidPosition(x, y)) {
      return squares[x][y];
    }
    return null;
  }

  public void setPiece(int x, int y, Piece piece) {
    squares[x][y] = piece;
  }

  public void setPiece(Pos pos, Piece piece) {
    squares[pos.getRow()][pos.getCol()] = piece;
  }

  public Piece getPiece(Pos pos) {
    if (isValidPosition(pos)) {
      return squares[pos.getRow()][pos.getCol()];
    }
    return null;
  }

  /**
   * Copy the board but share the same piece objects
   *
   * @return
   */
  public Board copy() {
    Piece[][] temp = new Piece[8][8];
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        temp[row][col] = squares[row][col];
      }
    }
    return new Board(temp);
  }

  public Set<Piece> getCurrentActivePieces() {
    Set<Piece> pieces = new HashSet<>();
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        if (squares[row][col] != null) {
          pieces.add(squares[row][col]);
        }
      }
    }
    return pieces;
  }
}
