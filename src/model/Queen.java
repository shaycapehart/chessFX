package model;

import java.util.Arrays;
import java.util.HashSet;

public class Queen extends Piece {

  public Queen(String color, Pos pos) {
    super(color, pos);
    setId(getColor().equals("W") ? "Q" : "q"); // white is uppercase P, black is p.)
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
    return new Queen(getColor(), getPos());
  }
}
