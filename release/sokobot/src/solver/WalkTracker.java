package solver;

public class WalkTracker {

    public final Coordinate cameFrom;
    public final char directionMoved;

    public WalkTracker(Coordinate cameFrom, char directionMoved) {
        this.cameFrom = cameFrom;
        this.directionMoved = directionMoved;
    }
}
