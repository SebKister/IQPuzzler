package com.arianesline.iqpuzzle;


import java.util.ArrayList;
import java.util.List;

public class Placement {
    final List<Positioning> positioningList;
    final List<Part> unusedParts;


    public Placement( List<Part> parts) {
        this.positioningList = new ArrayList<>();
        this.unusedParts = new ArrayList<>(parts);
    }

    public Placement(Placement placementOri, Positioning positioningAdded) {

        this.positioningList = new ArrayList<>(placementOri.positioningList);
        this.unusedParts = new ArrayList<>(placementOri.unusedParts);

        if (positioningAdded != null) {
            this.positioningList.add(positioningAdded);
            this.unusedParts.remove(positioningAdded.part);
        }
    }

    public void addPositioning(Positioning positioning) {
        positioningList.add(positioning);
        unusedParts.remove(positioning.part);
    }


    public boolean containsPart(Part partSelected) {
        return positioningList.stream().map(positioning -> positioning.part).anyMatch(part -> part == partSelected);
    }

    public void removePart(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(o -> {
            positioningList.remove(o);
            unusedParts.add(o.part);
        });
    }

    public void movePartLeft(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(positioning -> positioning.posx--);
    }

    public void movePartRight(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(positioning -> positioning.posx++);
    }

    public void movePartUp(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(positioning -> positioning.posy++);
    }

    public void movePartDown(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(positioning -> positioning.posy--);
    }

    public void rotatePart(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(positioning -> positioning.orientation = Orientation.values()[(positioning.orientation.ordinal() + 1) % Orientation.values().length]);
    }

    public void flipPart(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(positioning -> positioning.flipState = FlipState.values()[(positioning.flipState.ordinal() + 1) % FlipState.values().length]);

    }

    public void clear(List<Part> parts) {
        this.positioningList.clear();
        this.unusedParts.clear();
        this.unusedParts.addAll(parts);
    }
}
