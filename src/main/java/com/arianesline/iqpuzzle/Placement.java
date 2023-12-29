package com.arianesline.iqpuzzle;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Placement {
    final List<Positioning> positioningList;
    final int id;

    final static AtomicInteger counter = new AtomicInteger(1000);

    public Placement(int id) {
        this.id = id;
        this.positioningList = new ArrayList<>();
    }

    public Placement(Placement placementOri, Positioning positioningAdded) {
        this.id = counter.incrementAndGet();

        this.positioningList = new ArrayList<>();
        placementOri.positioningList.forEach(positioning -> this.positioningList.add(new Positioning(positioning.part,
                positioning.posx, positioning.posy, positioning.orientation, positioning.flipState)));
        if (positioningAdded != null)
            this.positioningList.add(positioningAdded);

    }

    public void addPositioning(Positioning positioning) {
        positioningList.add(positioning);
    }


    public boolean containsPart(Part partSelected) {
        return positioningList.stream().map(positioning -> positioning.part).anyMatch(part -> part == partSelected);
    }

    public void removePart(Part partSelected) {
        var found = positioningList.stream().filter(positioning -> positioning.part == partSelected).findAny();
        found.ifPresent(positioningList::remove);
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
}
