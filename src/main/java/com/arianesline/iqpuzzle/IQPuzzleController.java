package com.arianesline.iqpuzzle;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.arianesline.iqpuzzle.Frame.bugBall;
import static com.arianesline.iqpuzzle.SolverDistributionTask.keepAlive;

public class IQPuzzleController implements Initializable {

    private static final int BALLSIZE = 20;
    public static final int WIDTH = 11;
    public static final int HEIGHT = 5;
    private static final double FILLFACTOR = 0.9;
    public Canvas mainCanvas;
    public Label messageLabel;
    public static final ConcurrentLinkedQueue<Placement> solutionPlacements = new ConcurrentLinkedQueue<>();
    public Label solutionLabel;
    public ListView<Canvas> solutionFlowPane;
    public ComboBox<Pane> partComboBox;
    public GridPane toolPane;
    public ProgressBar solverProgressBar;
    public CheckBox UIUpdateCheckBox;
    Frame currentFrame;

    final static List<Part> parts = new ArrayList<>();
    final static List<Placement> challengePlacements = new ArrayList<>();

    public final static AtomicInteger runningTaskCounter = new AtomicInteger(0);
    public final static AtomicInteger createdTaskCounter = new AtomicInteger(0);
    public final static AtomicInteger solutionCounter = new AtomicInteger(0);
    public static ExecutorService executorService;
    public static SolverDistributionTask distributionTask;
    public static Placement currentPlacement = new Placement(0, parts);
    static long startSolve;
    static long endSolve;

