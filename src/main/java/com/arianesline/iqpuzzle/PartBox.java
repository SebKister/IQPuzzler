package com.arianesline.iqpuzzle;

public class PartBox {
    int xmin, xmax, ymin, ymax;


    public PartBox() {
        xmin = xmax = ymin = ymax = 0;
    }

    public PartBox(Part part, Orientation orientation, FlipState flipState) {
        xmin = xmax = ymin = ymax = 0;

        for (Ball ball : part.balls) {
            int xFactor = (flipState == FlipState.FLIPPED) ? -1 : 1;
            int px, py;
            switch (orientation) {
                case UP -> {

                    px = ball.rpx * xFactor;
                    py = ball.rpy;
                }
                case RIGHT -> {
                    px = ball.rpy;
                    py = -ball.rpx * xFactor;
                }
                case LEFT -> {
                    px = -ball.rpy;
                    py = ball.rpx * xFactor;
                }
                case DOWN -> {
                    px = -ball.rpx * xFactor;
                    py = -ball.rpy;
                }
                default -> throw new IllegalStateException("Unexpected value: " + orientation);
            }

            if(px<xmin) xmin=px;
            if(px>xmax) xmax=px;
            if(py<ymin) ymin=py;
            if(py>ymax) ymax=py;

        }
    }
}
