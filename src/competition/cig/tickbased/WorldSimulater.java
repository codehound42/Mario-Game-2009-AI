package competition.cig.tickbased;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.idsia.mario.environments.Environment;
import competition.cig.tickbased.game.enemies.Mario;
import competition.cig.tickbased.game.enemies.Sprite;
import competition.cig.tickbased.game.world.LevelScene;

public class WorldSimulater {

	public static final int BLOCK_SIZE = 16;
	public static final float MAX_RIGHT = 200 * BLOCK_SIZE;
	public static final int SCREEN_WIDTH = 22;
	public static final int SCREEN_HEIGHT = 15;
	public static final int HALF_OBSERVED_WIDTH = SCREEN_WIDTH / 2;
	public static final int HALF_OBSERVED_HEIGHT = SCREEN_WIDTH / 2; // Note: Observed height is 22 and not 15 eventhough the screen height is 15
	public static final int MARIO_START_X_POS = 2;
	
	public float maxRightCanSee;
	public LevelScene worldScene;
	public LevelScene tentativeScene;
	public List<Sprite> sprites = new ArrayList<Sprite>();
	
	public Set<SearchNode> states = new HashSet<SearchNode>();
	public SearchNode initialSearchNode, goalSearchNode;
	public int timeUsed = 0;
	
	public WorldSimulater() {
		LevelScene levelScene = new LevelScene();
		worldScene = tentativeScene = levelScene;
		
		initialSearchNode = new SearchNode(null, null, levelScene, 0);
		goalSearchNode = new SearchNode(null, null, levelScene, 0);
	}
	
	/**
	 * Updates the levelScene (world) from the latest observation by setting blocks and enemies (sprites)
	 * @param observation
	 */
	public void updateLevel(Environment observation) {
		// Observation input
		byte[][] blockPositions = observation.getLevelSceneObservationZ(0); // 22x22 array
    	float[] enemyPositions = observation.getEnemiesFloatPos();
		float[] marioPosition = observation.getMarioFloatPos();
		
		Mario mario = worldScene.mario;
		
		// Update mario in world state to match observed mario position
		worldScene.mario.x = marioPosition[0];
		worldScene.mario.y = marioPosition[1];
		
		int marioXPos = (int) mario.x / BLOCK_SIZE; // block precision
		int marioYPos = (int) mario.y / BLOCK_SIZE; // block precision
		
		// Setup initial and goal search nodes
		initialSearchNode.x = mario.x;
		initialSearchNode.y = mario.y;
		initialSearchNode.xa = mario.xa;
		initialSearchNode.ya = mario.ya;
		maxRightCanSee = mario.x + HALF_OBSERVED_WIDTH * BLOCK_SIZE;
		goalSearchNode.x = maxRightCanSee;
		
		// Blocks
		int obsY = 0;
		for (int y = marioYPos - HALF_OBSERVED_HEIGHT; y < marioYPos + HALF_OBSERVED_HEIGHT; y++) {
			int obsX = 0;
        	for (int x = marioXPos - HALF_OBSERVED_WIDTH; x < marioXPos + HALF_OBSERVED_WIDTH; x++) {
        		if (x >= 0 && x <= worldScene.level.xExit && y >= 0 && y < worldScene.level.yExit ) {
        			worldScene.level.setBlock(x, y, blockPositions[obsY][obsX]);
        		}
        		obsX++;
        	}
        	obsY++;
        }
		
		System.out.println("===END OF UPDATE LEVEL===");
		printLevelScene();
	}
	
	public void printLevelScene() {
		System.out.println(worldScene.level.toString());
	}

	/**
	 * Advances the world state of the input level scene based on the input action
	 * @param levelScene
	 * @param action
	 */
	public void advanceWorldState(LevelScene levelScene, boolean[] action) {
		levelScene.mario.setKeys(action);
		levelScene.tick();
	}

}

