package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;

public class SolverDistributionTask extends Task<Void> {
    private static final int MAXRUNNINGTASKS = Runtime.getRuntime().availableProcessors();
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

        Thread.sleep(10);
        while (!solverTaskQueue.isEmpty()) {
            Thread.sleep(10);
        }
        keepAlive.set(false);
        endSolve = System.currentTimeMillis();
        System.out.println("Duration : " + (endSolve - startSolve) / 1000.0);
        Platform.runLater(() -> controller.onRefreshUI());
        executorService.shutdown();
        return null;
    }
}
