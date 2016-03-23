package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;

public class HarvestAction implements StripsAction {

	private Peasant peasant;

	public HarvestAction(Peasant peasant) {
		this.peasant = peasant;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return (peasant.isNextToGoldMine() || peasant.isNextToForest()) && peasant.getIsEmpty();
	}

	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}

}
