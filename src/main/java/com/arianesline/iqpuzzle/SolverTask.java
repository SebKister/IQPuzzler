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
                    System.out.println(solutionCounter.get() + "Solution found : " + createdTaskCounter.get());
                }
            }
            runningTaskCounter.decrementAndGet();
            if (verbose) System.out.println(runningTaskCounter.get() + " - " + createdTaskCounter.get());
            return null;
        }

        var frame = new Frame(WIDTH, HEIGHT);
        frame.loadPlacement(this.placement);
        List<SolverTask> tasks = new ArrayList<>();
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
                                    tasks.add(new SolverTask(this.controller, new Placement(this.placement, positioning)));
                                    createdTaskCounter.incrementAndGet();

                                }
                            }
                        }
                    }
                }
            }
        }
        solverTaskQueue.addAll(tasks);
        runningTaskCounter.decrementAndGet();
        if (verbose) System.out.println(runningTaskCounter.get() + " - " + createdTaskCounter.get());
        return null;
    }
}
