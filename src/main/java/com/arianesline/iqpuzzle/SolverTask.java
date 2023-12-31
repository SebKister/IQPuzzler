package com.arianesline.iqpuzzle;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;
import static com.arianesline.iqpuzzle.SolverDistributionTask.keepAlive;

public class SolverTask extends Task<Void> {

    IQPuzzleController controller;
    final Frame frame = new Frame(WIDTH, HEIGHT);
    final List<Placement> tasks = new ArrayList<>();

    public SolverTask(IQPuzzleController IQPuzzleController) {
        this.controller = IQPuzzleController;

    }

    @Override
    protected Void call() throws Exception {
        while (keepAlive.get()) {
            var taskPlacement = solverTaskQueue.pollLast();

            if (taskPlacement != null) {
                var freeParts = getFreeParts(taskPlacement);


                //Test for solution found
                if (freeParts.isEmpty()) {
                    // A solution has been found
                    synchronized (solutionPlacements) {
                        if (isUniqueSolution(taskPlacement)) {
                            solutionPlacements.add(taskPlacement);
                            solutionCounter.incrementAndGet();
                        }
                    }
                    continue;
                }

                frame.loadPlacement(taskPlacement);
                tasks.clear();
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
                                            tasks.add(new Placement(taskPlacement, positioning));
                                            createdTaskCounter.incrementAndGet();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                solverTaskQueue.addAll(tasks);
            }
            Thread.yield();
        }
        return null;
    }

    private static List<Part> getFreeParts(Placement taskPlacement) {
        return taskPlacement.unusedParts;

    }
}
