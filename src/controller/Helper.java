package controller;

import model.Board;
import model.Piece;
import model.Pos;

public class Helper {
    public static Board getBoardAfterRemove(Pos removed, Board board) {
        Board temp = board.copy();
        temp.setPiece(removed, null);
        return temp;
    }

    /**s
     * Still Working On It.
     * Like fen 4kb1r/p2rqppp/5n2/1B2p1B1/4P3/1Q6/PPP2PPP/2K4R w k - 0 14
     * @return
     */
    public static String convertToFen(Piece[][] state){
        String fen = "";
        for(int r = 0; r < 8; r++){
            int emptySquare = 0;
            for(int c = 0; c < 8; c++){
                Piece piece = state[r][c];
                if(piece != null){
                    if(emptySquare != 0){//this row has piece before this position,
                        fen += emptySquare;
                        emptySquare = 0;
                    }
                    fen += piece.toString();
                }else{ //if piece is null
                    emptySquare++;
                }
                if(c == 7){
                    if(emptySquare!= 0) {
                        fen += emptySquare;
                    }
                    fen += "/";
                }
            }
        }
        return fen;
    }
}

