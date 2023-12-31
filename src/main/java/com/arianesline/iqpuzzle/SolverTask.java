package com.arianesline.iqpuzzle;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

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
    protected Void call() {
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

                if (!checkHasFuture(frame)) continue;

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

    private boolean checkHasFuture(Frame frame) {

        // Check for condition that makes future impossible

        if (frame.balls[0][0] == null && frame.balls[0][1] != null && frame.balls[1][0] != null)
            return false;

        if (frame.balls[WIDTH-1][0] == null && frame.balls[WIDTH-2][0] != null && frame.balls[WIDTH-1][1] != null)
            return false;

        if (frame.balls[WIDTH-1][HEIGHT-1] == null && frame.balls[WIDTH-2][HEIGHT-1] != null && frame.balls[WIDTH-1][HEIGHT-2] != null)
            return false;

        if (frame.balls[0][HEIGHT-1] == null && frame.balls[1][HEIGHT-1] != null && frame.balls[0][HEIGHT-2] != null)
            return false;

        for (int i = 1; i < WIDTH - 1; i++) {
            for (int j = 1; j < HEIGHT - 1; j++) {
                if (frame.balls[i][j] == null && frame.balls[i - 1][j] != null && frame.balls[i + 1][j] != null && frame.balls[i][j - 1] != null && frame.balls[i][j + 1] != null) {
                    return false;


                }
            }
        }

        for (int i = 1; i < WIDTH - 1; i++) {
            if (frame.balls[i][0] == null && frame.balls[i - 1][0] != null && frame.balls[i + 1][0] != null && frame.balls[i][1] != null)
                return false;
            if (frame.balls[i][HEIGHT - 1] == null && frame.balls[i - 1][HEIGHT - 1] != null && frame.balls[i + 1][HEIGHT - 1] != null && frame.balls[i][HEIGHT - 2] != null)
                return false;
        }

        for (int j = 1; j < HEIGHT - 1; j++) {
            if (frame.balls[0][j] == null && frame.balls[0][j - 1] != null && frame.balls[0][j + 1] != null && frame.balls[1][j] != null)
                return false;

            if (frame.balls[WIDTH - 1][j] == null && frame.balls[WIDTH - 1][j - 1] != null && frame.balls[WIDTH - 1][j + 1] != null && frame.balls[WIDTH - 2][j] != null)
                return false;
        }

        return true;
    }

    private static List<Part> getFreeParts(Placement taskPlacement) {
        return taskPlacement.unusedParts;

    }
}
