package com.arianesline.iqpuzzle;

import javafx.scene.paint.Color;


public class Frame {
    final int width, height;
    final Ball[][] balls;


    static final Part bugPart = new Part(Color.BLACK.toString());
    static final Ball bugBall = new Ball(bugPart, 0, 0);

    static {
        bugPart.balls.add(bugBall);
    }

    public Frame(int width, int height) {
        this.width = width;
        this.height = height;
        balls = new Ball[width][height];
    }

    public void loadPlacement(Placement placement) {

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                balls[i][j] = null;
            }
        }

        placement.positioningList.forEach(positioning -> {
            positioning.part.balls.forEach(ball -> {
                int xFactor = (positioning.flipState == FlipState.FLIPPED) ? -1 : 1;
                int px, py;
                switch (positioning.orientation) {
                    case UP -> {

                        px = positioning.posx + ball.rpx * xFactor;
                        py = positioning.posy + ball.rpy;
                    }
                    case RIGHT -> {
                        px = positioning.posx + ball.rpy;
                        py = positioning.posy - ball.rpx * xFactor;
                    }
                    case LEFT -> {
                        px = positioning.posx - ball.rpy;
                        py = positioning.posy + ball.rpx * xFactor;
                    }
                    case DOWN -> {
                        px = positioning.posx - ball.rpx * xFactor;
                        py = positioning.posy - ball.rpy;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + positioning.orientation);
                }
                if (xInvalid(px) || yInvalid(py)) {
                    return;
                }
                if (balls[px][py] != null) {
                    balls[px][py] = bugBall;
                    return;
                }
                balls[px][py] = ball;
            });
        });
    }

    public boolean canAdd(Positioning positioning) {
        for (Ball ball : positioning.part.balls) {
            int xFactor = (positioning.flipState == FlipState.FLIPPED) ? -1 : 1;
            int px, py;
            switch (positioning.orientation) {
                case UP -> {

                    px = positioning.posx + ball.rpx * xFactor;
                    py = positioning.posy + ball.rpy;
                }
                case RIGHT -> {
                    px = positioning.posx + ball.rpy;
                    py = positioning.posy - ball.rpx * xFactor;
                }
                case LEFT -> {
                    px = positioning.posx - ball.rpy;
                    py = positioning.posy + ball.rpx * xFactor;
                }
                case DOWN -> {
                    px = positioning.posx - ball.rpx * xFactor;
                    py = positioning.posy - ball.rpy;
                }
                default -> throw new IllegalStateException("Unexpected value: " + positioning.orientation);
            }
            if (xInvalid(px) || yInvalid(py))
                return false;

            if (balls[px][py] != null) {
                return false;
            }
        }
        return true;
    }

    private boolean yInvalid(int i) {
        return (i < 0 || i >= height);

    }

    private boolean xInvalid(int i) {
        return (i < 0 || i >= width);
    }

    public boolean canAdd(Part part, int posx, int posy, Orientation orientation, FlipState flipState) {
        for (Ball ball : part.balls) {
            int xFactor = (flipState == FlipState.FLIPPED) ? -1 : 1;
            int px, py;
            switch (orientation) {
                case UP -> {

                    px = posx + ball.rpx * xFactor;
                    py = posy + ball.rpy;
                }
                case RIGHT -> {
                    px = posx + ball.rpy;
                    py = posy - ball.rpx * xFactor;
                }
                case LEFT -> {
                    px = posx - ball.rpy;
                    py = posy + ball.rpx * xFactor;
                }
                case DOWN -> {
                    px = posx - ball.rpx * xFactor;
                    py = posy - ball.rpy;
                }
                default -> throw new IllegalStateException("Unexpected value: " + orientation);
            }
            if (xInvalid(px) || yInvalid(py))
                return false;

            if (balls[px][py] != null) {
                return false;
            }
        }
        return true;
    }
}