    static final SimpleDoubleProperty solverProgress = new SimpleDoubleProperty(0);
    static final SimpleBooleanProperty UIUpdateFlag = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buildFrame();
        buildParts();
        buildChallenges();
        currentPlacement = challengePlacements.getFirst();
        currentFrame.loadPlacement(currentPlacement);
        drawParts();
        drawFrame();
        solverProgressBar.progressProperty().bind(solverProgress);
        UIUpdateCheckBox.selectedProperty().bindBidirectional(UIUpdateFlag);
    }

    private void buildChallenges() {
        var placement = new Placement(1, parts);

        placement.addPositioning(new Positioning(parts.get(0), 4, 4, Orientation.LEFT, FlipState.FLIPPED));
        placement.addPositioning(new Positioning(parts.get(4), 1, 1, Orientation.UP, FlipState.FLIPPED));
        placement.addPositioning(new Positioning(parts.get(9), 2, 0, Orientation.LEFT, FlipState.FLAT));
        placement.addPositioning(new Positioning(parts.get(3), 2, 1, Orientation.UP, FlipState.FLAT));
        placement.addPositioning(new Positioning(parts.get(5), 5, 0, Orientation.UP, FlipState.FLIPPED));
        placement.addPositioning(new Positioning(parts.get(2), 6, 2, Orientation.LEFT, FlipState.FLAT));
        placement.addPositioning(new Positioning(parts.get(8), 4, 0, Orientation.LEFT, FlipState.FLAT));
        //    placement.addPositioning(new Positioning(parts.get(11), 6, 0, Orientation.RIGHT, FlipState.FLIPPED));
        //   placement.addPositioning(new Positioning(parts.get(1), 6, 4, Orientation.RIGHT, FlipState.FLAT));
        challengePlacements.add(placement);


    }

    private void drawParts() {
        partComboBox.getItems().clear();
        parts.forEach(part -> {
            Pane e = new Pane(drawPart(part));
            e.setUserData(part);
            partComboBox.getItems().add(e);
        });
    }

    private Canvas drawPart(Part part) {
        Canvas canvas = new Canvas();
        var partWidth = part.balls.stream().mapToInt(ball -> ball.rpx).max().orElse(0) - part.balls.stream().mapToInt(ball -> ball.rpx).min().orElse(0) + 1;
        var partHeight = part.balls.stream().mapToInt(ball -> ball.rpy).max().orElse(0) - part.balls.stream().mapToInt(ball -> ball.rpy).min().orElse(0) + 1;

        canvas.setWidth(partWidth * BALLSIZE + BALLSIZE);
        canvas.setHeight(partHeight * BALLSIZE + BALLSIZE);
        var gc = canvas.getGraphicsContext2D();
        part.balls.forEach(ball -> drawBall(ball, gc));
        canvas.setScaleY(-1);
        return canvas;
    }

    private void buildParts() {
        var part = new Part(Color.YELLOW);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 0, 3));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

        part = new Part(Color.DARKBLUE);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 0));

        parts.add(part);

        part = new Part(Color.ORANGE);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 2, 1));
        part.balls.add(new Ball(part, 1, 2));

        parts.add(part);

        part = new Part(Color.SEAGREEN);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

        part = new Part(Color.PINK);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 2));
        part.balls.add(new Ball(part, 1, 3));

        parts.add(part);

        part = new Part(Color.DARKRED);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 1, 2));

        parts.add(part);

        part = new Part(Color.DARKVIOLET);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 1, 2));
        part.balls.add(new Ball(part, 2, 2));

        parts.add(part);

        part = new Part(Color.LIGHTGREEN);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 2, 1));
        part.balls.add(new Ball(part, 2, 0));

        parts.add(part);

        part = new Part(Color.LIGHTBLUE);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

        part = new Part(Color.BLUE);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 2));
        part.balls.add(new Ball(part, 2, 2));

        parts.add(part);

        part = new Part(Color.ORANGERED);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 0, 3));
        part.balls.add(new Ball(part, 1, 3));

        parts.add(part);

        part = new Part(Color.AQUAMARINE);
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 0));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

    }

    public void drawFrame() {
        var gc = mainCanvas.getGraphicsContext2D();
        mainCanvas.setScaleY(-1);
        gc.clearRect(0, 0, currentFrame.width * BALLSIZE, currentFrame.height * BALLSIZE);
        for (int i = 0; i < currentFrame.width; i++)
            for (int j = 0; j < currentFrame.height; j++) {
                if (currentFrame.balls[i][j] == bugBall) {
                    drawBugBallRect(i, j, gc);
                } else {
                    drawBallRect(i, j, gc);
                }
                drawBall(currentFrame.balls[i][j], i, j, gc);
            }
    }

    public Canvas drawFrame(Frame frameArg) {

        Canvas canvas = new Canvas();
        var partWidth = frameArg.width;
        var partHeight = frameArg.height;

        canvas.setWidth(partWidth * BALLSIZE);
        canvas.setHeight(partHeight * BALLSIZE);
        var gc = canvas.getGraphicsContext2D();

        canvas.setScaleY(-1);

        gc.clearRect(0, 0, frameArg.width * BALLSIZE, frameArg.height * BALLSIZE);
        for (int i = 0; i < frameArg.width; i++)
            for (int j = 0; j < frameArg.height; j++) {
                drawBallRect(i, j, gc);
                drawBall(frameArg.balls[i][j], i, j, gc);
            }
        return canvas;
    }

    private void drawBall(Ball ball, int i, int j, GraphicsContext gc) {
        if (ball == null) return;
        gc.setFill(ball.part.color);
        gc.fillOval(i * BALLSIZE + BALLSIZE * (1 - FILLFACTOR) / 2.0, j * BALLSIZE + BALLSIZE * (1 - FILLFACTOR) / 2.0, BALLSIZE * FILLFACTOR, BALLSIZE * FILLFACTOR);
    }

    private void drawBallRect(int i, int j, GraphicsContext gc) {
        gc.setFill(Color.color(0.1, 0.1, 0.1));
        gc.setStroke(Color.color(0.5, 0.5, 0.5));
        gc.fillRect(i * BALLSIZE, j * BALLSIZE, BALLSIZE, BALLSIZE);
        gc.strokeRect(i * BALLSIZE, j * BALLSIZE, BALLSIZE, BALLSIZE);
    }

    private void drawBugBallRect(int i, int j, GraphicsContext gc) {
        gc.setFill(Color.color(0.8, 0.1, 0.1));
        gc.setStroke(Color.color(0.5, 0.5, 0.5));
        gc.fillRect(i * BALLSIZE, j * BALLSIZE, BALLSIZE, BALLSIZE);
        gc.strokeRect(i * BALLSIZE, j * BALLSIZE, BALLSIZE, BALLSIZE);
    }

    private void drawBall(Ball ball, GraphicsContext gc) {
        if (ball == null) return;
        gc.setFill(ball.part.color);
        gc.fillOval(ball.rpx * BALLSIZE + BALLSIZE * (1 - FILLFACTOR) / 2.0, ball.rpy * BALLSIZE + BALLSIZE * (1 - FILLFACTOR) / 2.0, BALLSIZE * FILLFACTOR, BALLSIZE * FILLFACTOR);
    }

    private void buildFrame() {
        currentFrame = new Frame(WIDTH, HEIGHT);
    }

    public void onSolve() {
        startSolve = System.currentTimeMillis();
        endSolve = 0;
        solutionPlacements.clear();
        runningTaskCounter.set(0);
        createdTaskCounter.set(0);
        solutionCounter.set(0);

        createdTaskCounter.incrementAndGet();
        executorService = Executors.newCachedThreadPool();
        distributionTask = new SolverDistributionTask(this,currentPlacement);
        executorService.submit(distributionTask);
    }

    public void displayTasks() {
        messageLabel.setText("Tasks : " + createdTaskCounter.get()
                + " - Duration : " + (((endSolve == 0) ? System.currentTimeMillis() : endSolve) - startSolve) / 1000.0);
    }

    public void displaySolutionCount() {
        solutionLabel.setText("Solution : " + solutionCounter.get());
    }

    public Canvas drawSolutionPlacement(Placement placement) {
        var frameSolution = new Frame(WIDTH, HEIGHT);
        frameSolution.loadPlacement(placement);
        return drawFrame(frameSolution);
    }

    public static boolean isUniqueSolution(Placement placement) {
        return solutionPlacements.stream().noneMatch(placement1 -> identicPlacement(placement1, placement));

    }

    public static boolean identicPlacement(Placement placementA, Placement placementB) {
        var frameA = new Frame(WIDTH, HEIGHT);
        var frameB = new Frame(WIDTH, HEIGHT);

        frameA.loadPlacement(placementA);
        frameB.loadPlacement(placementB);

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (frameA.balls[i][j] == null || frameB.balls[i][j] == null || frameA.balls[i][j].part.color != frameB.balls[i][j].part.color)
                    return false;
            }
        }

        return true;
    }

    public void onMoveRight() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        currentPlacement.movePartRight(partSelected);
        drawCurrentPlacement();
    }

    public void onMoveUp() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        currentPlacement.movePartUp(partSelected);
        drawCurrentPlacement();

    }

    public void onMoveDown() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        currentPlacement.movePartDown(partSelected);
        drawCurrentPlacement();
    }

    public void onMoveLeft() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        currentPlacement.movePartLeft(partSelected);
        drawCurrentPlacement();
    }

    public void onRotate() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        currentPlacement.rotatePart(partSelected);
        drawCurrentPlacement();
    }

    public void onFlip() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        currentPlacement.flipPart(partSelected);
        drawCurrentPlacement();
    }

    public void onRemovePart() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        if (!currentPlacement.containsPart(partSelected)) return;

        currentPlacement.removePart(partSelected);

        drawCurrentPlacement();
        updateToolPane();
    }

    public void onAddPart() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        if (currentPlacement.containsPart(partSelected)) return;

        currentPlacement.addPositioning(new Positioning(partSelected, 0, 0, Orientation.UP, FlipState.FLAT));

        drawCurrentPlacement();
        updateToolPane();
    }

    private void drawCurrentPlacement() {
        currentFrame.loadPlacement(currentPlacement);
        drawFrame();
    }

    public void onPartSelected() {
        updateToolPane();

    }

    private void updateToolPane() {
        Pane selectedItem = partComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        var partSelected = (Part) selectedItem.getUserData();

        toolPane.setDisable(!currentPlacement.containsPart(partSelected));
    }

    public void onLoadChallenge() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CHA files (*.cha)", "*.cha");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            openChallengeFromFile(file);
            drawCurrentPlacement();
            updateToolPane();
        }
    }

    private void openChallengeFromFile(File file) {
        currentPlacement.clear(parts);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            reader.lines().forEach(s -> {
                var splits = s.split(";");
                Optional<Part> first = parts.stream()
                        .filter(part -> part.color.toString().equals(splits[0]))
                        .findFirst();
                first.ifPresent(part -> currentPlacement.addPositioning(new Positioning(part
                        , Integer.parseInt(splits[1])
                        , Integer.parseInt(splits[2])
                        , Orientation.valueOf(splits[3]), FlipState.valueOf(splits[4]))));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onSaveChallenge() throws IOException {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CHA files (*.cha)", "*.cha");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            saveChallengeToFile(file);
        }
    }

    private void saveChallengeToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            currentPlacement.positioningList.forEach(positioning -> {
                try {
                    writer.write(positioning.part.color.toString());
                    writer.write(";");
                    writer.write(Integer.toString(positioning.posx));
                    writer.write(";");
                    writer.write(Integer.toString(positioning.posy));
                    writer.write(";");
                    writer.write(positioning.orientation.name());
                    writer.write(";");
                    writer.write(positioning.flipState.name());
                    writer.write(";");
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void onResetChallenge() {
        currentPlacement.positioningList.clear();
        currentPlacement.unusedParts.clear();
        currentPlacement.unusedParts.addAll(parts);
        drawCurrentPlacement();
        updateToolPane();
    }

    public void onRefreshUI() {
        solutionFlowPane.getItems().clear();
        solutionPlacements.forEach(placement -> solutionFlowPane.getItems().add(drawSolutionPlacement(placement)));
        displaySolutionCount();
        displayTasks();
    }

    public void onStopSolve() {

        keepAlive.set(false);
        endSolve = System.currentTimeMillis();
        onRefreshUI();
        if (executorService != null) executorService.shutdown();
    }
}