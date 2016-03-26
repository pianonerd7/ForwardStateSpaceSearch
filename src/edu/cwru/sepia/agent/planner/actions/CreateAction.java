package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class CreateAction implements StripsAction {

	private Peasant peasant;

	@Override
	public boolean preconditionsMet(GameState state) {
		return state.isBuildPeasants() && state.getMyGold() >= 400
				&& ((state.getTotalFoodOnMap() - state.getPeasants().size()) >= 0);
	}

	@Override
	public GameState apply(GameState state) {

		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();

		for (Peasant statePeasant : state.getPeasants()) {
			if (statePeasant.getUnitID() == this.peasant.getUnitID()) {
				newPeasants.add(statePeasant);
			}
		}

		for (Position pos : state.getTownHall().getPosition().getAdjacentPositions()) {

			if (pos.inBounds(state.getState().getXExtent(), state.getState().getYExtent())
					&& !state.getState().isResourceAt(pos.x, pos.y)) {
				newPeasants.add(new Peasant(null, 0, new Position(pos.x, pos.y), state.getPeasants().size()));
			}
		}

		GameState newState = new GameState(this, state, newPeasants, state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState(), 1, state.getTotalWoodOnMap(), state.getTotalGoldOnMap(),
				state.isBuildPeasants(), state.getTotalFoodOnMap());

		return newState;
	}

	@Override
	public String getAction() {
		return "CREATE";
	}

	public String toString() {
		return "[BUILDING A PEASANT WITH ID: " + peasant.getUnitID() + "] \n";
	}

}
