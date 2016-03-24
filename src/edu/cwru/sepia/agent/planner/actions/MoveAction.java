package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.MapObject;
import edu.cwru.sepia.agent.planner.Peasant;

public class MoveAction implements StripsAction {

	private Peasant peasant;
	private MapObject mapObject;

	public MoveAction(Peasant peasant, MapObject mapObject) {
		this.peasant = peasant;
		this.mapObject = mapObject;
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

		GameState newState = new GameState();

		return newState;
	}

	@Override
	public String getAction() {
		return "MOVE";
	}

}
