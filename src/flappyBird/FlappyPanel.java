package flappyBird;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author Lotfi Boukhemerra.
 */
public class FlappyPanel extends Application implements EventHandler<KeyEvent> {
    
    private final double SPEED = 5.0;//origin = 7.0

    private final Image BACKGROUND = new Image(getClass().getResourceAsStream("bg-2.png"));
    private final Image GROUND = new Image(getClass().getResourceAsStream("ground-2.png"));
    private final Image FLAPPING_MODE_1 = new Image(getClass().getResourceAsStream("bird.png"));
    private final Image FLAPPING_MODE_2 = new Image(getClass().getResourceAsStream("bird2.png"));
    private final Image TOPTUBE_BG = new Image(getClass().getResourceAsStream("toptube.png"));
    private final Image BOTTOMTUBE_BG = new Image(getClass().getResourceAsStream("bottomtube.png"));
    //private final String URL = FlappyPanel.class.getResource("music.mp3").toExternalForm();
    private final String URL = FlappyPanel.class.getResource("Woodland Fantasy.mp3").toExternalForm();
    private final double WIDTH = 350.0, HEIGHT = 600.0;
    private final int TUBE_WIDTH = 52;
    private final ImageView[][] MATRIX_TUBES = new ImageView[3][2];
    private double initX;
    private double initY;
    private double move;
    private boolean isClashed;
    private double yPosition;
    static ImageView bird;
    private int tubeHeader;
    public MediaPlayer music;
    private Timeline falling, timeline, flying;

