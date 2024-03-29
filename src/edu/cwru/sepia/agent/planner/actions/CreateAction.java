package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class CreateAction implements StripsAction {

	private Peasant creatorPeasant;
	private Peasant createdPeasant;

	public CreateAction(Peasant creator) {
		this.creatorPeasant = creator;
	}

	/**
	 * Checks to see if the state passed in is capable of creating a new peasant
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		return state.isBuildPeasants() && state.getMyGold() >= 400
				&& ((state.getTotalFoodOnMap() - state.getPeasants().size()) > 0);
	}

	/**
	 * Creates a new peasant in the state passed in
	 */
	@Override
	public GameState apply(GameState state) {

		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();

		for (Peasant statePeasant : state.getPeasants()) {
			newPeasants.add(new Peasant(statePeasant));
		}

		// Puts the peasant at a location. This is approximate since we won't
		// know where SEPIA places the peasant
		for (Position pos : state.getTownHall().getPosition().getAdjacentPositions()) {

			if (pos.inBounds(state.getState().getXExtent(), state.getState().getYExtent())
					&& !state.getState().isResourceAt(pos.x, pos.y)) {
				this.createdPeasant = new Peasant(null, 0, new Position(pos.x, pos.y), state.getPeasants().size());
				newPeasants.add(createdPeasant);
				break;
			}
		}

		ArrayList<StripsAction> action = new ArrayList<StripsAction>();
		action.add(this);
		GameState newState = new GameState(action, state, newPeasants, state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState(), 1, state.getTotalWoodOnMap(), state.getTotalGoldOnMap(),
				state.isBuildPeasants(), state.getTotalFoodOnMap());

		// Creating a peasant costs 400 gold, so we deduct
		newState.setMyGold(newState.getMyGold() - 400);
		return newState;
	}

	@Override
	public String getAction() {
		return "CREATE";
	}

	public String toString() {
		return "[ PEASANT ID: " + this.creatorPeasant.getUnitID() + " BUILDING A PEASANT WITH ID: "
				+ createdPeasant.getUnitID() + "] \n";
	}

	@Override
	public Peasant getPeasant() {
		return creatorPeasant;
	}

	public Peasant getCreatedPeasant() {
		return createdPeasant;
	}

}
