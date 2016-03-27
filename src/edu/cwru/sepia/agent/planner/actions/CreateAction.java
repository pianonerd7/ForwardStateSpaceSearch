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
			newPeasants.add(statePeasant);
		}

		for (Position pos : state.getTownHall().getPosition().getAdjacentPositions()) {

			if (pos.inBounds(state.getState().getXExtent(), state.getState().getYExtent())
					&& !state.getState().isResourceAt(pos.x, pos.y)) {
				this.peasant = new Peasant(null, 0, new Position(pos.x, pos.y), state.getPeasants().size());
				newPeasants.add(peasant);
			}
		}

		ArrayList<StripsAction> action = new ArrayList<StripsAction>();
		action.add(this);
		GameState newState = new GameState(action, state, newPeasants, state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState(), 1, state.getTotalWoodOnMap(), state.getTotalGoldOnMap(),
				state.isBuildPeasants(), state.getTotalFoodOnMap());

		return newState;
	}

	@Override
	public Peasant getPeasant() {
		return peasant;
	}

	@Override
	public String getAction() {
		return "CREATE";
	}

	public String toString() {
		return "[BUILDING A PEASANT WITH ID: " + peasant.getUnitID() + "] \n";
	}

}
