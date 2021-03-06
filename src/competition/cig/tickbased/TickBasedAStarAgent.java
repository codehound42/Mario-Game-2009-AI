package competition.cig.tickbased;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class TickBasedAStarAgent implements Agent {
	private String name = "Tick based";
	private final boolean[] DEFAULT_ACTION = {false, false, false, false, false};
	private boolean[] action = new boolean[Environment.numberOfButtons];
	List<boolean[]> plan = new ArrayList<boolean[]>();
	WorldSimulater worldSimulater;
	AStar aStar;
	boolean doneSettingUp;
	
	private static final int NUM_TICKS_PLAN_AHEAD = 2;
	private static final int NUM_TICKS_BEFORE_REPLANNING = 0;
	private int ticksBeforeReplanning = NUM_TICKS_BEFORE_REPLANNING;
	
	public static final int MAX_ALLOWED_RUN_TIME = 100;

	public TickBasedAStarAgent() {
		reset();
	}

	/**
	 * Note to self: Main method called internally within the Mario game engine.
	 * Compute an action and return it in order to have it execute.
	 */
	public boolean[] getAction(Environment observation) {
		// Default action
		//action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = observation.mayMarioJump() || !observation.isMarioOnGround();
		//action[Mario.KEY_RIGHT] = true;
		if (!doneSettingUp) return DEFAULT_ACTION;
		
		long startTime = System.currentTimeMillis();
		worldSimulater.updateLevel(observation);
		
		if (plan.size() <= 0 || ticksBeforeReplanning < 0) {
			worldSimulater.states.clear();
			plan = aStar.runAStar(startTime);
			ticksBeforeReplanning = NUM_TICKS_BEFORE_REPLANNING;
		}
		
		if (plan == null || plan.size() == 0) {
	        return DEFAULT_ACTION; // empty action
		}
		
		ticksBeforeReplanning--;
		action = plan.remove(0);
		worldSimulater.advanceWorldState(worldSimulater.worldScene, action);
		return action;
	}
	
	public void reset() {
		action = new boolean[Environment.numberOfButtons];
		
		worldSimulater = new WorldSimulater();
		aStar = new AStar(worldSimulater);
		doneSettingUp = true;
	}

	public Agent.AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return name;
	}

	public void setName(String Name) {
		this.name = Name;
	}

}
