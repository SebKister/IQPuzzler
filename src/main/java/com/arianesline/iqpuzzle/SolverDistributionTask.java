package com.arianesline.iqpuzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.arianesline.iqpuzzle.Core.solving;
import static com.arianesline.iqpuzzle.IQPuzzleController.updateFlag;

public class SolverDistributionTask implements Runnable {
    public final static AtomicInteger solutionCounter = new AtomicInteger(0);
    public static int MAXRUNNINGTASKS;
    public static ExecutorService executorService;
    Runnable updateCallBack;
    public static final ConcurrentLinkedQueue<Placement> solutionPlacements = new ConcurrentLinkedQueue<>();
    Placement initialPlacement;
    public static int sizeQueue = 1;
    public static int maxSizeQueue = sizeQueue;
    public static List<SolverTask> workers = new ArrayList<>();
    public static final ConcurrentLinkedQueue<SolverTask> freeWorkers = new ConcurrentLinkedQueue<>();
    private boolean interrupted;

    public SolverDistributionTask(Runnable callback, Placement challenge) {
        updateCallBack = callback;
        this.initialPlacement = challenge;
        interrupted=false;
    }

    public static boolean isUniqueSolution(Placement placement) {
        return solutionPlacements.stream().noneMatch(placement1 -> identicPlacement(placement1, placement));

    }

    public static boolean identicPlacement(Placement placementA, Placement placementB) {
        var frameA = new Frame(Core.WIDTH, Core.HEIGHT);
        var frameB = new Frame(Core.WIDTH, Core.HEIGHT);

        frameA.loadPlacement(placementA);
        frameB.loadPlacement(placementB);

        for (int i = 0; i < Core.WIDTH; i++) {
            for (int j = 0; j < Core.HEIGHT; j++) {
                if (frameA.balls[i][j] == null || frameB.balls[i][j] == null || !frameA.balls[i][j].part.color.equals(frameB.balls[i][j].part.color))
                    return false;
            }
        }
        return true;
    }


    @Override
    public void run() {
        workers.clear();
        freeWorkers.clear();

        executorService = Executors.newFixedThreadPool(MAXRUNNINGTASKS);
        //Create worker tasks

        for (int i = 0; i < MAXRUNNINGTASKS; i++) {
            var worker = new SolverTask();
            executorService.submit(worker);
            workers.add(worker);
        }

        while (freeWorkers.isEmpty()) Thread.yield();

        freeWorkers.poll().solverTaskQueue.add(initialPlacement);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        int duration = 10;
        int counter = 0;
        while (workers.stream().anyMatch(solverTask -> !solverTask.solverTaskQueue.isEmpty())) {

            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            counter++;
            if (counter > 500) duration = 1000;

            if (updateFlag.get()) {
                sizeQueue = workers.stream().mapToInt(solverTask -> solverTask.solverTaskQueue.size()).sum();
                if (sizeQueue > maxSizeQueue) maxSizeQueue = sizeQueue;

                if (updateCallBack != null) updateCallBack.run();
            }
            if(interrupted)
                break;
        }

        Core.endSolve = System.currentTimeMillis();
        if (updateCallBack != null) updateCallBack.run();

        for (SolverTask worker : workers) worker.stop();
        executorService.shutdown();
        solving.set(false);
    }


    public void stop(){

        interrupted=true;
    }
}
