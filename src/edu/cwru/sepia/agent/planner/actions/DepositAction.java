package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
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

		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();
		ResourceType resourceType = null;
		int resourceQuantity = 0;

		for (Peasant statePeasant : state.getPeasants()) {
			if (statePeasant.getUnitID() != this.peasant.getUnitID()) {
				newPeasants.add(statePeasant);
			} else {
				Peasant newPeasant = new Peasant(statePeasant.getHoldingObject(), statePeasant.getResourceQuantity(),
						new Position(statePeasant.getPosition().x, statePeasant.getPosition().y),
						statePeasant.getUnitID());
				newPeasant.setNextToForest(statePeasant.isNextToForest());
				newPeasant.setNextToGoldMine(statePeasant.isNextToGoldMine());
				newPeasant.setNextToTownHall(statePeasant.isNextToTownHall());

				resourceType = newPeasant.getHoldingObject();
				resourceQuantity = newPeasant.getResourceQuantity();
				newPeasant.resetNextTo();

				newPeasant.setResourceQuantity(0);
				newPeasant.setHoldingObject(null);
				newPeasant.setIsEmpty(true);
				newPeasant.setNextToTownHall(true);

				newPeasants.add(newPeasant);
			}
		}

		GameState newState = new GameState(this, state, newPeasants, state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState(), 1, state.getTotalWoodOnMap(), state.getTotalGoldOnMap(),
				state.isBuildPeasants());

		if (resourceType.toString().equals("WOOD")) {
			newState.setMyWood(newState.getMyWood() + resourceQuantity);
		} else if (resourceType.toString().equals("GOLD")) {
			newState.setMyGold(newState.getMyGold() + resourceQuantity);
		}

		return newState;
	}

	public Peasant getPeasant() {
		return peasant;
	}

	public void setPeasant(Peasant peasant) {
		this.peasant = peasant;
	}

	@Override
	public String getAction() {
		return "DEPOSIT";
	}

	public String toString() {
		return "[" + "DEPOSIT, I came from" + peasant.getPosition().toString() + "]";
	}
}