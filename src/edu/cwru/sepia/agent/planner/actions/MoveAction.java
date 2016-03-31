package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.MapObject;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction {

	private Peasant peasant;
	private MapObject mapObject;
	private Position moveToThisLocation;
	private Position bestPosition = null;

	public MoveAction(Peasant peasant, MapObject mapObject, Position newPos) {
		this.peasant = peasant;
		this.mapObject = mapObject;
		this.moveToThisLocation = newPos;
	}

	/**
	 * You should always be able to move. It may not be the action with the
	 * highest utility, but you always have the option to
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		return true;
	}

	/**
	 * Applies the move action to the game state and returns that state
	 */
	@Override
	public GameState apply(GameState state) {

		Position bestNeighbor = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);

		for (Position pos : moveToThisLocation.getAdjacentPositions()) {

			if (pos.inBounds(state.getState().getXExtent(), state.getState().getYExtent())
					&& !state.getState().isResourceAt(pos.x, pos.y)) {
				if (peasant.getPosition().euclideanDistance(pos) < peasant.getPosition()
						.euclideanDistance(bestNeighbor)) {
					bestNeighbor = new Position(pos.x, pos.y);
				}
			}
		}

		this.bestPosition = bestNeighbor;
		int cost = 0;

		// only need to clone peasant because that's the only thing changing
		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();

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

				newPeasant.setPosition(bestNeighbor);
				newPeasant.resetNextTo();

				String type = mapObject.getName();

				switch (type) {
				case "FOREST":
					newPeasant.setNextToForest(true);
					break;
				case "GOLDMINE":
					newPeasant.setNextToGoldMine(true);
					break;
				case "TOWNHALL":
					newPeasant.setNextToTownHall(true);
					break;
				}

				cost = (int) newPeasant.getPosition().euclideanDistance(bestNeighbor);

				newPeasants.add(newPeasant);
			}
		}

		ArrayList<StripsAction> action = new ArrayList<StripsAction>();
		action.add(this);
		GameState newState = new GameState(action, state, newPeasants, state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState(), cost, state.getTotalWoodOnMap(), state.getTotalGoldOnMap(),
				state.isBuildPeasants(), state.getTotalFoodOnMap());

		return newState;
	}

	public Peasant getPeasant() {
		return peasant;
	}

	public Position getBestPosition() {
		return bestPosition;
	}

	public void setBestPosition(Position position) {
		this.bestPosition = position;
	}

	@Override
	public String getAction() {
		return "MOVE";
	}

	public MapObject getMapObject() {
		return mapObject;
	}

	public void setMapObject(MapObject mapObject) {
		this.mapObject = mapObject;
	}

	public String toString() {
		return "[" + " PEASANT ID: " + this.peasant.getUnitID() + ", MOVE to " + this.bestPosition.toString() + "]";
	}
}
