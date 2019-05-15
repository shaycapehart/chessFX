package model;

import java.util.Arrays;
import java.util.HashSet;

public class Rook extends Piece {
  public Rook(String color, Pos pos) {
    super(color, pos);
    setId(getColor().equals("W") ? "R" : "r");
    moveDeltas =
        new HashSet<>(Arrays.asList(new Pos(1, 0), new Pos(-1, 0), new Pos(0, 1), new Pos(0, -1)));
  }

  @Override
  public Piece copy() {
    return new Rook(getColor(), getPos().copy());
  }
}
