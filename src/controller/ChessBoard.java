package controller;

import model.*;

import java.util.HashSet;
import java.util.Set;

/**
 * the checkBoard simulates a check board.getSquares() whose rows are from 0 to 7 and columns are
 * from 0 to 7.?????????
 */
public class ChessBoard {
  private Board board;
  private King k, K;

  private Set<Pos> currentPossibleMoves = new HashSet<>();
  private Pos selectPosition;

  public ChessBoard() {
    // initiate 32 pieces and fill the board.getSquares()
    board = new Board(new Piece[8][8]);
    Piece.getTrace().clear();
    initiateKings();
    initiateQueens();
    initiateBishops();
    initiatePawns();
    initiateKnights();
    initiateRooks();
  }

  public Pos getSelectPosition() {

    return selectPosition;
  }

  public void setSelectPosition(Pos selectPosition) {
    this.selectPosition = selectPosition;
  }

  public Set<Pos> getCurrentPossibleMoves() {
    return currentPossibleMoves;
  }

  public Board getBoard() {
    return board;
  }

  private void initiateKings() {
    Piece whiteKing = new King("W", new Pos(7, 4));
    board.setPiece(7, 4, whiteKing); // RNBQKBNR
    this.K = (King) whiteKing;
    Piece blackKing = new King("B", new Pos(0, 4));
    board.setPiece(0, 4, blackKing); // rnbqkbnr
    this.k = (King) blackKing;
  }

  private void initiatePawns() {
    // 8 black pawns on row[1]: ppppppp
    // 8 white pawns on row[6]: PPPPPPPP
    int row1 = 1, row6 = 6;
    for (int col = 0; col < 8; col++) {
      Pos blackPawnPos = new Pos(row1, col);
      Piece blackPawn = new Pawn("B", blackPawnPos);
      blackPawn.setMyKing(k);
      board.setPiece(row1, col, blackPawn);

      Pos whitePawnPos = new Pos(row6, col);
      Piece whitePawn = new Pawn("W", whitePawnPos);
      whitePawn.setMyKing(K);
      board.setPiece(row6, col, whitePawn);
    }
  }

  private void initiateQueens() {
    Piece whiteQueen = new Queen("W", new Pos(7, 3));
    board.setPiece(7, 3, whiteQueen); // RNBQKBNR
    whiteQueen.setMyKing(K);

    Piece blackQueen = new Queen("B", new Pos(0, 3));
    board.setPiece(0, 3, blackQueen); // rnbqkbnr
    blackQueen.setMyKing(k);
  }

  // TODO create piece with position
  private void initiateBishops() {
    Pos[] posList1 = {new Pos(7, 2), new Pos(7, 5)};
    for (Pos targetPos : posList1) {
      Piece whiteBishop = new Bishop("W", targetPos);
      whiteBishop.setMyKing(K);
      board.setPiece(targetPos, whiteBishop);
    }

    Pos[] posList2 = {new Pos(0, 2), new Pos(0, 5)};
    for (Pos targetPos : posList2) {
      Piece blackBishop = new Bishop("B", targetPos);
      blackBishop.setMyKing(k);
      board.setPiece(targetPos, blackBishop);
    }
  }

  private void initiateKnights() {
    Pos[] posList1 = {new Pos(7, 1), new Pos(7, 6)};
    for (Pos targetPos : posList1) {
      Piece whiteKnight = new Knight("W", targetPos);
      whiteKnight.setMyKing(K);
      board.setPiece(targetPos, whiteKnight);
    }

    Pos[] posList2 = {new Pos(0, 1), new Pos(0, 6)};
    for (Pos targetPos : posList2) {
      Piece blackKnight = new Knight("B", targetPos);
      blackKnight.setMyKing(k);
      board.setPiece(targetPos, blackKnight);
    }
  }

  private void initiateRooks() {
    Pos[] posList1 = {new Pos(7, 0), new Pos(7, 7)};
    for (Pos targetPos : posList1) {
      Piece whiteRook = new Rook("W", targetPos);
      whiteRook.setMyKing(K);
      board.setPiece(targetPos, whiteRook);
    }

    Pos[] posList2 = {new Pos(0, 0), new Pos(0, 7)};
    for (Pos targetPos : posList2) {
      Piece blackRook = new Rook("B", targetPos);
      blackRook.setMyKing(k);
      board.setPiece(targetPos, blackRook);
    }
  }

  /**
   * Still Working On It. Like fen 4kb1r/p2rqppp/5n2/1B2p1B1/4P3/1Q6/PPP2PPP/2K4R w k - 0 14
   *
   * @return
   */
  public String converToFen() {
    String fen = "";
    for (int r = 0; r < 8; r++) {
      int emptySquare = 0;
      for (int c = 0; c < 8; c++) {
        Piece piece = board.getPiece(r, c);
        if (piece != null) {
          if (emptySquare != 0) { // print empty squares before this square,
            fen += emptySquare;
            emptySquare = 0;
          }
          fen += piece.toString();
        } else { // if piece is null
          emptySquare++;
        }
        if (c == 7) {
          fen += "/";
        }
      }
    }
    return fen;
  }


  public void printViewState(ViewState viewState) {
//    printBoard();
    System.out.println(
        "Total round: " + viewState.getTotalRound() + "; whose turn: " + viewState.getWhoseTurn());
    if (Piece.getLastStep() != null) {
      System.out.println(
          "last step from " + Piece.getLastStep().getFrom() + " to " + Piece.getLastStep().getTo());
    }
  }
}
