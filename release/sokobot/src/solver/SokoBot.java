package solver;

import java.util.*;

public class SokoBot {

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
      Set<Coordinate> walls = new HashSet<>();
      Set<Coordinate> startingCrates = new HashSet<>();
      List<Coordinate> targets = new ArrayList<>();
      Coordinate playerStart = null;

      // Parse the map
      for (int r = 0; r < height; r++) {
        for (int c = 0; c < width; c++) {
          if (mapData[r][c] == '#') walls.add(new Coordinate(c, r));
          if (mapData[r][c] == '.') targets.add(new Coordinate(c, r));
          if (itemsData[r][c] == '@') playerStart = new Coordinate(c, r);
          if (itemsData[r][c] == '$') startingCrates.add(new Coordinate(c, r));
        }
      }

      PriorityQueue<State> frontier = new PriorityQueue<>();
      Set<State> explored = new HashSet<>();

      int initialHeuristic = heuristic(startingCrates, targets);
      // Use null character for the starting state since no move was taken yet
      State firstState = new State(playerStart, startingCrates, '\0', null, initialHeuristic, 0);

      frontier.add(firstState);

      long startTime = System.currentTimeMillis();
      long timeLimit = 15000;

      while(!frontier.isEmpty()) {
        if (System.currentTimeMillis() - startTime > timeLimit) break;

        State current = frontier.poll();

        // If goal is found, trace back the predecessors to build the solution string
        if (isGoal(current.crates, targets)) {
            return buildPath(current);
        }

        // Skip if we've already evaluated this exact board state
        if (explored.contains(current)) continue;
        explored.add(current);

        List<State> successors = getSuccessors(current, walls, targets);
        
        for (State state : successors) {
          if (!explored.contains(state)) {
            frontier.add(state);
          }
        }
      }

      return null;
  }

  // Backtracks from the winning state to the start, collecting the moves
  private String buildPath(State endState) {
      StringBuilder sb = new StringBuilder();
      State current = endState;
      while (current.predecessor != null) {
          sb.append(current.move);
          current = current.predecessor;
      }
      return sb.reverse().toString();
  }

  private int heuristic(Set<Coordinate> crates, List<Coordinate> targets) {
    int total = 0;
    List<Coordinate> availableTargets = new ArrayList<>(targets);

    for (Coordinate crate: crates) {
      int minDistance = Integer.MAX_VALUE;
      Coordinate closestTarget = null;

      for (Coordinate target: availableTargets) {
        int distance = Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y);
        if (distance < minDistance) {
            minDistance = distance;
            closestTarget = target;
        }
      }
      
      if (closestTarget != null) {
          total += minDistance;
          availableTargets.remove(closestTarget); 
      }
    }

    return total;
  }

  private boolean isGoal(Set<Coordinate> crates, List<Coordinate> targets) {
    for (Coordinate target : targets) {
      if(!crates.contains(target)) return false;
    }
    return true;
  }

  private boolean isCornerDeadlock(Coordinate crate, Set<Coordinate> walls, List<Coordinate> targets) {
    if (targets.contains(crate)) return false;

    boolean wallUp = walls.contains(new Coordinate(crate.x, crate.y - 1));
    boolean wallDown = walls.contains(new Coordinate(crate.x, crate.y + 1));
    boolean wallLeft = walls.contains(new Coordinate(crate.x - 1, crate.y));
    boolean wallRight = walls.contains(new Coordinate(crate.x + 1, crate.y));

    // Deadlocked if blocked vertically and horizontally
    return (wallUp || wallDown) && (wallLeft || wallRight);
  }

  public List<State> getSuccessors(State current, Set<Coordinate> walls, List<Coordinate> targets) {
    List<State> successors = new ArrayList<>();
    
    int[] dx = {0, 0, -1, 1};
    int[] dy = {-1, 1, 0, 0};
    char[] dirChars = {'u', 'd', 'l', 'r'};

    for (int i = 0; i < 4; i++) {
      int newPx = current.player.x + dx[i];
      int newPy = current.player.y + dy[i];
      Coordinate newPlayerPos = new Coordinate(newPx, newPy);

      if (walls.contains(newPlayerPos)) continue;

      Set<Coordinate> newCrates = new HashSet<>(current.crates);

      if (newCrates.contains(newPlayerPos)) {
        int newCx = newPx + dx[i];
        int newCy = newPy + dy[i];
        Coordinate newCratePos = new Coordinate(newCx, newCy);

        if (walls.contains(newCratePos) || newCrates.contains(newCratePos)) continue; 
        if (isCornerDeadlock(newCratePos, walls, targets)) continue;

        newCrates.remove(newPlayerPos);
        newCrates.add(newCratePos);
      }

      int newG = current.g + 1;
      int newH = heuristic(newCrates, targets);

      State nextState = new State(newPlayerPos, newCrates, dirChars[i], current, newH, newG);
      successors.add(nextState);
    }

    return successors;
  }
}
