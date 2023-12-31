package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;

public class SolverDistributionTask extends Task<Void> {
    private static final int MAXRUNNINGTASKS = Runtime.getRuntime().availableProcessors()-2;
    IQPuzzleController controller;
    static AtomicBoolean keepAlive = new AtomicBoolean(false);

    public SolverDistributionTask(IQPuzzleController iqPuzzleController) {
        controller = iqPuzzleController;
    }

    @Override
    protected Void call() throws Exception {

        keepAlive.set(true);
        //Create worker tasks
        for (int i = 0; i < MAXRUNNINGTASKS; i++) {
            var worker = new SolverTask(controller);
            executorService.submit(worker);
        }

        Thread.sleep(100);

        int size = solverTaskQueue.size();
        int maxSize = size;

        while (size > 0) {
            Thread.sleep(10);
            size = solverTaskQueue.size();
            if (size > maxSize) maxSize = size;
            if(UIUpdateFlag.get()){
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
