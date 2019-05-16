package View;

import controller.Controller;
import controller.ViewPiece;
import controller.ViewState;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Character.isUpperCase;

public class MainView extends Application {
    private final GridPane chessBoardPane = new GridPane();
    private Text whoseTurnText = new Text();
    private Text totalRoundText = new Text();
    private Group centerGroup = new Group();
    private Square[][] squares = new Square[8][8]; //view
    private SquareBox[][] squareBoxes = new SquareBox[8][8];
    private Controller controller;

    private SubScene subScene;
    private final Xform world = new Xform();           //   |___world
    private final Xform axisGroup = new Xform();       //        |___axisGroup
    private final Xform boardGroup = new Xform();      //        |___boardGroup
    private Group pieceGroup = new Group();            //        |___pieceGroup

    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();

    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;
    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 45.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 0.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 250.0;
    private static final double SQUARE_SIDE = 30.0;
    private Image lightStar, darkStar;
    private Image lightSquare, darkSquare;
    private Image lightPiece, darkPiece;
    private Background lightStarBack, darkStarBack;
    private static boolean is3D = true;

    // Used for UI control with the mouse
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private GameMenu gameMenu;

    Stage window;


    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Beginner's Chess");

        lightSquare = new Image(getClass().getResource("/images/LightSquare.jpg").toString());
        darkSquare = new Image(getClass().getResource("/images/DarkSquare.jpg").toString());
        lightPiece = new Image(getClass().getResource("/images/LightPiece.jpg").toString());
        darkPiece = new Image(getClass().getResource("/images/DarkPiece.jpg").toString());
        lightStar = new Image(getClass().getResource("/images/lightstar.jpg").toString());
        darkStar = new Image(getClass().getResource("/images/darkstar.jpg").toString());
        lightStarBack =  new Background(new BackgroundImage(new Image(getClass().getResource("/images/lightstar.jpg").toString()),null,null,null,null));
        darkStarBack =  new Background(new BackgroundImage(new Image(getClass().getResource("/images/darkstar.jpg").toString()),null,null,null,null));

        buildCamera();
        buildAxes();
        buildBoard();
        buildChessPieces();
        build2dBoard();

        Group root3D = new Group(world);
        root3D.setAutoSizeChildren(true);
        root3D.prefHeight(SCENE_HEIGHT*0.5);
        root3D.prefWidth(SCENE_WIDTH*0.5);
        subScene = new SubScene(root3D, SCENE_WIDTH*0.8, SCENE_HEIGHT*0.8, true,SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        // testing menu
        BorderPane pane = new BorderPane();
        InputStream is = Files.newInputStream(Paths.get("res/images/chessSplash.jpg"));
        Image img = new Image(is);
        is.close();

        HBox whoTurn = new HBox(50);
        whoTurn.getChildren().add(whoseTurnText);
        HBox totalRound = new HBox(50);
        totalRound.getChildren().add(totalRoundText);
        HBox topPane = new HBox();
        topPane.getChildren().addAll(whoTurn,totalRound);

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
        gameMenu.setVisible(false);

        centerGroup.getChildren().addAll(subScene,gameMenu);
        pane.setCenter(centerGroup);

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

        newGame();

//        controller = new Controller();
//        drawBoard(controller.clickHandler(new model.Pos(4,4)));

        window.setScene(scene);
        window.show();
    }

