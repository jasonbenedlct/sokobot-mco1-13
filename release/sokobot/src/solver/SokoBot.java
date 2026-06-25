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

      int numTargets = targets.size();
      int[][][] pullDistances = new int[numTargets][height][width];
      for (int i = 0; i < numTargets; i++) {
          for (int r = 0; r < height; r++) {
              Arrays.fill(pullDistances[i][r], Integer.MAX_VALUE);
          }
      }

      int[] dx = {0, 0, -1, 1};
      int[] dy = {-1, 1, 0, 0};

      // Precompute pull-distances for each target using reverse BFS
      for (int i = 0; i < numTargets; i++) {
          Coordinate target = targets.get(i);
          Queue<Coordinate> queue = new LinkedList<>();
          pullDistances[i][target.y][target.x] = 0;
          queue.add(target);

          while (!queue.isEmpty()) {
              Coordinate curr = queue.poll();
              int currDist = pullDistances[i][curr.y][curr.x];

              for (int d = 0; d < 4; d++) {
                  int nextX = curr.x + dx[d];
                  int nextY = curr.y + dy[d];
                  int playerX = curr.x + 2 * dx[d];
                  int playerY = curr.y + 2 * dy[d];

                  if (nextX >= 0 && nextX < width && nextY >= 0 && nextY < height &&
                      playerX >= 0 && playerX < width && playerY >= 0 && playerY < height) {
                      
                      Coordinate nextCoord = new Coordinate(nextX, nextY);
                      Coordinate playerCoord = new Coordinate(playerX, playerY);

                      if (!walls.contains(nextCoord) && !walls.contains(playerCoord)) {
                          if (pullDistances[i][nextY][nextX] == Integer.MAX_VALUE) {
                              pullDistances[i][nextY][nextX] = currDist + 1;
                              queue.add(nextCoord);
                          }
                      }
                  }
              }
          }
      }

      PriorityQueue<State> frontier = new PriorityQueue<>();
      Set<State> explored = new HashSet<>();

      Coordinate canonicalPlayerStart = getCanonicalPlayer(playerStart, startingCrates, walls, width, height);
      int initialHeuristic = heuristic(startingCrates, pullDistances);
      if (initialHeuristic == -1) {
          return null;
      }
      
      State firstState = new State(playerStart, canonicalPlayerStart, startingCrates, "", null, initialHeuristic, 0);
      frontier.add(firstState);

      long startTime = System.currentTimeMillis();
      long timeLimit = 14500; // Leave 500ms safety margin

      while(!frontier.isEmpty()) {
        if (System.currentTimeMillis() - startTime > timeLimit) break;

        State current = frontier.poll();

        if (isGoal(current.crates, targets)) {
            return buildPath(current);
        }

        if (explored.contains(current)) continue;
        explored.add(current);

        List<State> successors = getSuccessors(current, walls, targets, pullDistances, width, height);
        
        for (State state : successors) {
          if (!explored.contains(state)) {
            frontier.add(state);
          }
        }
      }

      return null;
  }

  // Backtracks from the winning state to the start, joining transition segments in order
  private String buildPath(State endState) {
      List<String> pathSegments = new ArrayList<>();
      State current = endState;
      while (current.predecessor != null) {
          pathSegments.add(current.move);
          current = current.predecessor;
      }
      StringBuilder sb = new StringBuilder();
      for (int i = pathSegments.size() - 1; i >= 0; i--) {
          sb.append(pathSegments.get(i));
      }
      return sb.toString();
  }

  // Calculates the sum of minimum pull distances from each crate to its closest target
  private int heuristic(Set<Coordinate> crates, int[][][] pullDistances) {
    int total = 0;
    int numTargets = pullDistances.length;

    for (Coordinate crate: crates) {
      int minDistance = Integer.MAX_VALUE;

      for (int i = 0; i < numTargets; i++) {
        int dist = pullDistances[i][crate.y][crate.x];
        if (dist < minDistance) {
            minDistance = dist;
        }
      }
      
      if (minDistance == Integer.MAX_VALUE) {
          return -1; // Crate is in a deadlock zone (cannot reach any target)
      }
      total += minDistance;
    }

    return total;
  }

  private boolean isGoal(Set<Coordinate> crates, List<Coordinate> targets) {
    for (Coordinate target : targets) {
      if(!crates.contains(target)) return false;
    }
    return true;
  }

  // Dynamic 2x2 deadlock detection at the newly pushed crate position
  private boolean has2x2DeadlockAt(Coordinate crate, Set<Coordinate> crates, Set<Coordinate> walls, List<Coordinate> targets) {
      int cx = crate.x;
      int cy = crate.y;

      return isSolid2x2AndDeadlocked(cx - 1, cy - 1, crates, walls, targets) ||
             isSolid2x2AndDeadlocked(cx, cy - 1, crates, walls, targets) ||
             isSolid2x2AndDeadlocked(cx - 1, cy, crates, walls, targets) ||
             isSolid2x2AndDeadlocked(cx, cy, crates, walls, targets);
  }

  private boolean isSolid2x2AndDeadlocked(int x, int y, Set<Coordinate> crates, Set<Coordinate> walls, List<Coordinate> targets) {
      if (isSolid(x, y, crates, walls) &&
          isSolid(x + 1, y, crates, walls) &&
          isSolid(x, y + 1, crates, walls) &&
          isSolid(x + 1, y + 1, crates, walls)) {
          
          return isCrateAndNotTarget(x, y, crates, targets) ||
                 isCrateAndNotTarget(x + 1, y, crates, targets) ||
                 isCrateAndNotTarget(x, y + 1, crates, targets) ||
                 isCrateAndNotTarget(x + 1, y + 1, crates, targets);
      }
      return false;
  }

  private boolean isCrateAndNotTarget(int x, int y, Set<Coordinate> crates, List<Coordinate> targets) {
      Coordinate c = new Coordinate(x, y);
      return crates.contains(c) && !targets.contains(c);
  }
  
  private boolean isSolid(int x, int y, Set<Coordinate> crates, Set<Coordinate> walls) {
      Coordinate c = new Coordinate(x, y);
      return walls.contains(c) || crates.contains(c);
  }

  // Helper to find the canonical representative player coordinate for a component
  private Coordinate getCanonicalPlayer(Coordinate player, Set<Coordinate> crates, Set<Coordinate> walls, int width, int height) {
      Queue<Coordinate> queue = new LinkedList<>();
      Set<Coordinate> visited = new HashSet<>();
      queue.add(player);
      visited.add(player);
      
      Coordinate canonical = player;
      int[] dx = {0, 0, -1, 1};
      int[] dy = {-1, 1, 0, 0};
      
      while (!queue.isEmpty()) {
          Coordinate c = queue.poll();
          if (c.x < canonical.x || (c.x == canonical.x && c.y < canonical.y)) {
              canonical = c;
          }
          for (int d = 0; d < 4; d++) {
              int nx = c.x + dx[d];
              int ny = c.y + dy[d];
              Coordinate nextCoord = new Coordinate(nx, ny);
              if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                  if (!walls.contains(nextCoord) && !crates.contains(nextCoord)) {
                      if (visited.add(nextCoord)) {
                          queue.add(nextCoord);
                      }
                  }
              }
          }
      }
      return canonical;
  }

  public List<State> getSuccessors(State current, Set<Coordinate> walls, List<Coordinate> targets, int[][][] pullDistances, int width, int height) {
      List<State> successors = new ArrayList<>();
      
      int[] dx = {0, 0, -1, 1};
      int[] dy = {-1, 1, 0, 0};
      char[] dirChars = {'u', 'd', 'l', 'r'};

      // 1. Run BFS from current.player to find all reachable tiles and their shortest paths
      class BFSNode {
          Coordinate coord;
          BFSNode parent;
          char move;
          int dist;
          
          BFSNode(Coordinate coord, BFSNode parent, char move, int dist) {
              this.coord = coord;
              this.parent = parent;
              this.move = move;
              this.dist = dist;
          }
      }
      
      Queue<BFSNode> queue = new LinkedList<>();
      Map<Coordinate, BFSNode> visited = new HashMap<>();
      
      BFSNode startNode = new BFSNode(current.player, null, '\0', 0);
      queue.add(startNode);
      visited.put(current.player, startNode);
      
      while (!queue.isEmpty()) {
          BFSNode node = queue.poll();
          Coordinate c = node.coord;
          
          for (int d = 0; d < 4; d++) {
              int nx = c.x + dx[d];
              int ny = c.y + dy[d];
              Coordinate nextCoord = new Coordinate(nx, ny);
              
              if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                  if (!walls.contains(nextCoord) && !current.crates.contains(nextCoord)) {
                      if (!visited.containsKey(nextCoord)) {
                          BFSNode nextNode = new BFSNode(nextCoord, node, dirChars[d], node.dist + 1);
                          visited.put(nextCoord, nextNode);
                          queue.add(nextNode);
                      }
                  }
              }
          }
      }
      
      // 2. For each crate, check if the player can reach a position to push it
      for (Coordinate crate : current.crates) {
          for (int d = 0; d < 4; d++) {
              // The player must stand at (crate.x - dx, crate.y - dy) to push the crate in direction d
              int px = crate.x - dx[d];
              int py = crate.y - dy[d];
              Coordinate playerPushPos = new Coordinate(px, py);
              
              // The crate will move to (crate.x + dx, crate.y + dy)
              int cx = crate.x + dx[d];
              int cy = crate.y + dy[d];
              Coordinate crateDestPos = new Coordinate(cx, cy);
              
              if (visited.containsKey(playerPushPos)) {
                  // Crate can only be pushed to an empty space
                  if (!walls.contains(crateDestPos) && !current.crates.contains(crateDestPos)) {
                      
                      // Check deadlock zone (must be able to reach at least one target)
                      boolean isDeadlockZone = true;
                      for (int t = 0; t < pullDistances.length; t++) {
                          if (pullDistances[t][crateDestPos.y][crateDestPos.x] != Integer.MAX_VALUE) {
                              isDeadlockZone = false;
                              break;
                          }
                      }
                      if (isDeadlockZone) continue;
                      
                      // Create new crates set
                      Set<Coordinate> newCrates = new HashSet<>(current.crates);
                      newCrates.remove(crate);
                      newCrates.add(crateDestPos);
                      
                      // Check 2x2 dynamic deadlock
                      if (has2x2DeadlockAt(crateDestPos, newCrates, walls, targets)) continue;
                      
                      // Reconstruct player walk path to the push position
                      StringBuilder pathSb = new StringBuilder();
                      BFSNode currNode = visited.get(playerPushPos);
                      while (currNode.parent != null) {
                          pathSb.append(currNode.move);
                          currNode = currNode.parent;
                      }
                      String walkPath = pathSb.reverse().toString();
                      String transitionMoves = walkPath + dirChars[d];
                      int transitionCost = visited.get(playerPushPos).dist + 1;
                      
                      // After the push, the player is at the crate's old position
                      Coordinate newPlayerPos = crate;
                      Coordinate newCanonical = getCanonicalPlayer(newPlayerPos, newCrates, walls, width, height);
                      
                      int newG = current.g + transitionCost;
                      int newH = heuristic(newCrates, pullDistances);
                      if (newH == -1) continue;
                      
                      State nextState = new State(newPlayerPos, newCanonical, newCrates, transitionMoves, current, newH, newG);
                      successors.add(nextState);
                  }
              }
          }
      }
      
      return successors;
  }
}
