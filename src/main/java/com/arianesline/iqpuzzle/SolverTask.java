package com.arianesline.iqpuzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.arianesline.iqpuzzle.SolverDistributionTask.solutionCounter;
import static com.arianesline.iqpuzzle.SolverDistributionTask.*;


public class SolverTask implements Runnable {

    public static final Orientation[] ORIENTATIONS = Orientation.values();
    public static final FlipState[] FLIP_STATES = FlipState.values();
    public static final FlipState[] NOFLIP_STATES = new FlipState[]{FlipState.FLAT};
    final Frame frame = new Frame(Core.WIDTH, Core.HEIGHT);
    final List<Placement> placements = new ArrayList<>();
    public final ConcurrentLinkedDeque<Placement> solverTaskQueue = new ConcurrentLinkedDeque<>();

    public long createdTaskCounter;


    @Override
    public void run() {

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

                for (Part part : freeParts) {
                    for (Orientation orient : ORIENTATIONS) {
                        for (FlipState flipState : !part.noFlip ? FLIP_STATES : NOFLIP_STATES) {
                            var partBox = new PartBox(part, orient, flipState);
                            for (int i = -partBox.xmin; i < Core.WIDTH - partBox.xmax; i++) {
                                for (int j = -partBox.ymin; j < Core.HEIGHT - partBox.ymax; j++) {
                                    // Go over all empty cell
                                    if (frame.balls[i][j] == null) {
                                        if (frame.canAdd(part, i, j, orient, flipState)) {
                                            // Create new Task
                                            final Positioning positioning = new Positioning(part, i, j, orient, flipState);
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
    }

    private boolean checkHasFuture(Frame frame) {

        // Check for condition that makes future impossible
        // No isolated single ball free

        if (frame.balls[0][0] == null && frame.balls[0][1] != null && frame.balls[1][0] != null)
            return false;

        if (frame.balls[Core.WIDTH - 1][0] == null && frame.balls[Core.WIDTH - 2][0] != null && frame.balls[Core.WIDTH - 1][1] != null)
            return false;

        if (frame.balls[Core.WIDTH - 1][Core.HEIGHT - 1] == null && frame.balls[Core.WIDTH - 2][Core.HEIGHT - 1] != null && frame.balls[Core.WIDTH - 1][Core.HEIGHT - 2] != null)
            return false;

        if (frame.balls[0][Core.HEIGHT - 1] == null && frame.balls[1][Core.HEIGHT - 1] != null && frame.balls[0][Core.HEIGHT - 2] != null)
            return false;

        for (int i = 1; i < Core.WIDTH - 1; i++) {
            for (int j = 1; j < Core.HEIGHT - 1; j++) {
                if (frame.balls[i][j] == null && frame.balls[i - 1][j] != null && frame.balls[i + 1][j] != null && frame.balls[i][j - 1] != null && frame.balls[i][j + 1] != null) {
                    return false;
                }
            }
        }

        for (int i = 1; i < Core.WIDTH - 1; i++) {
            if (frame.balls[i][0] == null && frame.balls[i - 1][0] != null && frame.balls[i + 1][0] != null && frame.balls[i][1] != null)
                return false;
            if (frame.balls[i][Core.HEIGHT - 1] == null && frame.balls[i - 1][Core.HEIGHT - 1] != null && frame.balls[i + 1][Core.HEIGHT - 1] != null && frame.balls[i][Core.HEIGHT - 2] != null)
                return false;
        }

        for (int j = 1; j < Core.HEIGHT - 1; j++) {
            if (frame.balls[0][j] == null && frame.balls[0][j - 1] != null && frame.balls[0][j + 1] != null && frame.balls[1][j] != null)
                return false;

            if (frame.balls[Core.WIDTH - 1][j] == null && frame.balls[Core.WIDTH - 1][j - 1] != null && frame.balls[Core.WIDTH - 1][j + 1] != null && frame.balls[Core.WIDTH - 2][j] != null)
                return false;
        }

        //Look for isolated pairs

        //pair horizontal
        for (int i = 1; i < Core.WIDTH - 2; i++) {
            for (int j = 1; j < Core.HEIGHT - 1; j++) {
                if (frame.balls[i][j] == null && frame.balls[i + 1][j] == null && frame.balls[i - 1][j] != null && frame.balls[i + 2][j] != null
                        && frame.balls[i][j - 1] != null && frame.balls[i + 1][j - 1] != null
                        && frame.balls[i][j + 1] != null && frame.balls[i + 1][j + 1] != null) {
                    return false;
                }
            }
        }

        //pair vertical
        for (int i = 1; i < Core.WIDTH - 1; i++) {
            for (int j = 1; j < Core.HEIGHT - 2; j++) {
                if (frame.balls[i][j] == null && frame.balls[i][j + 1] == null && frame.balls[i][j - 1] != null && frame.balls[i][j + 2] != null
                        && frame.balls[i - 1][j] != null && frame.balls[i - 1][j + 1] != null
                        && frame.balls[i + 1][j] != null && frame.balls[i + 1][j + 1] != null) {
                    return false;
                }
            }
        }

        // horizontal pair on long side
        for (int i = 1; i < Core.WIDTH - 2; i++) {

            if (frame.balls[i][0] == null && frame.balls[i + 1][0] == null && frame.balls[i - 1][0] != null
                    && frame.balls[i][1] != null && frame.balls[i + 1][1] != null && frame.balls[i + 2][0] != null)
                return false;

            if (frame.balls[i][Core.HEIGHT - 1] == null && frame.balls[i + 1][Core.HEIGHT - 1] == null && frame.balls[i - 1][Core.HEIGHT - 1] != null
                    && frame.balls[i][Core.HEIGHT - 2] != null && frame.balls[i + 1][Core.HEIGHT - 2] != null && frame.balls[i + 2][Core.HEIGHT - 1] != null)
                return false;

        }
        // horizontal pair on short side
        for (int i = 1; i < Core.HEIGHT - 1; i++) {

            if (frame.balls[0][i] == null && frame.balls[1][i] == null && frame.balls[0][i + 1] != null
                    && frame.balls[1][i + 1] != null && frame.balls[0][i - 1] != null && frame.balls[1][i - 1] != null && frame.balls[2][i] != null)
                return false;

            if (frame.balls[Core.WIDTH - 1][i] == null && frame.balls[Core.WIDTH - 2][i] == null && frame.balls[Core.WIDTH - 1][i + 1] != null
                    && frame.balls[Core.WIDTH - 2][i + 1] != null && frame.balls[Core.WIDTH - 1][i - 1] != null && frame.balls[Core.WIDTH - 2][i - 1] != null && frame.balls[Core.WIDTH - 3][i] != null)
                return false;

        }

        //Vertical pair on long side
        for (int i = 1; i < Core.WIDTH - 1; i++) {

            if (frame.balls[i][0] == null && frame.balls[i][1] == null && frame.balls[i - 1][0] != null
                    && frame.balls[i - 1][1] != null && frame.balls[i][2] != null && frame.balls[i + 1][1] != null && frame.balls[i + 1][0] != null)
                return false;

            if (frame.balls[i][Core.HEIGHT - 1] == null && frame.balls[i][Core.HEIGHT - 2] == null && frame.balls[i - 1][Core.HEIGHT - 1] != null
                    && frame.balls[i - 1][Core.HEIGHT - 2] != null && frame.balls[i][Core.HEIGHT - 3] != null && frame.balls[i + 1][Core.HEIGHT - 2] != null && frame.balls[i + 1][Core.HEIGHT - 1] != null)
                return false;

        }

        //Vertical pair on short side
        for (int i = 1; i < Core.HEIGHT - 2; i++) {

            if (frame.balls[0][i] == null && frame.balls[0][i + 1] == null && frame.balls[0][i - 1] != null
                    && frame.balls[1][i] != null && frame.balls[1][i + 1] != null && frame.balls[0][i + 2] != null)
                return false;

            if (frame.balls[Core.WIDTH - 1][i] == null && frame.balls[Core.WIDTH - 1][i + 1] == null && frame.balls[Core.WIDTH - 1][i - 1] != null
                    && frame.balls[Core.WIDTH - 2][i] != null && frame.balls[Core.WIDTH - 2][i + 1] != null && frame.balls[Core.WIDTH - 1][i + 2] != null)
                return false;
        }

        return true;
    }
}
