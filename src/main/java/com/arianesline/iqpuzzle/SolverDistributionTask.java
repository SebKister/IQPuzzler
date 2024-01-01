package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;

public class SolverDistributionTask extends Task<Void> {
    public static final int MAXRUNNINGTASKS = Runtime.getRuntime().availableProcessors() - 2;
    IQPuzzleController controller;
    static AtomicBoolean keepAlive = new AtomicBoolean(false);

    Placement initialPlacement;

    public static List<SolverTask> workers = new ArrayList<>();

    public SolverDistributionTask(IQPuzzleController iqPuzzleController, Placement challenge) {
        controller = iqPuzzleController;
        this.initialPlacement = challenge;
    }

    @Override
    protected Void call() throws Exception {
        workers.clear();
        keepAlive.set(true);
        //Create worker tasks

        for (int i = 0; i < MAXRUNNINGTASKS; i++) {
            var worker = new SolverTask();
            executorService.submit(worker);
            workers.add(worker);
        }

        workers.getFirst().solverTaskQueue.add(initialPlacement);

        Thread.sleep(100);

        int size =1;
        int maxSize = size;

        while (size > 0) {
            Thread.sleep(10);

            size = workers.stream().mapToInt(value -> value.solverTaskQueue.size()).sum();
            if (size > maxSize) maxSize = size;
            if (UIUpdateFlag.get()) {
                solverProgress.set(1.0 - (double) size / maxSize);
                Platform.runLater(() -> controller.onRefreshUI());
            }
        }

        keepAlive.set(false);
        endSolve = System.currentTimeMillis();
        Platform.runLater(() -> controller.onRefreshUI());
        executorService.shutdown();
        return null;
    }
}
