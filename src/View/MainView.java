package View;

import controller.Controller;
import controller.ViewPiece;
import controller.ViewState;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Xform;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Character.isUpperCase;

public class MainView extends Application {
    private Text whoseTurnText = new Text();
    private Text totalRoundText = new Text();
    private Square[][] squares = new Square[8][8];
    private SquareBox[][] squareBoxes = new SquareBox[8][8];
    private Integer[][] helper = new Integer[8][8];
    private Controller controller;
    private final GridPane chessBoardPane = new GridPane();
    private SubScene subScene;
    private final Xform world = new Xform();
    private final Xform axisGroup = new Xform();
    private final Xform boardGroup = new Xform();
    private Group pieceGroup = new Group();
    private Group centerGroup = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();

    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;
    private static final double CAMERA_INITIAL_DISTANCE = -500;
    private static final double CAMERA_INITIAL_X_ANGLE = 45.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 0.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 250.0;
    private static final double SQUARE_SIDE = 30.0;
    private Image lightPiece, darkPiece;

    // Used for UI control with the mouse
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private GameMenu gameMenu;
    private Image star, lightS, darkS;


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ChessFX");

        lightPiece = new Image(getClass().getResource("/images/LightPiece.jpg").toString());
        darkPiece = new Image(getClass().getResource("/images/DarkPiece.jpg").toString());
        star = new Image(getClass().getResource("/images/ray.png").toString());
        lightS = new Image(getClass().getResource("/images/LightSquare.jpg").toString());
        darkS = new Image(getClass().getResource("/images/DarkSquare.jpg").toString());

        buildCamera();
        buildAxes();
        buildBoard();
        buildChessPieces();
        build2dBoard();

