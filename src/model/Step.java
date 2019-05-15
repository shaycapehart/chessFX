package model;

public class Step {
  private final Pos from;
  private final Pos to;
  private final Piece piece;
  private Piece attacked;

  public void setAttacked(Piece attacked) {
    this.attacked = attacked;
  }

  public Step(Pos from, Pos to, Piece piece, Piece attacked) {
    this.from = from;
    this.to = to;
    this.piece = piece;
    this.attacked = attacked;
  }

  public Piece getAttacked() {
    return attacked;
  }

  public Pos getFrom() {
    return from;
  }

  public Pos getTo() {
    return to;
  }

  public Piece getPiece() {
    return piece;
  }

}
