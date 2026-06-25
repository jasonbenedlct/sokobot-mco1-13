package solver;

import java.util.*;

public class State implements Comparable<State> {
    public final Coordinate player;
    public final Coordinate canonicalPlayer;
    public final Set<Coordinate> crates;
    public final String move;
    public final State predecessor;
    public int h, g, f;

    public State(Coordinate player, Coordinate canonicalPlayer, Set<Coordinate> crates, String move, State pre, int h, int g) {
        this.player = player;
        this.canonicalPlayer = canonicalPlayer;
        this.crates = crates;
        this.move = move;
        this.predecessor = pre;
        this.h = h;
        this.g = g;
        this.f = g + h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof State)) return false;
        State other = (State) obj;
        return this.canonicalPlayer.equals(other.canonicalPlayer) && this.crates.equals(other.crates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canonicalPlayer, crates);
    }

    @Override
    public int compareTo(State other) {
        return Integer.compare(this.f, other.f);
    }
}
