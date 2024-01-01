package com.arianesline.iqpuzzle;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.arianesline.iqpuzzle.IQPuzzleController.*;
import static com.arianesline.iqpuzzle.SolverDistributionTask.*;


public class SolverTask extends Task<Void> {

    public static final FlipState[] FLIP_STATES = FlipState.values();
    public static final Orientation[] ORIENTATIONS = Orientation.values();
    final Frame frame = new Frame(WIDTH, HEIGHT);
    final List<Placement> placements = new ArrayList<>();
    public final ConcurrentLinkedDeque<Placement> solverTaskQueue = new ConcurrentLinkedDeque<>();

    public long createdTaskCounter;


    @Override
    protected Void call() {

        createdTaskCounter = 0;

        while (keepAlive.get()) {
            var taskPlacement = solverTaskQueue.pollLast();

            if (taskPlacement != null) {
                var freeParts = taskPlacement.unusedParts;

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
                placements.clear();

                //Go over all available parts ( not used in Placement)
                if (!checkHasFuture(frame)) continue;

                for (int i = 0; i < WIDTH; i++) {
                    for (int j = 0; j < HEIGHT; j++) {
                        // Go over all empty cell
                        if (frame.balls[i][j] == null) {
                            //Go over all rotation states

                            for (Orientation orient : ORIENTATIONS) {
                                //Go over all flip states
                                for (FlipState flststate : FLIP_STATES) {
                                    for (Part part : freeParts) {
                                        if (frame.canAdd(part, i, j, orient, flststate)) {
                                            // Create new Task
                                            final Positioning positioning = new Positioning(part, i, j, orient, flststate);
                                            placements.add(new Placement(taskPlacement, positioning));
                                            createdTaskCounter++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!SolverDistributionTask.freeWorkers.isEmpty()) {
                    SolverTask poll = SolverDistributionTask.freeWorkers.poll();
                    if (poll != null)
                        poll.solverTaskQueue.addAll(placements);
                    else solverTaskQueue.addAll(placements);
                } else
                    solverTaskQueue.addAll(placements);
            } else {
                if (!freeWorkers.contains(this))
                    freeWorkers.add(this);
            }
        }
        return null;
    }

    private boolean checkHasFuture(Frame frame) {

        // Check for condition that makes future impossible
        // No isolated single ball free

        if (frame.balls[0][0] == null && frame.balls[0][1] != null && frame.balls[1][0] != null)
            return false;

        if (frame.balls[WIDTH - 1][0] == null && frame.balls[WIDTH - 2][0] != null && frame.balls[WIDTH - 1][1] != null)
            return false;

        if (frame.balls[WIDTH - 1][HEIGHT - 1] == null && frame.balls[WIDTH - 2][HEIGHT - 1] != null && frame.balls[WIDTH - 1][HEIGHT - 2] != null)
            return false;

        if (frame.balls[0][HEIGHT - 1] == null && frame.balls[1][HEIGHT - 1] != null && frame.balls[0][HEIGHT - 2] != null)
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

        //Look for isolated pairs

        //pair horizontal
        for (int i = 1; i < WIDTH - 2; i++) {
            for (int j = 1; j < HEIGHT - 1; j++) {
                if (frame.balls[i][j] == null && frame.balls[i + 1][j] == null && frame.balls[i - 1][j] != null && frame.balls[i + 2][j] != null
                        && frame.balls[i][j - 1] != null && frame.balls[i + 1][j - 1] != null
                        && frame.balls[i][j + 1] != null && frame.balls[i + 1][j + 1] != null) {
                    return false;
                }
            }
        }

        //pair vertical
        for (int i = 1; i < WIDTH - 1; i++) {
            for (int j = 1; j < HEIGHT - 2; j++) {
                if (frame.balls[i][j] == null && frame.balls[i][j + 1] == null && frame.balls[i][j - 1] != null && frame.balls[i][j + 2] != null
                        && frame.balls[i - 1][j] != null && frame.balls[i - 1][j + 1] != null
                        && frame.balls[i + 1][j] != null && frame.balls[i + 1][j + 1] != null) {
                    return false;
                }
            }
        }

        // horizontal pair on long side
        for (int i = 1; i < WIDTH - 2; i++) {

            if (frame.balls[i][0] == null && frame.balls[i + 1][0] == null && frame.balls[i - 1][0] != null
                    && frame.balls[i][1] != null && frame.balls[i + 1][1] != null && frame.balls[i + 2][0] != null)
                return false;

            if (frame.balls[i][HEIGHT - 1] == null && frame.balls[i + 1][HEIGHT - 1] == null && frame.balls[i - 1][HEIGHT - 1] != null
                    && frame.balls[i][HEIGHT - 2] != null && frame.balls[i + 1][HEIGHT - 2] != null && frame.balls[i + 2][HEIGHT - 1] != null)
                return false;

        }
        // horizontal pair on short side
        for (int i = 1; i < HEIGHT - 1; i++) {

            if (frame.balls[0][i] == null && frame.balls[1][i] == null && frame.balls[0][i + 1] != null
                    && frame.balls[1][i + 1] != null && frame.balls[0][i - 1] != null && frame.balls[1][i - 1] != null && frame.balls[2][i] != null)
                return false;

            if (frame.balls[WIDTH - 1][i] == null && frame.balls[WIDTH - 2][i] == null && frame.balls[WIDTH - 1][i + 1] != null
                    && frame.balls[WIDTH - 2][i + 1] != null && frame.balls[WIDTH - 1][i - 1] != null && frame.balls[WIDTH - 2][i - 1] != null && frame.balls[WIDTH - 3][i] != null)
                return false;

        }

        //Vertical pair on long side
        for (int i = 1; i < WIDTH - 1; i++) {

            if (frame.balls[i][0] == null && frame.balls[i][1] == null && frame.balls[i - 1][0] != null
                    && frame.balls[i - 1][1] != null && frame.balls[i][2] != null && frame.balls[i + 1][1] != null && frame.balls[i + 1][0] != null)
                return false;

            if (frame.balls[i][HEIGHT - 1] == null && frame.balls[i][HEIGHT - 2] == null && frame.balls[i - 1][HEIGHT - 1] != null
                    && frame.balls[i - 1][HEIGHT - 2] != null && frame.balls[i][HEIGHT - 3] != null && frame.balls[i + 1][HEIGHT - 2] != null && frame.balls[i + 1][HEIGHT - 1] != null)
                return false;

        }

        //Vertical pair on short side
        for (int i = 1; i < HEIGHT - 2; i++) {

            if (frame.balls[0][i] == null && frame.balls[0][i + 1] == null && frame.balls[0][i - 1] != null
                    && frame.balls[1][i] != null && frame.balls[1][i + 1] != null && frame.balls[0][i + 2] != null)
                return false;

            if (frame.balls[WIDTH - 1][i] == null && frame.balls[WIDTH - 1][i + 1] == null && frame.balls[WIDTH - 1][i - 1] != null
                    && frame.balls[WIDTH - 2][i] != null && frame.balls[WIDTH - 2][i + 1] != null && frame.balls[WIDTH - 1][i + 2] != null)
                return false;
        }

        return true;
    }
}
