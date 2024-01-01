package com.arianesline.iqpuzzle;

import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Queue;

public class Part {
    final Queue<Ball> balls;
    final Color color;
    boolean noFlip = false;

    public Part(Color color) {

        this.color = color;
        balls = new ArrayDeque<>();
    }
}
