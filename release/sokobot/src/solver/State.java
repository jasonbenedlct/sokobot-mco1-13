package solver;

import java.util.*;

public class State implements Comparable<State> {
    public final Coordinate player;
    public final Set<Coordinate> crates;
    public final String path;
    public final State predecessor;
    public int h, g, f;

    public State(Coordinate player, Set<Coordinate> crates, String path, State pre, int h, int g) {
        this.player = player;
        this.crates = crates;
        this.path = path;
        this.predecessor = pre;
        this.h = h;
        this.g = g;
        this.f = g+h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof State)) return false;
        State other = (State) obj;

        // Two states are identical if the player position matches 
        // AND all boxes are in the exact same positions
        return this.player.equals(other.player) && this.crates.equals(other.crates);
    }

    @Override
    public int compareTo(State other) {
        return Integer.compare(this.f, other.f);
    }

}
