package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;

public class SolverDistributionTask extends Task<Void> {
    public static final int MAXRUNNINGTASKS = Runtime.getRuntime().availableProcessors() - 2;
    IQPuzzleController controller;
    static AtomicBoolean keepAlive = new AtomicBoolean(false);

    Placement initialPlacement;

    public static List<SolverTask> workers = new ArrayList<>();
    public static final ConcurrentLinkedQueue<SolverTask> freeWorkers = new ConcurrentLinkedQueue<>();

    public SolverDistributionTask(IQPuzzleController iqPuzzleController, Placement challenge) {
        controller = iqPuzzleController;
        this.initialPlacement = challenge;
    }

    @SuppressWarnings("BusyWait")
    @Override
    protected Void call() throws Exception {
        workers.clear();
        freeWorkers.clear();
        keepAlive.set(true);
        //Create worker tasks

        for (int i = 0; i < MAXRUNNINGTASKS; i++) {
            var worker = new SolverTask();
            executorService.submit(worker);
            workers.add(worker);
        }

        while (freeWorkers.isEmpty()) Thread.yield();

        freeWorkers.poll().solverTaskQueue.add(initialPlacement);

        Thread.sleep(100);

        int size = 1;
        int maxSize = size;
        int duration = 10;
        int counter = 0;
        while (workers.stream().anyMatch(solverTask -> !solverTask.solverTaskQueue.isEmpty())) {

            Thread.sleep(duration);
            counter++;
            if(counter>500) duration=1000;

            if (UIUpdateFlag.get()) {
                size = workers.stream().mapToInt(solverTask -> solverTask.solverTaskQueue.size()).sum();
                if (size > maxSize) maxSize = size;
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
