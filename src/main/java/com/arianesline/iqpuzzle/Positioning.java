package com.arianesline.iqpuzzle;

public class Positioning {
    Part part;
    int posx,posy;
    Orientation orientation;
    FlipState flipState;

    public Positioning(Part part, int posx, int posy, Orientation orientation, FlipState flipState) {
        this.part = part;
        this.posx = posx;
        this.posy = posy;
        this.orientation = orientation;
        this.flipState = flipState;
    }
}
