package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.MapObject;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction {

	private Peasant peasant;
	private MapObject mapObject;
	private Position moveToThisLocation;

	public MoveAction(Peasant peasant, MapObject mapObject, Position newPos) {
		this.peasant = peasant;
		this.mapObject = mapObject;
		this.moveToThisLocation = newPos;
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

		// only need to clone peasant because that's the only thing changing
		Peasant newPeasant = new Peasant(state.getPeasant().getHoldingObject(),
				state.getPeasant().getResourceQuantity(),
				new Position(state.getPeasant().getPosition().x, state.getPeasant().getPosition().y));

		GameState newState = new GameState(this, state, newPeasant, state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState());

		newState.getPeasant().setPosition(new Position(moveToThisLocation.x, moveToThisLocation.y));
		newState.getPeasant().resetNextTo();

		String type = mapObject.getName();

		switch (type) {
		case "FOREST":
			newState.getPeasant().setNextToForest(true);
			break;
		case "GOLDMINE":
			newState.getPeasant().setNextToGoldMine(true);
			break;
		case "TOWNHALL":
			newState.getPeasant().setNextToTownHall(true);
			break;
		}

		return newState;
	}

	@Override
	public String getAction() {
		return "MOVE";
	}

}
