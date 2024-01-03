package com.arianesline.iqpuzzle;

import java.util.ArrayDeque;
import java.util.Queue;

public class Part {
    final Queue<Ball> balls;
    final String color;
    boolean noFlip = false;

    public Part(String color) {

        this.color = color;
        balls = new ArrayDeque<>();
    }
}