    private void newGame(){

        controller = new Controller();
        drawBoard(controller.clickHandler(new model.Pos(4,4)));
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
        group.selectToggle(tb1);

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
                        centerGroup.getChildren().remove(id.equals("3-D")?chessBoardPane:subScene);
                        centerGroup.getChildren().add(id.equals("3-D")?subScene:chessBoardPane);
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
        public GameMenu() {
            VBox menu0 = new VBox(10);
            VBox menu1 = new VBox(10);
            menu0.setTranslateX(100);
            menu0.setTranslateY(200);
            menu1.setTranslateX(100);
            menu1.setTranslateY(200);

            MenuButton btnNew = new MenuButton("NEW GAME");
            btnNew.setOnMouseClicked(event -> {
                FadeTransition ft = new FadeTransition(Duration.seconds(0.5), this);
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(evt -> setVisible(false));
                ft.play();
                newGame();
            });

            MenuButton btnResume = new MenuButton("RESUME");
            btnResume.setOnMouseClicked(event -> {
                FadeTransition ft = new FadeTransition(Duration.seconds(0.5), this);
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(evt -> setVisible(false));
                ft.play();
            });

            MenuButton btnOptions = new MenuButton("OPTIONS");
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

            MenuButton btnExit = new MenuButton("EXIT");
            btnExit.setOnMouseClicked(event -> {
                System.exit(0);
            });

            MenuButton btnBack = new MenuButton("BACK");
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
            menu0.getChildren().addAll(btnNew, btnResume, btnOptions, btnExit);
            menu1.getChildren().addAll(btnBack, btnSound, btnVideo);

            Rectangle bg = new Rectangle(500, 500);
            bg.setFill(Color.GREY);
            bg.setOpacity(0.4);

            getChildren().add(menu0);
        }
    }

    private static class MenuButton extends StackPane {
        private Text text;

        public MenuButton(String name) {
            text = new Text(name);
            text.setFont(text.getFont().font(20));
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

    private void clearBoard() {
        boolean whiteSquare = true;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j< 8; j++){

                    squareBoxes[i][j].setColor(whiteSquare);

                    squares[i][j].setColor(whiteSquare);
                    squares[i][j].resetText("");

                if(j != 7){
                    whiteSquare = (!whiteSquare);
                }
            }
        }
        resetPieces();
    }

    private void drawBoard(ViewState viewState){
        clearBoard();
        final PhongMaterial newLightMaterial = new PhongMaterial();
        newLightMaterial.setDiffuseMap(lightStar);
        final PhongMaterial newDarkMaterial = new PhongMaterial();
        newDarkMaterial.setDiffuseMap(darkStar);
        for (model.Pos pos: viewState.getPossibleMoves()) {

                if(squareBoxes[pos.getRow()][pos.getCol()].getColor()){
                    squareBoxes[pos.getRow()][pos.getCol()].setMaterial(newLightMaterial);;
                }else {
                    squareBoxes[pos.getRow()][pos.getCol()].setMaterial(newDarkMaterial);
                }

                if(squares[pos.getRow()][pos.getCol()].getColor()){
                    squares[pos.getRow()][pos.getCol()].getChildren().clear();
//                    squares[pos.getRow()][pos.getCol()].getChildren().add((new ImageView(darkStar).autosize()));//write 2 functions

                }else {
                    squares[pos.getRow()][pos.getCol()].getChildren().clear();
//                    squares[pos.getRow()][pos.getCol()].getChildren().add(new ImageView(lightStar).autosize());
                }

        }
        for(ViewPiece viewPiece: viewState.getPieces()){

                movePiece(viewPiece.getId(),viewPiece.getPos());
                squares[viewPiece.getPos().getRow()][viewPiece.getPos().getCol()].drawPiece(viewPiece.getId());
        }
        clearRemaining();
        if ("WB".contains(viewState.getWhoseTurn())) {
            whoseTurnText.setText("Whose Turn: "+viewState.getWhoseTurn());
        } else {
            whoseTurnText.setText(viewState.getWhoseTurn());
        }
        totalRoundText.setText("Total round: "+ viewState.getTotalRound());
    }

    private void movePiece(String id, model.Pos pos) {
        for (Node n : pieceGroup.getChildren()){
            if(n.getClass() == MeshView.class){
                if ((id.equals(n.getId().substring(0, 1))) && (!n.getId().contains("set")) && (!n.getId().contains("killed"))) {
                    n.relocate(0.0,0.0);
                    n.setTranslateX(-n.getBoundsInLocal().getWidth()/2.0);
                    n.setTranslateY(-n.getBoundsInLocal().getHeight()/2.0);
                    n.setTranslateX(n.getTranslateX()+(-3.5 + pos.getRow())*SQUARE_SIDE);
                    n.setTranslateY(n.getTranslateY()+(-3.5+ pos.getCol())*SQUARE_SIDE);
                    System.out.println("Translate  X: "+n.getTranslateX()+" Y: "+n.getTranslateY()+" Z: "+n.getTranslateZ());
                    n.setId(n.getId() + "_set");
                    break;
                }
            }
        }
    }

