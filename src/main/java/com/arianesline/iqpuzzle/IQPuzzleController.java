package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.*;
import java.net.URL;
import java.util.*;

import static com.arianesline.iqpuzzle.Frame.bugBall;
import static com.arianesline.iqpuzzle.SolverDistributionTask.*;

public class IQPuzzleController implements Initializable {

    private static final int BALLSIZE = 20;
    private static final double FILLFACTOR = 0.9;
    public Canvas mainCanvas;
    public Label messageLabel;

    public Label solutionLabel;
    public ListView<Canvas> solutionFlowPane;
    public ComboBox<Part> partComboBox;
    public GridPane toolPane;
    public ProgressBar solverProgressBar;
    public CheckBox UIUpdateCheckBox;
    Frame currentFrame;

    static final SimpleDoubleProperty solverProgress = new SimpleDoubleProperty(0);
    static final SimpleBooleanProperty updateFlag = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Callback<ListView<Part>, ListCell<Part>> callback = new Callback<>() {
            @Override
            public ListCell<Part> call(ListView<Part> p) {
                return new ListCell<>() {
                    private final Canvas canvas;

                    {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        canvas = new Canvas();
                        canvas.setWidth(4 * BALLSIZE);
                        canvas.setHeight(4 * BALLSIZE);
                    }

                    @Override
                    protected void updateItem(Part item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            drawPart(canvas, item);
                            setGraphic(canvas);
                        }
                    }
                };
            }
        };
        partComboBox.setButtonCell(callback.call(null));
        partComboBox.setCellFactory(callback);


        buildFrame();
        currentFrame.loadPlacement(Core.currentPlacement);
        drawParts();
        drawFrame();
        solverProgressBar.progressProperty().bind(solverProgress);
        UIUpdateCheckBox.selectedProperty().bindBidirectional(updateFlag);

    }


    private void drawParts() {
        partComboBox.getItems().clear();
        partComboBox.getItems().addAll(Core.parts);
    }

    private void drawPart(Canvas canvas, Part part) {

        var partWidth = part.balls.stream().mapToInt(ball -> ball.rpx).max().orElse(0) - part.balls.stream().mapToInt(ball -> ball.rpx).min().orElse(0) + 1;
        var partHeight = part.balls.stream().mapToInt(ball -> ball.rpy).max().orElse(0) - part.balls.stream().mapToInt(ball -> ball.rpy).min().orElse(0) + 1;

        canvas.setWidth(partWidth * BALLSIZE);
        canvas.setHeight(partHeight * BALLSIZE);
        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        part.balls.forEach(ball -> drawBall(ball, gc));
        canvas.setScaleY(-1);

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
        gc.setFill(Color.valueOf(ball.part.color));
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
        gc.setFill(Color.valueOf(ball.part.color));
        gc.fillOval(ball.rpx * BALLSIZE + BALLSIZE * (1 - FILLFACTOR) / 2.0, ball.rpy * BALLSIZE + BALLSIZE * (1 - FILLFACTOR) / 2.0, BALLSIZE * FILLFACTOR, BALLSIZE * FILLFACTOR);
    }

    private void buildFrame() {
        currentFrame = new Frame(Core.WIDTH, Core.HEIGHT);
    }

    public void onSolve() {
        Core.startSolve = System.currentTimeMillis();
        Core.endSolve = 0;
        solutionPlacements.clear();
        solutionCounter.set(0);

        Core.distributionTask = new SolverDistributionTask(this::onRefreshUI, Core.currentPlacement);
        Core.distriExecutorService.submit(Core.distributionTask);
    }

    public void displayTasks() {
        messageLabel.setText("Tasks : " + workers.stream().mapToLong(solverTask -> solverTask.createdTaskCounter).sum()
                + " - Duration : " + (((Core.endSolve == 0) ? System.currentTimeMillis() : Core.endSolve) - Core.startSolve) / 1000.0);
    }

    public void displaySolutionCount() {
        solutionLabel.setText("Solution : " + solutionCounter.get());
    }

    public Canvas drawSolutionPlacement(Placement placement) {
        var frameSolution = new Frame(Core.WIDTH, Core.HEIGHT);
        frameSolution.loadPlacement(placement);
        return drawFrame(frameSolution);
    }

    public void onMoveRight() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        Core.currentPlacement.movePartRight(partSelected);
        drawCurrentPlacement();
    }

    public void onMoveUp() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        Core.currentPlacement.movePartUp(partSelected);
        drawCurrentPlacement();

    }

    public void onMoveDown() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        Core.currentPlacement.movePartDown(partSelected);
        drawCurrentPlacement();
    }

    public void onMoveLeft() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        Core.currentPlacement.movePartLeft(partSelected);
        drawCurrentPlacement();
    }

    public void onRotate() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        Core.currentPlacement.rotatePart(partSelected);
        drawCurrentPlacement();
    }

    public void onFlip() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        Core.currentPlacement.flipPart(partSelected);
        drawCurrentPlacement();
    }

    public void onRemovePart() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        if (!Core.currentPlacement.containsPart(partSelected)) return;

        Core.currentPlacement.removePart(partSelected);

        drawCurrentPlacement();
        updateToolPane();
    }

    public void onAddPart() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();
        if (partSelected == null) return;
        if (Core.currentPlacement.containsPart(partSelected)) return;

        Core.currentPlacement.addPositioning(new Positioning(partSelected, 0, 0, Orientation.UP, FlipState.FLAT));

        drawCurrentPlacement();
        updateToolPane();
    }

    private void drawCurrentPlacement() {
        currentFrame.loadPlacement(Core.currentPlacement);
        drawFrame();
    }

    public void onPartSelected() {
        updateToolPane();

    }

    private void updateToolPane() {
        var partSelected = partComboBox.getSelectionModel().getSelectedItem();

        toolPane.setDisable(!Core.currentPlacement.containsPart(partSelected));
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
        Core.currentPlacement.clear(Core.parts);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            reader.lines().forEach(s -> {
                var splits = s.split(";");
                Optional<Part> first = Core.parts.stream()
                        .filter(part -> part.color.equals(splits[0]))
                        .findFirst();
                first.ifPresent(part -> Core.currentPlacement.addPositioning(new Positioning(part
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
            Core.currentPlacement.positioningList.forEach(positioning -> {
                try {
                    writer.write(positioning.part.color);
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
        Core.currentPlacement.positioningList.clear();
        Core.currentPlacement.unusedParts.clear();
        Core.currentPlacement.unusedParts.addAll(Core.parts);
        drawCurrentPlacement();
        updateToolPane();
    }

    public void onRefreshUI() {
        Platform.runLater(() -> {
            solverProgress.set(1.0 - (double) sizeQueue / maxSizeQueue);
            solutionFlowPane.getItems().clear();
            solutionPlacements.forEach(placement -> solutionFlowPane.getItems().add(drawSolutionPlacement(placement)));
            displaySolutionCount();
            displayTasks();
        });
    }

    public void onStopSolve() {

        keepAlive.set(false);
        Core.endSolve = System.currentTimeMillis();
        onRefreshUI();
        if (executorService != null) executorService.shutdown();
    }
}