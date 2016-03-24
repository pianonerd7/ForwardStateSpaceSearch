package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.Unit;

public class MoveAction implements StripsAction {

	private Peasant peasant;
	private Unit.UnitView townHall;
	private ResourceNode.ResourceView resource;

	public MoveAction(Peasant peasant, Unit.UnitView townHall) {
		this.peasant = peasant;
		this.townHall = townHall;
	}

	public MoveAction(Peasant peasant, ResourceNode.ResourceView resource) {
		this.peasant = peasant;
		this.resource = resource;
	}

	/**
	 * You should always be able to move. It may not be the move with the
	 * highest utility, but you always have the option to
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		return true;
	}

	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}

}
