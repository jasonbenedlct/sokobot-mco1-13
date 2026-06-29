package solver;

import java.util.*;

public class State implements Comparable<State> {
    public final Coordinate player;
    public final Coordinate canonicalPlayer;
    public final Set<Coordinate> boxes;
    public final char move;
    public final State predecessor;
    public int h;
    public int g;

    public State(Coordinate player, Coordinate canonicalPlayer, Set<Coordinate> boxes, char move, State pre, int g) {
        this.player = player;
        this.canonicalPlayer = canonicalPlayer;
        this.boxes = boxes;
        this.move = move;
        this.predecessor = pre;
        this.g = g;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof State)) return false;
        State other = (State) obj;
        return this.canonicalPlayer.equals(other.canonicalPlayer) && this.boxes.equals(other.boxes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canonicalPlayer, boxes);
    }

    @Override
    public int compareTo(State other) {
        // FIXED TO GREEDY: Sorts strictly by h (closest to goal), completely ignoring path cost g
        return Integer.compare(this.h, other.h);
    }
}