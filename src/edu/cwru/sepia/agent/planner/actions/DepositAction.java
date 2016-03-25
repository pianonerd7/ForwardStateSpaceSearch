package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.environment.model.state.ResourceType;

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

		GameState newState = new GameState(this, state, state.getPeasant(), state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState());

		ResourceType resourceType = newState.getPeasant().getHoldingObject();
		newState.getPeasant().resetNextTo();

		if (resourceType.equals("WOOD")) {
			newState.setMyWood(newState.getMyWood() + newState.getPeasant().getResourceQuantity());
		} else if (resourceType.equals("GOLD")) {
			newState.setMyGold(newState.getMyGold() + newState.getPeasant().getResourceQuantity());
		}

		newState.getPeasant().setResourceQuantity(0);
		newState.getPeasant().setHoldingObject(null);
		newState.getPeasant().setIsEmpty(true);
		newState.getPeasant().setNextToTownHall(true);
		return newState;
	}

	@Override
	public String getAction() {
		return "DEPOSIT";
	}
}