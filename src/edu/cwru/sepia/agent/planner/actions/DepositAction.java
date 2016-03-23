package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;

public class DepositAction implements StripsAction {

	private Peasant peasant;

	public DepositAction(Peasant peasant) {
		this.peasant = peasant;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return peasant.getResourceQuantity() > 0 && peasant.isNextToTownHall();
	}

	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}

}