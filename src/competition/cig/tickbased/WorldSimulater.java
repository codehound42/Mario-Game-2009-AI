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
	public int maxRightSeenSoFar = 15 * BLOCK_SIZE;
	public LevelScene worldScene;
	public LevelScene tentativeScene;
	public List<Sprite> sprites = new ArrayList<Sprite>();
	
	public Set<SearchNode> states = new HashSet<SearchNode>();
	public SearchNode initialSearchNode;
	public SearchNode goalSearchNode;
	public int timeUsed = 0;
	public final int MAX_ALLOWED_RUN_TIME = 42;
	
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
		byte[][] blockPositions = observation.getLevelSceneObservationZ(0);
    	float[] enemyPositions = observation.getEnemiesFloatPos();
		float[] marioPosition = observation.getMarioFloatPos();
		
		worldScene.mario.x = marioPosition[0];
		worldScene.mario.y = marioPosition[1];
		
		Mario mario = worldScene.mario;
		int marioXPos = (int) mario.x / 16; // block precision
		int marioYPos = (int) mario.y / 16; // block precision
		final int halfObservedWidth = 11;
		final int halfObservedHeight = 11;
		initialSearchNode.x = mario.x;
		initialSearchNode.y = mario.y;
		initialSearchNode.xa = mario.xa;
		initialSearchNode.ya = mario.ya;
		
		goalSearchNode.x = mario.x + halfObservedWidth * 16;
		
		// Blocks
		int obsY = 0;
		for (int y = marioYPos - halfObservedHeight; y < marioYPos + halfObservedHeight; y++) {
			int obsX = 0;
        	for (int x = marioXPos - halfObservedWidth; x < marioXPos + halfObservedWidth; x++) {
        		if (x >= 0 && x <= worldScene.level.xExit && y >= 0 && y < worldScene.level.yExit ) {
        			worldScene.level.setBlock(x, y, blockPositions[obsY][obsX]);
        			if (blockPositions[obsY][obsX] != 0) {
        				//System.out.println(blockPositions[obsY][obsX]);
        			}
        		} else {
        			//System.out.println("spring over");
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

	public void advanceWorldState(LevelScene levelScene, boolean[] action) {
		levelScene.mario.setKeys(action);
		levelScene.tick();
	}

}

