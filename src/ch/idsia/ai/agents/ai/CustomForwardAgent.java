package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class CustomForwardAgent extends BasicAIAgent implements Agent {

	public CustomForwardAgent() {
		super("CustomForwardAgent");
		reset();
	}
	
	public boolean[] getAction(Environment observation) {
		action[Mario.KEY_RIGHT] = true;
		return action;
		
	}

}
