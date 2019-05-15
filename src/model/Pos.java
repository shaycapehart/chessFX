package model;

public class Pos {
  private final int row;
  private final int col;

  public Pos(int r, int c) {
    this.row = r;
    this.col = c;
  }

  public static boolean isValid(int x, int y) {
    return x >= 0 && x <= 7 && y >= 0 && y <= 7;
  }

  public Pos moveDelta(int deltaX, int deltaY) {
    Pos pos = new Pos(row + deltaX, col + deltaY);
    if (pos.isValid()) {
      return pos;
    }
    return null;
  }

  public Pos moveDelta(Pos delta) {
    Pos pos = new Pos(row + delta.getRow(), col + delta.getCol());
    if (pos.isValid()) {
      return pos;
    }
    return null;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public boolean isValid() {
    return row >= 0 && row <= 7 && col >= 0 && col <= 7;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return toString().equals(obj.toString());
  }

  public Pos copy() {
    return new Pos(row, col);
  }


  @Override
  public String toString() {
    return "(" + row + ", " + col + ")";
  }
}
