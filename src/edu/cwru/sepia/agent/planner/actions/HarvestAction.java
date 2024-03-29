package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;

import edu.cwru.sepia.agent.planner.Forest;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GoldMine;
import edu.cwru.sepia.agent.planner.MapObject;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class HarvestAction implements StripsAction {

	private Peasant peasant;
	private MapObject resource;

	public HarvestAction(Peasant peasant, MapObject resource) {
		this.peasant = peasant;
		this.resource = resource;
	}

	/**
	 * Checks to see if the peasant is able to harvest in the passed in game
	 * state
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		return resource.getPosition().isAdjacent(peasant.getPosition())
				&& (peasant.isNextToGoldMine() || peasant.isNextToForest()) && peasant.getIsEmpty();
	}

	/**
	 * Applies the harvest action to the passed in game state
	 */
	@Override
	public GameState apply(GameState state) {

		Position resourcePos = resource.getPosition();
		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();

		/**
		 * Finds the peasant that is to carry out the harvest action and changes
		 * the resource type its holding and the quantity
		 */
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

				if (resource.getName().equals("FOREST")) {
					newPeasant.setHoldingObject(ResourceType.WOOD);
					newPeasant.setIsEmpty(false);
					newPeasant.setResourceQuantity(100);
				} else if (resource.getName().equals("GOLDMINE")) {
					newPeasant.setHoldingObject(ResourceType.GOLD);
					newPeasant.setIsEmpty(false);
					newPeasant.setResourceQuantity(100);
				}

				newPeasants.add(newPeasant);
			}
		}

		// clones the forests
		ArrayList<Forest> newForests = new ArrayList<Forest>();

		for (Forest forest : state.getForests()) {
			Forest newForest = new Forest(false, forest.getResourceQuantity(),
					new Position(forest.getPosition().x, forest.getPosition().y));

			newForests.add(newForest);
		}

		// clones the goldmines
		ArrayList<GoldMine> newGoldMines = new ArrayList<GoldMine>();

		for (GoldMine goldmine : state.getGoldMines()) {
			GoldMine newGoldMine = new GoldMine(false, goldmine.getResourceQuantity(),
					new Position(goldmine.getPosition().x, goldmine.getPosition().y));

			newGoldMines.add(newGoldMine);
		}

		ArrayList<StripsAction> action = new ArrayList<StripsAction>();
		action.add(this);
		GameState newState = new GameState(action, state, newPeasants, newForests, newGoldMines, state.getTownHall(),
				state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(), state.getPlayerNum(),
				state.getState(), 1, state.getTotalWoodOnMap(), state.getTotalGoldOnMap(), state.isBuildPeasants(),
				state.getTotalFoodOnMap());

		// if we are harvesting from a forest, we want to deduct 100 from the
		// resource
		if (resource.getName().equals("FOREST")) {
			for (Forest forest : newState.getForests()) {
				if (forest.getPosition().x == resourcePos.x && forest.getPosition().y == resourcePos.y) {

					forest.setResourceQuantity(forest.getResourceQuantity() - 100);

					// If the amount of wood at that forest is less than 0, then
					// we don't consider it anymore
					if (forest.getResourceQuantity() <= 0) {
						ArrayList<Forest> newForest = newState.getForests();
						newForest.remove(forest);
						newState.setForests(newForest);
					}

					break;
				}
			}
		}

		// if we are harvesting from a goldmine, we want to deduct 100 from the
		// resource
		else if (resource.getName().equals("GOLDMINE")) {
			for (GoldMine goldmine : newState.getGoldMines()) {
				if (goldmine.getPosition().x == resourcePos.x && goldmine.getPosition().y == resourcePos.y) {

					goldmine.setResourceQuantity(goldmine.getResourceQuantity() - 100);

					// If the amount of wood at that forest is less than 0, then
					// we don't consider it anymore
					if (goldmine.getResourceQuantity() <= 0) {
						ArrayList<GoldMine> newGoldMine = newState.getGoldMines();
						newGoldMine.remove(goldmine);
						newState.setGoldMines(newGoldMine);
					}

					break;
				}
			}
		}

		return newState;
	}

	public Peasant getPeasant() {
		return peasant;
	}

	public MapObject getResource() {
		return resource;
	}

	public void setResource(MapObject resource) {
		this.resource = resource;
	}

	@Override
	public String getAction() {
		return "HARVEST";
	}

	public String toString() {
		return "[" + " PEASANT ID: " + this.peasant.getUnitID() + ", HARVEST from " + resource.getPosition().toString()
				+ "]";
	}

}
