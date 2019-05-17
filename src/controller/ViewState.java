package controller;

import model.Pos;
import model.Step;

import java.util.Set;

public class ViewState {
  private final Set<Pos> possibleMoves;
  private final String whoseTurn;
  private final int totalRound;
  private final Set<ViewPiece> pieces;
  private Step currentMove;

  public ViewState(Set<Pos> possibleMoves, String whoseTurn, int totalRound, Set<ViewPiece> pieces) {
    this.possibleMoves = possibleMoves;
    this.whoseTurn = whoseTurn;
    this.totalRound = totalRound;
    this.pieces = pieces;
  }

  public Set<Pos> getPossibleMoves() {
    return possibleMoves;
  }

  public String getWhoseTurn() {
    return whoseTurn;
  }

  public int getTotalRound() {
    return totalRound;
  }

  public Set<ViewPiece> getPieces() {
    return pieces;
  }

  public boolean setCurrentMove(Step step){
    this.currentMove = step;
    return step==null;
  }

  public Step getCurrentMove(){
    return currentMove;
  }
}
