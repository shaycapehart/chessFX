package controller;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 *
 * @author JosePereda
 */
public class Qubit3D extends Group {

    private int currentStep = 0;
    private double mouseOldX, mouseOldY = 0;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private Group rodSphere;
    private final Rotate myRotate = new Rotate(90, 0, 0, 0, Rotate.Z_AXIS);

    public Qubit3D() {
        createQubit();
    }

    private void createQubit() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.getTransforms().addAll(rotateX, rotateY, new Translate(0, 0, -200));

//        FrustumMesh plane = new FrustumMesh(50, 50, 1, 1, new Point3D(0, -0.5f, 0), new Point3D(0, 0.5f, 0));
//        plane.setMaterial(new PhongMaterial(Color.web("#ccdd3320")));
//
//        SegmentedSphereMesh innerSphere = new SegmentedSphereMesh(40, 0, 0, 50, new Point3D(0, 0, 0));
//        innerSphere.setMaterial(new PhongMaterial(Color.web("#ff800080")));
//
//        SegmentedSphereMesh frameSphere = new SegmentedSphereMesh(20, 0, 0, 50, new Point3D(0, 0, 0));
//        frameSphere.setMaterial(new PhongMaterial(Color.BLACK));
//        frameSphere.setDrawMode(DrawMode.LINE);
//
//        FrustumMesh rod = new FrustumMesh(2, 2, 1, 1, new Point3D(0, 0, 0), new Point3D(50, 0, 0));
//        rod.setMaterial(new PhongMaterial(Color.web("#0080ff")));
//
//        SegmentedSphereMesh smallSphere = new SegmentedSphereMesh(20, 0, 0, 4, new Point3D(50, 0, 0));
//        smallSphere.setMaterial(new PhongMaterial(Color.web("#0080ff")));

        Box myBox = new Box(25,25,25);
        myBox.setMaterial(new PhongMaterial(Color.web("#ccdd3320")));

        Box myBox2 = new Box(25,25,25);
        myBox2.setMaterial(new PhongMaterial(Color.web("#0080ff")));

        rodSphere = new Group(myBox);
        Group group = new Group(myBox2, rodSphere, new AmbientLight(Color.BISQUE));

        SubScene subScene = new SubScene(group, 100, 100, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        subScene.setOnMousePressed(event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            rotateX.setAngle(rotateX.getAngle() - (event.getSceneY() - mouseOldY));
            rotateY.setAngle(rotateY.getAngle() + (event.getSceneX() - mouseOldX));
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        getChildren().add(subScene);

        rodSphere.getTransforms().setAll(myRotate);
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void incrementStep() {
        currentStep++;
    }

    public void resetStep() {
        currentStep = 0;
    }

    public void flip() {
        myRotate.setAngle(- myRotate.getAngle());
    }


    public void show() {
        BorderPane bp = new BorderPane(this);
        Scene scene = new Scene(bp, 300, 300);
        Stage stage = new Stage();
        stage.setTitle("StrangeFX rendering");
        stage.setScene(scene);
        System.out.println("show stage...");
        stage.show();
        System.out.println("showed scene");

    }
}