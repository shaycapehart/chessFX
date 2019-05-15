package controller;

import model.Piece;
import model.Pos;

import java.util.Set;
import java.util.stream.Collectors;

public class Controller {
  private ChessBoard chessBoard;

  public Controller() {
    chessBoard = new ChessBoard();
  }

  public ChessBoard getChessBoard() {
    return chessBoard;
  }

  public ViewState clickHandler(Pos clickPosition) {
    //if possibleMoves is empty and not piece in the chosen square, do nothing????
    //if possibleMoves is empty and a piece is chosen, add the piece's validMoves
    if (chessBoard.getCurrentPossibleMoves().isEmpty()) {
      if (chessBoard.getBoard().getPiece(clickPosition) != null) {
        chessBoard
            .getCurrentPossibleMoves()
            .addAll(
                chessBoard.getBoard().getPiece(clickPosition).getValidMoves(chessBoard.getBoard()));
        chessBoard.setSelectPosition(clickPosition);//mark the clicked position
      }
    } else {
      //      //if possibleMoves is not empty, and clickPosition is in the possibleMoves,
      //      // move the piece that be marked selectionPosition during last clicking.
      if (chessBoard.getCurrentPossibleMoves().contains(clickPosition)) {
        chessBoard
            .getBoard()
            .getPiece(chessBoard.getSelectPosition())
            .move(chessBoard.getBoard(), clickPosition, false);
        chessBoard.getCurrentPossibleMoves().clear();
        chessBoard.setSelectPosition(null);
      } else {
        // if possibleMoves is not empty but clickPosition is not in the possibleMoves, clear
        // possible moves
        // if the chose square has a piece, shows the piece's possibleMoves
        chessBoard.getCurrentPossibleMoves().clear();
        if (chessBoard.getBoard().getPiece(clickPosition) != null) {
          chessBoard
              .getCurrentPossibleMoves()
              .addAll(
                  chessBoard
                      .getBoard()
                      .getPiece(clickPosition)
                      .getValidMoves(chessBoard.getBoard()));
          chessBoard.setSelectPosition(clickPosition); // mark the clicked position
        }
      }
      }
    return new ViewState(
        chessBoard.getCurrentPossibleMoves(),
        Piece.getWhoseTurn(),
        Piece.getTotalRound(),
        getViewPieces());
  }

  public Set<ViewPiece> getViewPieces() {
    return chessBoard.getBoard().getCurrentActivePieces().stream()
        .map(p -> new ViewPiece(p.getPos(), p.getId()))
        .collect(Collectors.toSet());
  }
}
