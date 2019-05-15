package model;

import controller.Controller;
import controller.ViewState;

import java.util.*;
import java.util.stream.Collectors;

public class modelTest {
  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      Controller controller = new Controller();
      System.out.println("==============================");
      simulateSteps(500, controller);
    }
  }

  public static void simulateClick(Controller controller) {
//    controller.getChessBoard().printBoard();
    Scanner input = new Scanner(System.in);
    while (input.hasNext()) {
      String coordinate = input.next();
      if (getPos(coordinate) != null) {
        ViewState state = controller.clickHandler(getPos(coordinate));
        controller.getChessBoard().printViewState(state);
      } else {
        System.out.println("Input format not correct, should be x,y.");
      }
    }
  }

  private static Pos getPos(String input) {
    String[] coordinate = input.replace(" ", "").split(",");
    if (coordinate.length == 2
        && Board.isValidPosition(Integer.valueOf(coordinate[0]), Integer.valueOf(coordinate[1]))) {
      return new Pos(Integer.valueOf(coordinate[0]), Integer.valueOf(coordinate[1]));
    }
    return null;
  }

  private static void simulateSteps(int n, Controller controller) {
    Random rand = new Random();
    ViewState state;
    Piece.setEnded(false);
    for (int i = 0; i < n && !Piece.ended; i++) {
      List<Piece> pieces =
          controller.getChessBoard().getBoard().getCurrentActivePieces().stream()
              .filter(p -> p.getColor().equals(Piece.getWhoseTurn()))
              .collect(Collectors.toList());
      Piece randomPiece;
      int repeat = 0;
      for (randomPiece = pieces.get(rand.nextInt(pieces.size()));
          randomPiece.getValidMoves(controller.getChessBoard().getBoard()).isEmpty();
          randomPiece = pieces.get(rand.nextInt(pieces.size()))) {
        repeat++;
        if (repeat > 50) {
          break;
        }
      }
      Optional<Piece> validPiece =
          pieces.stream()
              .filter(p -> !p.getValidMoves(controller.getChessBoard().getBoard()).isEmpty())
              .findAny();
      if (!validPiece.isPresent()) {
        System.out.println("Could not find valid moves");
        System.out.println(pieces.iterator().next().getOpositeColor() + " wins the game.");
        Piece.setEnded(true);
        break;
      }
      randomPiece = (repeat > 50 ? validPiece.get() : randomPiece);
      state = controller.clickHandler(randomPiece.getPos());
      controller.getChessBoard().printViewState(state);
      List<Pos> moves =
          new ArrayList<>(randomPiece.getValidMoves(controller.getChessBoard().getBoard()));
      state = controller.clickHandler(moves.get(rand.nextInt(moves.size())));
      controller.getChessBoard().printViewState(state);
    }
  }
}
