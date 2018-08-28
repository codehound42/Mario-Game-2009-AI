package competition.cig.tickbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import competition.cig.tickbased.game.enemies.Mario;
import competition.cig.tickbased.game.world.LevelScene;

public class AStar {

	public boolean finishedNewRun = true;
	public WorldSimulater worldSimulater;
	int maxRightSeenSoFar;
	
	public AStar(WorldSimulater worldSimulater) {
		this.worldSimulater = worldSimulater;
	}

	public List<boolean[]> runAStar(long startTime) {
//		long startTime = System.currentTimeMillis();
		finishedNewRun = false;
		
		SearchNode start = worldSimulater.initialSearchNode;
		SearchNode goal = worldSimulater.goalSearchNode;
		maxRightSeenSoFar = (int) goal.x;
		SearchNode currentBest = start;
		
		// Set of nodes already explored
		Set<SearchNode> explored = new HashSet<SearchNode>();
		// Nodes yet to be explored
		PriorityQueue<SearchNode> frontier = new PriorityQueue<SearchNode>();
		Map<Integer, SearchNode> frontierMap = new HashMap<Integer, SearchNode>();

		// Initialization
		frontier.add(start);
		//frontierMap.put(start.hashCode(), start);
		start.gScore = 0;
		start.fScore = heuristicFunction(start, goal);
		
		int timeTaken = (int) (System.currentTimeMillis() - startTime);
		System.out.println("Time taken before search starts:" + timeTaken);
		
		// Continue exploring as long as there are states in the state space, which have not been visited, or until goal is reached
		while (!frontier.isEmpty() && timeTaken < TickBasedAStarAgent.MAX_ALLOWED_RUN_TIME) {
			SearchNode current = frontier.remove();
			//frontierMap.remove(current.hashCode());
			
//			if (((Node)current.state).x > 187.6) {
//				System.out.println("x > 187.6");
//			}

			// If goal is reached return solution path
			if (goalTest(current)) {
				finishedNewRun = true;
				return reconstructPath(current);
			}
			
			// If the searchNode has a higher x value than the previous ones then this will be the new currentBest
			if (current.x > currentBest.x) {
				currentBest = current;
			}
			
			// Current node has been explored
			explored.add(current);
			
			// Explore each neighbor of current node
			for (boolean[] action : actions(current)) {
				SearchNode child = childNode(current, action);
				
				// Cost of reaching child node
				double tentativeGScore = current.gScore + pathCost(current, child);
				
				//if (!explored.contains(child) && !frontierMap.containsKey(child.hashCode())) {
				if (!explored.contains(child) && !frontier.contains(child)) {
					insertChildNode(child, current, tentativeGScore, goal, frontier, frontierMap);
				//} else if (frontierMap.containsKey(child.hashCode()) && frontierMap.get(child.hashCode()).gScore > tentativeGScore) {
				} else if (frontier.contains(child)) {
					if (frontier.stream().filter(e -> e.equals(child)).findFirst().get().gScore > tentativeGScore) {
						// the path the child node gives rise to is better than the original node (and corresponding path) so add child node instead
						frontier.remove(child);
						//frontierMap.remove(child.hashCode());
						insertChildNode(child, current, tentativeGScore, goal, frontier, frontierMap);
					}
				}
			}
			
			timeTaken = (int) (System.currentTimeMillis() - startTime);
			//System.out.println("Time taken when search ends:" + timeTaken);
			System.out.println(current.toString());
		}
		
		// No solution exists or no solution was found in the given time.
		// Return the best route found so far.
		finishedNewRun = true;
		System.out.println("=== END OF A* SEARCH ===");
		return reconstructPath(currentBest);
	}
	
