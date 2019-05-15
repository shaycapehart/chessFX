package model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Knight extends Piece {

  public Knight(String color, Pos pos) {
    super(color, pos);
    setId(getColor().equals("W") ? "N" : "n");
    moveDeltas =
        new HashSet<>(
            Arrays.asList(
                new Pos(2, 1),
                new Pos(2, -1),
                new Pos(-2, 1),
                new Pos(-2, -1),
                new Pos(1, 2),
                new Pos(-1, 2),
                new Pos(1, -2),
                new Pos(-1, -2)));
  }

  @Override
  public Set<Pos> getAllDeltaMoves(Board board) {
    return moveDeltas.stream()
        .map(delta -> getPos().moveDelta(delta))
        .filter(pos -> pos != null && isEmptyOrEnemy(board, pos))
        .collect(Collectors.toSet());
  }

  @Override
  public Piece copy() {
    return new Knight(getColor(), getPos().copy());
  }
}
