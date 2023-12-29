package com.arianesline.iqpuzzle;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Part {
    final List<Ball> balls;
    final Color color;


    public Part(Color color) {

        this.color = color;
        balls = new ArrayList<>();
    }
}
