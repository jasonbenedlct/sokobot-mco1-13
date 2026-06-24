package solver;

import java.util.*;

/*
TO-DO (REMOVE WHEN DONE!!)
string builder for path, not yet implemented
getsuccessors() method
deadlock checks (if moves are possible, if there's a wall behind the crate, if pushing the crate will push it to a wall, etc.)

 */


public class SokoBot {

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    /*
     * YOU NEED TO REWRITE THE IMPLEMENTATION OF THIS METHOD TO MAKE THE BOT SMARTER
     */

    /* TO-DO:
      Interpret mapData and itemsData, store into Hashset/List, whichever makes more sense
      
     */
      Set<Coordinate> walls = new HashSet<>();
      Set<Coordinate> startingCrates = new HashSet<>();
      List<Coordinate> targets = new ArrayList<>();

      Coordinate playerStart = null;

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

      int heuristic = heuristic(startingCrates, targets);
      State firstState = new State(playerStart, startingCrates, null, null, heuristic, 0);

      frontier.add(firstState);

      long startTime = System.currentTimeMillis();
      long timeLimit = 15000;


      while(!frontier.isEmpty()) {
        if (System.currentTimeMillis() - startTime > timeLimit) break;

        // gets lowest value state
        State current = frontier.poll();

        // checks if all crates are in goals, if true, return the path of the current state
        if (isGoal(current.crates, targets)) return current.path;

        explored.add(current);

        // gets successors (or all possible next states) of current state
        List<State> successors = getSuccessors(current, walls, targets);
        
        for (State state : successors) {
          if (!explored.contains(state)) {
            frontier.add(state);
          }
        }

      }

      return null;
    
  }

  private int heuristic(Set<Coordinate> crates, List<Coordinate> targets) {
    int total = 0;

    for (Coordinate crate: crates) {
      int minDistance = Integer.MAX_VALUE;

      for (Coordinate target: targets) {
        int distance = Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y);
        if (distance < minDistance) minDistance = distance;
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

  // TODO, gets all possible moves and returns the list of states from current state
  public List<State> getSuccessors(State current, Set<Coordinate> walls, List<Coordinate> targets) {
    
    return null;
  }

}