        Group root3D = new Group(world);
        root3D.setAutoSizeChildren(true);
        root3D.prefHeight(SCENE_HEIGHT*0.5);
        root3D.prefWidth(SCENE_WIDTH*0.5);
        subScene = new SubScene(root3D, SCENE_WIDTH*0.9, SCENE_HEIGHT*0.9, true,SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        BorderPane pane = new BorderPane();
        InputStream is = Files.newInputStream(Paths.get("res/images/chessSplash.jpg"));
        Image img = new Image(is);
        is.close();

        pane.setTop(createInfoBoard());
        Region region1 = new Region();

        HBox.setHgrow(region1, Priority.ALWAYS);

        HBox bottomPane = new HBox();
        bottomPane.getChildren().addAll(create3DPills(), region1, createAxisPills());
        pane.setBottom(bottomPane);

        pane.setBackground(new Background(new BackgroundImage(img,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(0, 0, false, false, false, true))));
        gameMenu = new GameMenu();

        centerGroup.getChildren().addAll(subScene,gameMenu);
        pane.setCenter(centerGroup);
        subScene.setVisible(false);
        Scene scene = new Scene(pane,SCENE_WIDTH, SCENE_HEIGHT);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (!gameMenu.isVisible()) {
                    FadeTransition ft = new FadeTransition(Duration.seconds(0.5), gameMenu);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    gameMenu.setVisible(true);
                    ft.play();
                }
                else {
                    FadeTransition ft = new FadeTransition(Duration.seconds(0.5), gameMenu);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(evt -> gameMenu.setVisible(false));
                    ft.play();
                }
            }
        });

        scene.setOnMousePressed((MouseEvent me) -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            cameraXform.ry.setAngle(cameraXform.ry.getAngle() -(mousePosX - mouseOldX));
            cameraXform.rx.setAngle(cameraXform.rx.getAngle() -(mousePosY - mouseOldY));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });

        gameMenu.setVisible(true);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void newGame(){

        resetPieces();
        controller = new Controller();
        drawBoard(controller.clickHandler(new model.Pos(4,4)));
        subScene.setVisible(true);
    }

    private HBox createAxisPills() {
        ToggleButton tb1 = new ToggleButton("AXIS ON");
        tb1.setPrefSize(76, 30);
        tb1.getStyleClass().add("left-pill");
        ToggleButton tb2 = new ToggleButton("AXIS OFF");
        tb2.setPrefSize(76, 30);
        tb2.getStyleClass().add("right-pill");

        final ToggleGroup group = new ToggleGroup();
        tb1.setToggleGroup(group);
        tb2.setToggleGroup(group);
        // select the first button to start with
        group.selectToggle(tb2);

        final ChangeListener<Toggle> listener =
                (ObservableValue<? extends Toggle> observable,
                 Toggle old, Toggle now) -> {
                    if (now == null) {
                        group.selectToggle(old);
                    }else{
                        String id = ((ToggleButton)now).getText();
                        if(id.equals("AXIS ON")){
                            axisGroup.setVisible(true);
                        }else{
                            axisGroup.setVisible(false);
                        }
                    }
                };
        group.selectedToggleProperty().addListener(listener);

        final String pillButtonCss =
                getClass().getResource("/css/PillButton.css").toExternalForm();
        final HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(tb1, tb2);
        hBox.getStylesheets().add(pillButtonCss);

        return hBox;
    }

    private HBox createInfoBoard() {
        HBox whoTurn = new HBox(50);
        whoTurn.getStyleClass().add("left-pill");
        whoTurn.setOpacity(0.6);
        whoTurn.getChildren().add(whoseTurnText);

        HBox totalRound = new HBox(50);
        totalRound.getStyleClass().add("left-pill");
        totalRound.getChildren().add(totalRoundText);

        final String pillButtonCss =
                getClass().getResource("/css/PillButton.css").toExternalForm();
        final HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(whoTurn, totalRound);
        hBox.getStylesheets().add(pillButtonCss);

        return hBox;
    }

    private HBox create3DPills() {
        ToggleButton tb1 = new ToggleButton("2-D");
        tb1.setPrefSize(76, 30);
        tb1.getStyleClass().add("left-pill");
        ToggleButton tb2 = new ToggleButton("3-D");
        tb2.setPrefSize(76, 30);
        tb2.getStyleClass().add("right-pill");

        final ToggleGroup group = new ToggleGroup();
        tb1.setToggleGroup(group);
        tb2.setToggleGroup(group);
        // select the first button to start with
        group.selectToggle(tb2);

        final ChangeListener<Toggle> listener =
                (ObservableValue<? extends Toggle> observable,
                 Toggle old, Toggle now) -> {
                    if (now == null) {
                        group.selectToggle(old);
                    }else{
                        String id = ((ToggleButton)now).getText();
                        centerGroup.getChildren().clear();
                        centerGroup.getChildren().add(id.equals("3-D")?subScene:chessBoardPane);
                        centerGroup.getChildren().add(gameMenu);
                    }
                };
        group.selectedToggleProperty().addListener(listener);

        final String pillButtonCss =
                getClass().getResource("/css/PillButton.css").toExternalForm();
        final HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(tb1, tb2);
        hBox.getStylesheets().add(pillButtonCss);

        return hBox;
    }

    private class GameMenu extends Group {
        GameMenu() {
            VBox menu0 = new VBox(10);
            VBox menu1 = new VBox(10);
            VBox menu2 = new VBox(10);
            MenuButton btnNew,btnResume,btnOptions,btnExit,btnBack;
            menu0.setTranslateX(100);
            menu0.setTranslateY(200);
            menu1.setTranslateX(100);
            menu1.setTranslateY(200);
            menu2.setTranslateX(100);
            menu2.setTranslateY(200);

            btnResume = new MenuButton("RESUME");
            btnResume.setOnMouseClicked(event -> {
                FadeTransition ft = new FadeTransition(Duration.seconds(0.5), this);
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(evt -> setVisible(false));
                ft.play();
            });

            btnNew = new MenuButton("NEW GAME");
            btnNew.setOnMouseClicked(event -> {
                FadeTransition ft = new FadeTransition(Duration.seconds(0.5), this);
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(evt -> setVisible(false));
                ft.play();
                newGame();
                btnResume.setVisible(true);
            });

            btnOptions = new MenuButton("OPTIONS");
            btnOptions.setOnMouseClicked(event -> {
                getChildren().add(menu1);
                FadeTransition ft = new FadeTransition(Duration.seconds(0.5), menu0);
                ft.setFromValue(1);
                ft.setToValue(0);
                FadeTransition ft1 = new FadeTransition(Duration.seconds(0.5), menu1);
                ft1.setFromValue(0);
                ft1.setToValue(1);
                ft.play();
                ft1.play();
                getChildren().remove(menu0);
            });

            btnExit = new MenuButton("EXIT");
            btnExit.setOnMouseClicked(event -> System.exit(0));

            btnBack = new MenuButton("BACK");
            btnBack.setOnMouseClicked(event -> {
                getChildren().add(menu0);
                FadeTransition ft = new FadeTransition(Duration.seconds(0.5), menu1);
                ft.setFromValue(1);
                ft.setToValue(0);
                FadeTransition ft1 = new FadeTransition(Duration.seconds(0.5), menu0);
                ft1.setFromValue(0);
                ft1.setToValue(1);
                ft.play();
                ft1.play();
                getChildren().remove(menu1);
            });

            MenuButton btnSound = new MenuButton("SOUND");
            MenuButton btnVideo = new MenuButton("VIDEO");


            menu0.getChildren().addAll(btnResume, btnNew, btnOptions, btnExit);
            menu1.getChildren().addAll(btnBack, btnSound, btnVideo);

            Rectangle bg = new Rectangle(500, 500);
            bg.setFill(Color.GREY);
            bg.setOpacity(0.4);

            getChildren().add(menu0);
            btnResume.setVisible(false);
        }
    }

    private static class MenuButton extends StackPane {
        private Text text;

        MenuButton(String name) {
            text = new Text(name);
            text.setFont(Font.font(20));
            text.setFill(Color.WHITE);

            Rectangle bg = new Rectangle(250, 30);
            bg.setOpacity(0.6);
            bg.setFill(Color.BLACK);
            bg.setEffect(new GaussianBlur(3.5));

            setAlignment(Pos.CENTER_LEFT);
            setRotate(-0.5);
            getChildren().addAll(bg, text);

            setOnMouseEntered(event -> {
                bg.setFill(Color.WHITE);
                text.setFill(Color.BLACK);
            });

            setOnMouseExited(event -> {
                bg.setFill(Color.BLACK);
                text.setFill(Color.WHITE);
            });

            DropShadow drop = new DropShadow(50, Color.WHITE);
            drop.setInput(new Glow());

            setOnMousePressed(event -> setEffect(drop));
            setOnMouseReleased(event -> setEffect(null));
        }
    }

    /** Makes the "star" squares invisible for each square. */
    private void clearBoard() {
        boolean whiteSquare = true;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j< 8; j++){
                squareBoxes[i][j].getChildren().get(1).setVisible(false);
                squares[i][j].getChildren().get(1).setVisible(false);
                squares[i][j].getChildren().get(2).setVisible(false);
                if(j != 7){
                    whiteSquare = (!whiteSquare);
                }
            }
        }
    }

    private void movePieceAnimation(Node square, Node piece) {
        Bounds pb = piece.getBoundsInParent();
        Bounds sb = square.getBoundsInParent();
        double x = (sb.getMinX() - pb.getMinX() + (sb.getWidth() - pb.getWidth()) / 2);
        double y = (sb.getMinY() - pb.getMinY() + (sb.getWidth() - pb.getWidth()) / 2);
        Duration duration = Duration.millis(500);
        TranslateTransition t1 = new TranslateTransition(duration, piece);
        t1.setByZ(30);
        t1.setCycleCount(1);
        TranslateTransition t = new TranslateTransition(duration, piece);
        t.setByX(x);
        t.setByY(y);
        t.setCycleCount(1);
        TranslateTransition t2 = new TranslateTransition(duration, piece);
        t2.setByZ(-30);
        t2.setCycleCount(1);
        SequentialTransition s = new SequentialTransition(t1, t, t2);
        s.play();
    }

    private void removePieceAnimation(Node piece) {
        Bounds pb = piece.getBoundsInParent();
        Duration duration = Duration.millis(500);
        TranslateTransition transition = new TranslateTransition(duration, piece);
        transition.setByZ(-pb.getDepth());
        transition.setCycleCount(1);
        transition.play();
    }

    private void drawBoard(ViewState viewState){
        clearBoard();
        for (model.Pos pos: viewState.getPossibleMoves()) {
            squareBoxes[pos.getRow()][pos.getCol()].getChildren().get(1).setVisible(true);
            squares[pos.getRow()][pos.getCol()].getChildren().get(1).setVisible(true);
        }
        if(viewState.getCurrentMove()!=null){
            int fromRow = viewState.getCurrentMove().getFrom().getRow();
            int fromCol = viewState.getCurrentMove().getFrom().getCol();
            int toRow = viewState.getCurrentMove().getTo().getRow();
            int toCol = viewState.getCurrentMove().getTo().getCol();
            int fP = helper[fromRow][fromCol];
            if (helper[toRow][toCol]!=null){
                int tP = helper[toRow][toCol];
                removePieceAnimation(pieceGroup.getChildren().get(tP));
//                pieceGroup.getChildren().get(tP).setVisible(false);
            }
            movePieceAnimation(squareBoxes[toRow][toCol],pieceGroup.getChildren().get(fP));
//            pieceGroup.getChildren().get(fP).setTranslateX(pieceGroup.getChildren().get(fP).getTranslateX()+(toRow-fromRow)*SQUARE_SIDE);
//            pieceGroup.getChildren().get(fP).setTranslateY(pieceGroup.getChildren().get(fP).getTranslateY()+(toCol-fromCol)*SQUARE_SIDE);
            helper[fromRow][fromCol]=null;
            helper[toRow][toCol]=fP;
        }
        for(ViewPiece viewPiece: viewState.getPieces()){
            squares[viewPiece.getPos().getRow()][viewPiece.getPos().getCol()].drawPiece(viewPiece.getId());
        }
        if ("WB".contains(viewState.getWhoseTurn())) {
            whoseTurnText.setText("Whose Turn: "+viewState.getWhoseTurn());
        } else {
            whoseTurnText.setText(viewState.getWhoseTurn());
        }
        totalRoundText.setText("Total round: "+ viewState.getTotalRound());
    }

    /** Resets the 3D pieces when a new game begins. */
    private void resetPieces(){
        helper = new Integer[8][8];
        for (Node n : pieceGroup.getChildren()){
            n.relocate(0.0,0.0);
            n.setTranslateX(-n.getBoundsInLocal().getWidth()/2.0);
            n.setTranslateY(-n.getBoundsInLocal().getHeight()/2.0);
            n.setTranslateZ(-n.getBoundsInLocal().getDepth()/2.0-n.getBoundsInLocal().getMinZ());
            n.setTranslateZ(n.getTranslateZ()+n.getBoundsInParent().getMaxZ());
            n.setMouseTransparent(true);
            int row = Character.getNumericValue(n.getId().charAt(1));
            int col = Character.getNumericValue(n.getId().charAt(2));
            n.setTranslateX(n.getTranslateX()+(-3.5 + row)*SQUARE_SIDE);
            n.setTranslateY(n.getTranslateY()+(-3.5+ col)*SQUARE_SIDE);
            helper[row][col]=pieceGroup.getChildren().indexOf(n);
            n.setVisible(true);
        }
    }

    /** Perform the translation and rotation on the camera to change its default location. */
    private void buildCamera() {
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateX(180.0);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    /** Build the 3-axis for the 3-D scene */
    private void buildAxes() {
        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);
        xAxis.setMaterial(new PhongMaterial(Color.DARKRED));
        yAxis.setMaterial(new PhongMaterial(Color.DARKGREEN));
        zAxis.setMaterial(new PhongMaterial(Color.DARKBLUE));
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(false);
        world.getChildren().addAll(axisGroup);
    }

    private void buildBoard(){
        boolean whiteSquare = true;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j< 8; j++){
                SquareBox squareBox = new SquareBox();
                squareBoxes[i][j] = squareBox;
                squareBox.setRowCol(i, j);
                squareBox.setColor(whiteSquare);
                if(j != 7){whiteSquare = (!whiteSquare);}
                squareBox.setTranslateX((-3.5+i)*SQUARE_SIDE);
                squareBox.setTranslateY((-3.5+j)*SQUARE_SIDE);
                boardGroup.getChildren().add(squareBox);
            }
        }
        world.getChildren().addAll(boardGroup);
    }

    private void buildChessPieces() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/Chess-1.fxml"));
        try {
            pieceGroup = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final PhongMaterial lightPieceMaterial = new PhongMaterial();
        lightPieceMaterial.setDiffuseMap(lightPiece);
        final PhongMaterial darkPieceMaterial = new PhongMaterial();
        darkPieceMaterial.setDiffuseMap(darkPiece);

        double piecesX = pieceGroup.getLayoutBounds().getWidth();
        double piecesY = pieceGroup.getLayoutBounds().getDepth();
        double pieceScale = (8.0*SQUARE_SIDE - 0.2*SQUARE_SIDE) / Math.max(piecesX, piecesY);
        int counter = 0;
        for (Node n : pieceGroup.getChildren()) {
            if (isUpperCase(n.getId().charAt(0))) {
                if (n.getClass() == MeshView.class) {
                    ((MeshView) n).setMaterial(lightPieceMaterial);
                } else if (n.getClass() == Group.class) {
                    setMaterial((Group) n, lightPieceMaterial);
                }
            } else {
                if (n.getClass() == MeshView.class) {
                    ((MeshView) n).setMaterial(darkPieceMaterial);
                } else if (n.getClass() == Group.class) {
                    setMaterial((Group) n, darkPieceMaterial);
                }
            }
            n.setRotate(270.0);
            n.setRotationAxis(new Point3D(1.0,0.0,0.0));
            n.relocate(0.0,0.0);
            n.setTranslateX(-n.getBoundsInLocal().getWidth()/2.0);
            n.setTranslateY(-n.getBoundsInLocal().getHeight()/2.0);
            n.setTranslateZ(-n.getBoundsInLocal().getDepth()/2.0-n.getBoundsInLocal().getMinZ());
            n.setScaleX(pieceScale);
            n.setScaleY(pieceScale);
            n.setScaleZ(pieceScale);
            n.setTranslateZ(n.getTranslateZ()+n.getBoundsInParent().getMaxZ());
            n.setMouseTransparent(true);
            int row = Character.getNumericValue(n.getId().charAt(1));
            int col = Character.getNumericValue(n.getId().charAt(2));
            n.setTranslateX(n.getTranslateX()+(-3.5 + row)*SQUARE_SIDE);
            n.setTranslateY(n.getTranslateY()+(-3.5+ col)*SQUARE_SIDE);
            helper[row][col]=counter;
            counter=counter+1;
        }
        world.getChildren().add(pieceGroup);
    }

    private void build2dBoard(){
        boolean whiteSquare = true;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j< 8; j++){
                Square square = new Square();
                squares[i][j] = square;
                square.setRowCol(i, j);
                square.setColor(whiteSquare);
                if(j != 7){
                    whiteSquare = (!whiteSquare);
                }
                //be careful: j and i are exchanged
                chessBoardPane.add(square, j, i);
            }
        }
    }

    private class SquareBox extends Group {
        private int row, col;
        private Box myColor = new Box(SQUARE_SIDE,SQUARE_SIDE,5);
        final PhongMaterial squareMaterial = new PhongMaterial();

        private SquareBox(){
            ImageView starView = new ImageView(star);
            starView.setFitHeight(SQUARE_SIDE*0.85);
            starView.setFitWidth(SQUARE_SIDE*0.85);
            starView.setTranslateX(-(SQUARE_SIDE*0.8)/2);
            starView.setTranslateY(-(SQUARE_SIDE*0.85)/2);
            starView.setTranslateZ(0.5);

            myColor.setTranslateZ(-2.5);
            setOnMouseClicked(e -> handleMouseClick());
            getChildren().addAll(myColor,starView);
            starView.setVisible(false);
        }

        void setRowCol(int row, int col){
            this.row = row;
            this.col = col;
        }

        void setColor(boolean bool){
            if(bool){
                squareMaterial.setDiffuseMap(lightS);
            }else{
                squareMaterial.setDiffuseMap(darkS);
            }
            myColor.setMaterial(squareMaterial);
        }

        void handleMouseClick(){
            drawBoard(controller.clickHandler(new model.Pos(row, col)));
        }
    }

    private class Square extends StackPane {
        private int row, col;
        private ImageView localPiece = new ImageView();
        private ImageView myColor = new ImageView();

        private Square(){
            setPrefSize(60, 60);
            setStyle("-fx-border-color: black");
            ImageView starView = new ImageView(star);
            starView.setFitHeight(50);
            starView.setFitWidth(50);
            myColor.setFitHeight(60);
            myColor.setFitWidth(60);
            localPiece.setFitHeight(60);
            localPiece.setFitWidth(60);
            setOnMouseClicked(e -> handleMouseClick());
            getChildren().addAll(myColor, starView,localPiece);
            starView.setVisible(false);
        }

        void setRowCol(int row, int col){
            this.row = row;
            this.col = col;
        }

        void setColor(boolean bool){
            if(bool){
                myColor.setImage(lightS);
            }else{
                myColor.setImage(darkS);
            }
        }
        private void handleMouseClick(){
            drawBoard(controller.clickHandler(new model.Pos(row, col)));
        }
        void drawPiece(String id){
            String fileName;
            if("KQNBPR".contains(id)) {
                fileName = "res/images/w" + id.toLowerCase() + ".png";
            }else{
                fileName = "res/images/" + id.toLowerCase() + ".png";
            }
            try {
                FileInputStream input = new FileInputStream(fileName);
                Image img = new Image(input);
                localPiece.setImage(img);
                localPiece.setVisible(true);
            } catch(Exception e) {
                e.printStackTrace();
            }
            this.setAlignment(javafx.geometry.Pos.CENTER);
        }
    }

    private void setMaterial(Group group, Material material) {
        for (Node node : group.getChildren()) {
            if (node.getClass() == MeshView.class) {
                ((MeshView) node).setMaterial(material);
            } else if (node.getClass() == Group.class) {
                setMaterial((Group) node, material);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
