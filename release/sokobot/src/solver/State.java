package solver;

import java.util.*;

public class State implements Comparable<State> {
    public final Coordinate player;
    public final Set<Coordinate> crates;
    public final char move;
    public final State predecessor;
    public int h, g, f;

    public State(Coordinate player, Set<Coordinate> crates, char move, State pre, int h, int g) {
        this.player = player;
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
        return this.player.equals(other.player) && this.crates.equals(other.crates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, crates);
    }

    @Override
    public int compareTo(State other) {
        return Integer.compare(this.f, other.f);
    }
}
