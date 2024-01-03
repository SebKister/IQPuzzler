package com.arianesline.iqpuzzle;

import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.arianesline.iqpuzzle.SolverDistributionTask.*;

public class Core {
    public static final int WIDTH = 11;
    public static final int HEIGHT = 5;
    public final static Queue<Placement> challengePlacements = new ArrayDeque<>();
    public final static List<Part> parts = new ArrayList<>();
    public static Placement currentPlacement = new Placement(parts);
    public static ExecutorService distriExecutorService;
    public static SolverDistributionTask distributionTask;
    public static long startSolve;
    public static long endSolve;
    public static AtomicBoolean solving=new AtomicBoolean(false);

    public static void printResults() {
        System.out.println("Tasks : " + workers.stream().mapToLong(solverTask -> solverTask.createdTaskCounter).sum()
                + " - Duration : " + (((endSolve == 0) ? System.currentTimeMillis() : endSolve) - startSolve) / 1000.0);
        System.out.println("Solution : " + solutionCounter.get());
    }

    public static void buildParts() {

        //1
        var part = new Part(Color.YELLOW.toString());
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 0, 3));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

        //2
        part = new Part(Color.DARKBLUE.toString());
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 0));

        parts.add(part);

        //3
        part = new Part(Color.ORANGE.toString());
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 2, 1));
        part.balls.add(new Ball(part, 1, 2));

        parts.add(part);

        //4
        part = new Part(Color.SEAGREEN.toString());
        part.noFlip = true;
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

        //5
        part = new Part(Color.PINK.toString());
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 2));
        part.balls.add(new Ball(part, 1, 3));

        parts.add(part);

        //6
        part = new Part(Color.DARKRED.toString());
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 1, 2));

        parts.add(part);

        //7
        part = new Part(Color.DARKVIOLET.toString());
        part.noFlip = true;
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 1, 2));
        part.balls.add(new Ball(part, 2, 2));

        parts.add(part);

        //8
        part = new Part(Color.LIGHTGREEN.toString());
        part.noFlip = true;
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));
        part.balls.add(new Ball(part, 2, 1));
        part.balls.add(new Ball(part, 2, 0));

        parts.add(part);

        //9
        part = new Part(Color.LIGHTBLUE.toString());
        part.noFlip = true;
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

        //10
        part = new Part(Color.BLUE.toString());
        part.noFlip = true;
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 2));
        part.balls.add(new Ball(part, 2, 2));

        parts.add(part);

        //11
        part = new Part(Color.ORANGERED.toString());
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 0, 3));
        part.balls.add(new Ball(part, 1, 3));

        parts.add(part);

        //12
        part = new Part(Color.AQUAMARINE.toString());
        part.balls.add(new Ball(part, 0, 0));
        part.balls.add(new Ball(part, 0, 1));
        part.balls.add(new Ball(part, 0, 2));
        part.balls.add(new Ball(part, 1, 0));
        part.balls.add(new Ball(part, 1, 1));

        parts.add(part);

    }

    public static void buildChallenges() {
        var placement = new Placement(parts);

        placement.addPositioning(new Positioning(parts.get(0), 4, 4, Orientation.LEFT, FlipState.FLIPPED));
        placement.addPositioning(new Positioning(parts.get(4), 1, 1, Orientation.UP, FlipState.FLIPPED));
        placement.addPositioning(new Positioning(parts.get(9), 2, 0, Orientation.LEFT, FlipState.FLAT));
        placement.addPositioning(new Positioning(parts.get(3), 2, 1, Orientation.UP, FlipState.FLAT));
        placement.addPositioning(new Positioning(parts.get(5), 5, 0, Orientation.UP, FlipState.FLIPPED));
        placement.addPositioning(new Positioning(parts.get(2), 6, 2, Orientation.LEFT, FlipState.FLAT));
        placement.addPositioning(new Positioning(parts.get(8), 4, 0, Orientation.LEFT, FlipState.FLAT));
        //    placement.addPositioning(new Positioning(parts.get(11), 6, 0, Orientation.RIGHT, FlipState.FLIPPED));
        //   placement.addPositioning(new Positioning(parts.get(1), 6, 4, Orientation.RIGHT, FlipState.FLAT));
        challengePlacements.add(placement);

    }

    public static void initStatic() {
        MAXRUNNINGTASKS = Runtime.getRuntime().availableProcessors() - 2;
        buildParts();
        buildChallenges();
        currentPlacement = challengePlacements.poll();
        distriExecutorService = Executors.newSingleThreadExecutor();
    }

    public static void solve() {
        solving.set(true);
        startSolve = System.currentTimeMillis();
        endSolve = 0;
        solutionPlacements.clear();
        solutionCounter.set(0);

        distributionTask = new SolverDistributionTask(Core::printResults, currentPlacement);
        distriExecutorService.submit(distributionTask);
        while(solving.get()) Thread.yield();
    }


}