    private void resetPieces(){
        for (Node n : pieceGroup.getChildren()){
            n.setId(n.getId().replace("_set",""));
            n.setId(n.getId().replace("_killed",""));
        }
    }

    private void clearRemaining(){
        for (Node n : pieceGroup.getChildren()){
            if ((!n.getId().contains("set")) && (!n.getId().contains("killed"))){
                n.setId(n.getId() + "_killed");
                n.relocate(0.0,0.0);
                n.setTranslateX(-n.getBoundsInLocal().getWidth()/2.0);
                n.setTranslateY(-n.getBoundsInLocal().getHeight()/2.0);
                n.setTranslateX(n.getTranslateX()+(-4.5 )*SQUARE_SIDE);
                n.setTranslateY(n.getTranslateY()+(-3.5)*SQUARE_SIDE);
            }
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

    private void buildAxes() {

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);
        xAxis.setMaterial(new PhongMaterial(Color.DARKRED));
        yAxis.setMaterial(new PhongMaterial(Color.DARKGREEN));
        zAxis.setMaterial(new PhongMaterial(Color.DARKBLUE));
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(true);
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
        fxmlLoader.setLocation(getClass().getResource("/fxml/ChessObjects.fxml"));
        try {
            pieceGroup = fxmlLoader.<Group>load();
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

    private void handleKeyboard(Scene scene, final Node root) {

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case Z:
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                        break;
                    case X:
                        axisGroup.setVisible(!axisGroup.isVisible());
                        break;
                }
            }
        });
    }

    private class SquareBox extends Box {

        private int row, col;
        private boolean isWhite;
        final PhongMaterial squareMaterial = new PhongMaterial();

        private SquareBox(){
            setWidth(SQUARE_SIDE);
            setHeight(SQUARE_SIDE);
            setDepth(1.0);
            setTranslateZ(-1.0);
            setOnMouseClicked(e -> handleMouseClick());
        }

        void setRowCol(int row, int col){
            this.row = row;
            this.col = col;
        }

        public boolean getColor(){ return isWhite; }

        public void setColor(boolean bool){
            isWhite = bool;
            if(bool){
                squareMaterial.setDiffuseMap(lightSquare);
            }else{
                squareMaterial.setDiffuseMap(darkSquare);
            }
            setMaterial(squareMaterial);
        }

        void handleMouseClick(){
            drawBoard(controller.clickHandler(new model.Pos(row, col)));
        }
    }

    private class Square extends StackPane {
        private boolean isWhite;
        private int row, col;
        private Text text = new Text();
        //		private Piece piece;
        private Square(){
            setPrefSize(60, 60);
            setStyle("-fx-border-color: black");
            setOnMouseClicked(e -> handleMouseClick());
        }

        void setRowCol(int row, int col){
            this.row = row;
            this.col = col;
        }
        public int getRow(){
            return row;
        }
        public int getCol(){
            return col;
        }

        public boolean getColor(){ return isWhite; }

        public void setColor(boolean bool){
            isWhite = bool;
            if(isWhite){
                setBackground(new Background(new BackgroundImage(lightSquare,null,null,null,null)));
//                setStyle("-fx-background-color: burlywood");
            }else{
                setBackground(new Background(new BackgroundImage(darkSquare,null,null,null,null)));
//                setStyle("-fx-background-color: darkgoldenrod");
            }
        }
        private void handleMouseClick(){
            //use row, col and piece;
            drawBoard(controller.clickHandler(new model.Pos(row, col)));
            //draw chessBoardPane
        }
        void drawPiece(String id){
            text = new Text(id);
            text.setFont(Font.font(40));
            text.setTextAlignment(TextAlignment.CENTER);
            if("KQNBPR".contains(id)) {
                text.setFill(Color.WHITE);
            }else{
                text.setFill(Color.BLACK);
            }

            this.getChildren().add(text);

            this.setAlignment(javafx.geometry.Pos.CENTER);
        }
        void resetText(String id){
            text.setText(id);
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
