package model;

import java.util.Arrays;
import java.util.HashSet;

public class Bishop extends Piece {
  public Bishop(String color, Pos pos) {
    super(color, pos);
    setId(getColor().equals("W") ? "B" : "b"); // white is uppercase B, black is b.)
    moveDeltas =
        new HashSet<>(
            Arrays.asList(new Pos(1, 1), new Pos(-1, -1), new Pos(-1, 1), new Pos(1, -1)));
  }

  @Override
  public Piece copy() {
    return new Bishop(getColor(), getPos().copy());
  }
}