	/**
	 * Inserts a given child node into the frontier along with setting its parent, gScore and fScore
	 * @param child
	 * @param node
	 * @param tentativeGScore
	 * @param problem
	 * @param goal
	 * @param frontier
	 * @param frontierMap
	 */
	private void insertChildNode(SearchNode child, SearchNode node, double tentativeGScore,
										SearchNode goal, PriorityQueue<SearchNode> frontier, Map<Integer, SearchNode> frontierMap) {
		child.parent = node;
		child.gScore = tentativeGScore;
		child.fScore = child.gScore + heuristicFunction(child, goal);
		frontier.add(child);
		//frontierMap.put(child.hashCode(), child);
	}
	
	/**
	 * Constructs the solution path by retracing steps using parent links
	 * @param current
	 * @return solution path
	 */
	private List<boolean[]> reconstructPath(SearchNode current) {
		List<boolean[]> path = new ArrayList<boolean[]>();
		while (current.parent != null) {
			path.add(current.action);
			current = current.parent;
		}
		Collections.reverse(path);
		return path;
	}
	
	public List<boolean[]> actions(SearchNode node) {
		List<boolean[]> actions = new ArrayList<boolean[]>();
		
		actions.add(createAction(false, true, false, false, false));
		actions.add(createAction(true, false, false, false, false));
		actions.add(createAction(false, true, false, true, false));
		actions.add(createAction(true, false, false, true, false));
		
		// With speed keys enabled
		actions.add(createAction(false, true, false, false, true));
		actions.add(createAction(false, true, false, true, true));
		
		return actions;
	}

	/**
	 * Auxiliary method
	 * @param left
	 * @param right
	 * @param down
	 * @param jump
	 * @param speed
	 * @return
	 */
	private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean speed) {
		boolean[] action = new boolean[5];
		action[Mario.KEY_DOWN] = down;
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_SPEED] = speed;
		return action;
	}

	public SearchNode childNode(SearchNode parent, boolean[] action) {
		// TODO dont make more levelscenes. Instead manipulate the tentativeLevelscene by taking back-ups and advancing it in time.
		// Clone the levelScene and update it by executing the given action
		// TODO make sure cloning is done correctly (should all internal fields also be cloned themselves?)
		LevelScene levelScene = new LevelScene(parent.levelScene);
		worldSimulater.advanceWorldState(levelScene, action);
		
		SearchNode newNode = new SearchNode(parent, action, levelScene, parent.ticksInFuture + 1);
		worldSimulater.states.add(newNode);
		return newNode;
	}
	
	public double pathCost(SearchNode sn1, SearchNode sn2) {
		int tickDiff = sn2.ticksInFuture - sn1.ticksInFuture;
		if (tickDiff < 0) return 0;
		return tickDiff;
	}

	public double heuristicFunction(SearchNode searchNode, SearchNode goal) {
		float distToRightSideOfScreenTilePrecision = goal.x - searchNode.x;
		float distToRightSideOfScreenPixelPrecision = distToRightSideOfScreenTilePrecision * WorldSimulater.BLOCK_SIZE;
		
		float marioVelocity = searchNode.levelScene.mario.xa;
		if (marioVelocity < 0.5) marioVelocity = 0.5f;
		
		double numTicksToReachGoal = distToRightSideOfScreenPixelPrecision / marioVelocity;
		return numTicksToReachGoal;
	}

//	public double pathCost(SearchNode sn1, SearchNode sn2) {
//		return Math.sqrt(Math.pow((sn2.x - sn1.x), 2) + Math.pow((sn2.y - sn1.y), 2));
//	}
	
	/**
	 * Heuristic is the distance to the right side of the screen
	 */
//	public double heuristicFunction(SearchNode searchNode, SearchNode goal) {
//		float distToRightSideOfScreen = goal.x - searchNode.x;
//		return Math.max(0, distToRightSideOfScreen);
//	}

	public boolean goalTest(SearchNode node) {
		float distToRightSideOfScreen = node.x - maxRightSeenSoFar;
		if (distToRightSideOfScreen >= 0) {
			//maxRightSeenSoFar += node.levelScene.mario.x += WorldSimulater.SCREEN_WIDTH / 2;
			//maxRightSeenSoFar += distToRightSideOfScreen;
			return true;
		}
		return false;
	}

}