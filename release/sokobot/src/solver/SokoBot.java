package solver;

import java.util.*;

public class SokoBot {

    private static final Set<Coordinate> walls = new HashSet<>();
    private static final Set<Coordinate> goals = new HashSet<>();

    private static final int[] dx = {0, 0, -1, 1};
    private static final int[] dy = {-1, 1, 0, 0};
    private static final char[] charDirection = {'u', 'd', 'l', 'r'};

    public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
        walls.clear();
        goals.clear();

        Coordinate initialPlayer = null;
        Set<Coordinate> initialBoxes = new HashSet<>();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (mapData[i][j] == '#') walls.add(new Coordinate(j, i));
                if (mapData[i][j] == '.') goals.add(new Coordinate(j, i));
                if (itemsData[i][j] == '@') initialPlayer = new Coordinate(j, i);
                if (itemsData[i][j] == '$') initialBoxes.add(new Coordinate(j, i));
            }
        }

        Coordinate initialCanonical = getCanonicalPlayer(initialPlayer, initialBoxes, width, height);
        return Search(initialPlayer, initialCanonical, initialBoxes, width, height);
    }

    public String Search(Coordinate startPlayer, Coordinate startCanonical, Set<Coordinate> initialBoxes, int width, int height) {
        PriorityQueue<State> frontier = new PriorityQueue<>();
        Set<State> visitedStates = new HashSet<>();

        State initialState = new State(startPlayer, startCanonical, initialBoxes, ' ', null, 0);
        initialState.h = heuristic(initialBoxes);
        frontier.add(initialState);

        while (!frontier.isEmpty()) {
            State current = frontier.poll();

            if (isPuzzleSolved(current.boxes)) {
                return reconstructFullPath(current);
            }

            if (visitedStates.contains(current)) continue;
            visitedStates.add(current);

            for (State successor : getSuccessors(current, width, height)) {
                if (visitedStates.contains(successor)) continue;

                successor.h = heuristic(successor.boxes);
                frontier.add(successor);
            }
        }

        return null;
    }

    public List<State> getSuccessors(State current, int width, int height) {
        List<State> successors = new ArrayList<>();
        Map<Coordinate, BFSNode> reachableZone = getReachableZones(current.player, current.boxes, width, height);

        for (Coordinate playerPos : reachableZone.keySet()) {
            for (int d = 0; d < 4; d++) {
                Coordinate cratePos = new Coordinate(playerPos.x + dx[d], playerPos.y + dy[d]);

                if (current.boxes.contains(cratePos)) {
                    Coordinate beyondCratePos = new Coordinate(cratePos.x + dx[d], cratePos.y + dy[d]);

                    if (!walls.contains(beyondCratePos) && !current.boxes.contains(beyondCratePos)) {
                        if (isDeadlockCorner(beyondCratePos)) continue;

                        int walkingDistance = reachableZone.get(playerPos).distance;
                        int pathCostForThisPush = walkingDistance + 1;

                        Set<Coordinate> updatedCrates = new HashSet<>(current.boxes);
                        updatedCrates.remove(cratePos);
                        updatedCrates.add(beyondCratePos);

                        Coordinate updatedCanonical = getCanonicalPlayer(cratePos, updatedCrates, width, height);

                        State nextState = new State(
                                cratePos,
                                updatedCanonical,
                                updatedCrates,
                                charDirection[d],
                                current,
                                current.g + pathCostForThisPush
                        );
                        successors.add(nextState);
                    }
                }
            }
        }
        return successors;
    }

    public Map<Coordinate, BFSNode> getReachableZones (Coordinate start, Set<Coordinate> crates, int width, int height) {
        Map<Coordinate, BFSNode> visited = new HashMap<>();
        Queue<Coordinate> queue = new LinkedList<>();

        BFSNode startNode = new BFSNode(0);
        visited.put(start, startNode);
        queue.add(start);

        while (!queue.isEmpty()) {
            Coordinate curr = queue.poll();
            int currentDistance = visited.get(curr).distance;

            for (int d = 0; d < 4; d++) {
                Coordinate neighbor = new Coordinate(curr.x + dx[d], curr.y + dy[d]);

                if (neighbor.x >= 0 && neighbor.x < width && neighbor.y >= 0 && neighbor.y < height) {
                    if (!walls.contains(neighbor) && !crates.contains(neighbor) && !visited.containsKey(neighbor)) {
                        visited.put(neighbor, new BFSNode(currentDistance + 1));
                        queue.add(neighbor);
                    }
                }
            }
        }
        return visited;
    }

    public int heuristic(Set <Coordinate> crates) {
        int heuristicValue = 0;
        for (Coordinate crate : crates) {
            int closestGoalDistance = Integer.MAX_VALUE;
            for (Coordinate goal : goals) {
                int dist = Math.abs(crate.x - goal.x) + Math.abs(crate.y - goal.y);
                if (dist < closestGoalDistance) {
                    closestGoalDistance = dist;
                }
            }
            heuristicValue += closestGoalDistance;
        }
        return heuristicValue;
    }

    public Coordinate getCanonicalPlayer(Coordinate start, Set<Coordinate> crates, int width, int height) {
        Map<Coordinate, BFSNode> zone = getReachableZones(start, crates, width, height);
        Coordinate canonical = start;

        for (Coordinate p : zone.keySet()) {
            if (p.y < canonical.y || (p.y == canonical.y && p.x < canonical.x)) {
                canonical = p;
            }
        }
        return canonical;
    }

    public boolean isPuzzleSolved(Set<Coordinate> crates) {
        for (Coordinate goal : goals) {
            if (!crates.contains(goal)) return false;
        }
        return true;
    }

    public boolean isDeadlockCorner(Coordinate targetTile) {
        if (goals.contains(targetTile)) return false;

        boolean up = walls.contains(new Coordinate(targetTile.x, targetTile.y - 1));
        boolean down = walls.contains(new Coordinate(targetTile.x, targetTile.y + 1));
        boolean left = walls.contains(new Coordinate(targetTile.x - 1, targetTile.y));
        boolean right = walls.contains(new Coordinate(targetTile.x + 1, targetTile.y));

        return (up && left) || (up && right) || (down && left) || (down && right);
    }

    public String reconstructFullPath(State finalState) {
        StringBuilder absolutePathSequence = new StringBuilder();
        State current = finalState;

        while (current != null && current.predecessor != null) {
            absolutePathSequence.append(current.move);

            Coordinate pushLocation = current.predecessor.player;
            Coordinate targetWalkDestination = new Coordinate(current.player.x - getXDirectionOffset(current.move), current.player.y - getYDirectionOffset(current.move));

            absolutePathSequence.append(traceWalkPathBetweenPoints(pushLocation, targetWalkDestination, current.predecessor.boxes));
            current = current.predecessor;
        }

        return absolutePathSequence.reverse().toString();
    }

    public String traceWalkPathBetweenPoints(Coordinate from, Coordinate to, Set<Coordinate> obstacles) {
        if (from.equals(to)) return "";

        Map<Coordinate, WalkTracker> trackerMap = new HashMap<>();
        Queue<Coordinate> walkQueue = new LinkedList<>();

        trackerMap.put(from, new WalkTracker(null, ' '));
        walkQueue.add(from);

        while (!walkQueue.isEmpty()) {
            Coordinate curr = walkQueue.poll();
            if (curr.equals(to)) break;

            for (int d = 0; d < 4; d++) {
                Coordinate neighbor = new Coordinate(curr.x + dx[d], curr.y + dy[d]);
                if (!walls.contains(neighbor) && !obstacles.contains(neighbor) && !trackerMap.containsKey(neighbor)) {
                    trackerMap.put(neighbor, new WalkTracker(curr, charDirection[d]));
                    walkQueue.add(neighbor);
                }
            }
        }

        StringBuilder route = new StringBuilder();
        Coordinate sweep = to;
        while (sweep != null && trackerMap.containsKey(sweep)) {
            WalkTracker steps = trackerMap.get(sweep);
            if (steps.directionMoved != ' ') {
                route.append(steps.directionMoved);
            }
            sweep = steps.cameFrom;
        }
        return route.toString();
    }

    public int getXDirectionOffset(char move) {
        if (move == 'l') return -1;
        if (move == 'r') return 1;
        return 0;
    }

    public int getYDirectionOffset(char move) {
        if (move == 'u') return -1;
        if (move == 'd') return 1;
        return 0;
    }

}