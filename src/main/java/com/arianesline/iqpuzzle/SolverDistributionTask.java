package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.concurrent.Task;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;

public class SolverDistributionTask extends Task<Void> {
    private  static final int MAXRUNNINGTASKS = Runtime.getRuntime().availableProcessors() ;
    IQPuzzleController controller;

    public SolverDistributionTask(IQPuzzleController iqPuzzleController) {
        controller = iqPuzzleController;
    }

    @Override
    protected Void call() throws Exception {
        while (runningTaskCounter.get() > 0 || !solverTaskQueue.isEmpty()) {
            if (runningTaskCounter.get() < MAXRUNNINGTASKS) {
                var task = solverTaskQueue.poll();
                if (task != null) {
                    runningTaskCounter.incrementAndGet();
                    executorService.submit(task);
                    if (verbose) System.out.println(runningTaskCounter.get() + " - " + createdTaskCounter.get());
                }
            }
        }
        endSolve = System.currentTimeMillis();
        System.out.println("Duration : " + (endSolve - startSolve) / 1000.0);
      //  Platform.runLater(() -> controller.onRefreshUI());
        return null;
    }
}
