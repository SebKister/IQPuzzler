package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;

public class SolverTask extends Task<Void> {

    IQPuzzleController controller;
    Placement placement;

    public SolverTask(IQPuzzleController IQPuzzleController, Placement placement) {
        this.controller = IQPuzzleController;
        this.placement = placement;
    }

    @Override
    protected Void call() throws Exception {
        var freeParts = IQPuzzleController.parts.stream()
                .filter(part -> placement.positioningList.stream()
                        .map(positioning -> positioning.part)
                        .noneMatch(part1 -> part1 == part)).toList();

        //Test for solution found
        if (freeParts.isEmpty()) {
            // A solution has been found
            synchronized (solutionPlacements) {
                if (isUniqueSolution(this.placement)) {
                    solutionPlacements.add(this.placement);
                    solutionCounter.incrementAndGet();
                    // Platform.runLater(() -> controller.onRefreshUI());
                    System.out.println(solutionCounter.get() + "Solution found : " + createdTaskCounter.get());
                }
            }

            if (runningTaskCounter.decrementAndGet() == 0) {
                endSolve = System.currentTimeMillis();
                System.out.println("Duration :" + (endSolve - startSolve) / 1000.0);
                Platform.runLater(() -> controller.onRefreshUI());
            }
            return null;
        }

        var frame = new Frame(WIDTH, HEIGHT);
        frame.loadPlacement(this.placement);
        List<Future<?>> tasks = new ArrayList<>();
        //Go over all available parts ( not used in Placement)

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                // Go over all empty cell
                if (frame.balls[i][j] == null) {
                    //Go over all rotation states
                    for (Orientation orient : Orientation.values()) {
                        //Go over all flip states
                        for (FlipState flststate : FlipState.values()) {
                            for (Part part : freeParts) {

                                if (frame.canAdd(part, i, j, orient, flststate)) {
                                    // Create new Task
                                    final Positioning positioning = new Positioning(part, i, j, orient, flststate);
                                    var solverTask = new SolverTask(this.controller, new Placement(this.placement, positioning));
                                    //Start task if place
                                    while (tasks.size() >= freeParts.size()) {
                                        tasks.removeIf(Future::isDone);
                                        Thread.yield();
                                    }
                                    runningTaskCounter.incrementAndGet();
                                    createdTaskCounter.incrementAndGet();
                                    tasks.add(controller.executorService.submit(solverTask));

                            }
                        }
                    }
                }
            }
        }
    }

        if(runningTaskCounter.decrementAndGet()==0)

    {
        endSolve = System.currentTimeMillis();
        System.out.println("Duration :" + (endSolve - startSolve) / 1000.0);
        Platform.runLater(() -> controller.onRefreshUI());
        controller.executorService.shutdown();
    }
        return null;
}
}
