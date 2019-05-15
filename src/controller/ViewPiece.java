package controller;

import model.Pos;

public class ViewPiece {
  private final Pos pos;
  private final String id;

  public ViewPiece(Pos pos, String id) {
    this.pos = pos;
    this.id = id;
  }

  public Pos getPos() {
    return pos;
  }

  public String getId() {
    return id;
  }
}