    @Override
    public void start(Stage stage) throws Exception {
        // if Bird clash to tube then: isClashed = true
        isClashed = false;
        tubeHeader = 0;
        // background music
        Media media = new Media(URL);
        music = new MediaPlayer(media);
        //////////////////////////////////////
        // Root:
        Group root = new Group();
        root.setOnMousePressed(e -> {
            initX = e.getScreenX() - stage.getX();
            initY = e.getScreenY() - stage.getY();

        });
        root.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - initX);
            stage.setY(e.getScreenY() - initY);
        });

        root.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                music.stop();
                Platform.exit();// GUI
                System.exit(0);// JVM
            }
        });
        //////////////////////////////////
        // Background Image
        ImageView bg = new ImageView(BACKGROUND);
        bg.setFitWidth(350.0);
        bg.setFitHeight(510.0);

        List<ImageView> grounds = new ArrayList<>();
        grounds.add(new ImageView(GROUND));
        grounds.add(new ImageView(GROUND));
        grounds.forEach((ImageView ground_img) -> {
            ground_img.setFitWidth(350.0);
            ground_img.setFitHeight(90.0);//orgin size = 112
            ground_img.setY(510);
            ground_img.toFront();// what the role of this command.
        });
        grounds.get(1).setX(350);
        ////////////////////////////
        bird = new ImageView(FLAPPING_MODE_1);
        bird.setFitWidth(38.0);
        bird.setFitHeight(32.0);
        ////////////////////////////////////
        for (int i = 0; i < 3; i++) {
            MATRIX_TUBES[i][0] = new ImageView(TOPTUBE_BG);
            MATRIX_TUBES[i][0].setFitWidth(52.0);
            MATRIX_TUBES[i][0].setFitHeight(500.0);
            MATRIX_TUBES[i][0].toBack();

            MATRIX_TUBES[i][1] = new ImageView(BOTTOMTUBE_BG);
            MATRIX_TUBES[i][1].setFitWidth(52.0);
            MATRIX_TUBES[i][1].setFitHeight(500.0);
            MATRIX_TUBES[i][1].toBack();
        }
        makeNewGame();
        //////
        root.getChildren().addAll(bg, MATRIX_TUBES[0][0], MATRIX_TUBES[0][1], bird, grounds.get(0), grounds.get(1));
        /////////////////////////////////////////////////////////
        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.TRANSPARENT);
        scene.setOnKeyPressed(this);
        scene.setOnKeyReleased((event) -> {
            falling.play();
            flying.pause();
            bird.setImage(FLAPPING_MODE_1);
        });
        stage.setScene(scene);
        stage.sizeToScene();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.show();
        music.setVolume(0.3);
        music.play();
        music.setCycleCount(MediaPlayer.INDEFINITE);
        ////////////////////// code section /////////////////////////////////
        timeline = new Timeline(new KeyFrame(Duration.millis(SPEED), (ActionEvent e) -> {
            /// Background keep moving.
            if (grounds.get(0).getX() == -WIDTH) {
                grounds.get(0).setX(grounds.get(1).getX() + WIDTH - 1);
            } else {
                grounds.get(0).setX(grounds.get(0).getX() - 1);
            }
            if (grounds.get(1).getX() == -WIDTH) {
                grounds.get(1).setX(grounds.get(0).getX() + WIDTH - 1);

            } else {
                grounds.get(1).setX(grounds.get(1).getX() - 1);
            }
            ///// change tubes positions. 
            if ((MATRIX_TUBES[0][0].getX() + TUBE_WIDTH) == 0) {
                move = 350;// start again.
                do {
                    yPosition = new Random().nextInt(451);// yPosition € [190,450[
                } while (yPosition < 190);
                MATRIX_TUBES[0][1].setY(yPosition);
                MATRIX_TUBES[0][0].setY(yPosition - 628);

            } else {
                move -= 1;
            }
            MATRIX_TUBES[0][0].setX(move);
            MATRIX_TUBES[0][1].setX(move);

            /// test if the bird make clash or notyet.
            // 38 is the width of bird
            // 32 is the height of bird
            // 52 is the width of tube.
            if (bird.getX() + 38 == MATRIX_TUBES[tubeHeader][0].getX()) {
                if (bird.getY() <= MATRIX_TUBES[tubeHeader][0].getY() + 500
                        || bird.getY() + 32 >= MATRIX_TUBES[tubeHeader][1].getY()) {
                    timeline.pause();
                    bird.setRotate(90);
                    isClashed = true;
                }
            }
            if ((bird.getX() + 38 > MATRIX_TUBES[tubeHeader][0].getX() && bird.getX() + 38 <= MATRIX_TUBES[tubeHeader][0].getX() + 52)
                    || bird.getX() >= MATRIX_TUBES[tubeHeader][0].getX() && bird.getX() <= MATRIX_TUBES[tubeHeader][0].getX() + 52) {
                if (bird.getY() <= MATRIX_TUBES[tubeHeader][0].getY() + 500
                        || bird.getY() + 32 >= MATRIX_TUBES[tubeHeader][1].getY()) {
                    timeline.pause();
                    bird.setRotate(90);
                    isClashed = true;
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        /////////// falling the Bird.
        falling = new Timeline(new KeyFrame(Duration.millis(SPEED), (ActionEvent e) -> {
            bird.setY(bird.getY() + 1.0);// 50.0/ 5.0
        }));
        falling.setCycleCount(Timeline.INDEFINITE);
        /////////// flying the Bird.
        flying = new Timeline(new KeyFrame(Duration.millis(SPEED), (ActionEvent e) -> {
            bird.setImage(bird.getImage() == FLAPPING_MODE_1 ? FLAPPING_MODE_2 : FLAPPING_MODE_1);
        }));
        flying.setCycleCount(Timeline.INDEFINITE);
//        falling.play();
//        timeline.play();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.UP) {
            if (!isClashed) {
                flying.play();
                falling.pause();
                bird.setY(bird.getY() - 35.0);
            } else {
                makeNewGame();
                bird.setRotate(0);
                isClashed = false;
                timeline.play();
            }
            if (falling.getStatus() == Timeline.Status.STOPPED && timeline.getStatus() == Timeline.Status.STOPPED) {
                falling.play();
                timeline.play();
            }
        }
    }

    private void makeNewGame() {
        bird.setX(60);
        bird.setY(250);
        MATRIX_TUBES[0][0].setX(350);//start position
        MATRIX_TUBES[0][0].setY(-353);//start to = -353
        move = 350;// position of heading tubes.
        ////
        MATRIX_TUBES[0][1].setX(350);//start position
        MATRIX_TUBES[0][1].setY(275);//start to = 275
        
        MATRIX_TUBES[1][0].setX(move+236);
        MATRIX_TUBES[1][1].setX(move+236);
        MATRIX_TUBES[2][0].setX(move+(236*2));
        MATRIX_TUBES[2][0].setX(move+(236*2));
        //music.seek(Duration.ZERO);
    }
}
