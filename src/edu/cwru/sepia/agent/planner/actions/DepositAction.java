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

	/**
	 * Checks to see if a deposit is possible for the passed in game state
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		return peasant.getResourceQuantity() > 0 && peasant.isNextToTownHall();
	}

	/**
	 * Applies the deposit action on the passed in game state
	 */
	@Override
	public GameState apply(GameState state) {

		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();
		ResourceType resourceType = null;
		int resourceQuantity = 0;

		// resets the peasant's fields once a deposit is made. The peasant will
		// no longer be holding a resource and will have a quantity of 0.
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

		ArrayList<StripsAction> action = new ArrayList<StripsAction>();
		action.add(this);
		GameState newState = new GameState(action, state, newPeasants, state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState(), 1, state.getTotalWoodOnMap(), state.getTotalGoldOnMap(),
				state.isBuildPeasants(), state.getTotalFoodOnMap());

		// Updates myWood and myGold by adding 100
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

	@Override
	public String getAction() {
		return "DEPOSIT";
	}

	public String toString() {
		return "[" + " PEASANT ID: " + this.peasant.getUnitID() + ", DEPOSIT " + peasant.getPosition().toString() + "]";
	}
}