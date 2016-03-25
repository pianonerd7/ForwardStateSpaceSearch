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

	@Override
	public boolean preconditionsMet(GameState state) {
		return (peasant.isNextToGoldMine() || peasant.isNextToForest()) && peasant.getIsEmpty();
	}

	@Override
	public GameState apply(GameState state) {

		GameState newState = new GameState(this, state, state.getPeasant(), state.getForests(), state.getGoldMines(),
				state.getTownHall(), state.getGoalWood(), state.getGoalGold(), state.getMyWood(), state.getMyGold(),
				state.getPlayerNum(), state.getState());

		Position resourcePos = resource.getPosition();

		if (resource.getName().equals("FOREST")) {
			for (Forest forest : newState.getForests()) {
				if (forest.getPosition().x == resourcePos.x && forest.getPosition().y == resourcePos.y) {

					forest.setResourceQuantity(forest.getResourceQuantity() - 100);
					newState.getPeasant().setHoldingObject(ResourceType.WOOD);
					newState.getPeasant().setIsEmpty(false);
					newState.getPeasant().setResourceQuantity(100);

					// If the amount of wood at that forest is less than 0, then
					// we don't consider it anymore
					if (forest.getResourceQuantity() < 0) {
						ArrayList<Forest> newForests = newState.getForests();
						newForests.remove(forest);
						newState.setForests(newForests);
					}

					break;
				}
			}
		}

		else if (resource.getName().equals("GOLDMINE")) {
			for (GoldMine goldmine : newState.getGoldMines()) {
				if (goldmine.getPosition().x == resourcePos.x && goldmine.getPosition().y == resourcePos.y) {

					goldmine.setResourceQuantity(goldmine.getResourceQuantity() - 100);
					newState.getPeasant().setHoldingObject(ResourceType.GOLD);
					newState.getPeasant().setIsEmpty(false);
					newState.getPeasant().setResourceQuantity(100);

					// If the amount of wood at that forest is less than 0, then
					// we don't consider it anymore
					if (goldmine.getResourceQuantity() < 0) {
						ArrayList<Forest> newForests = newState.getForests();
						newForests.remove(goldmine);
						newState.setForests(newForests);
					}

					break;
				}
			}
		}

		return newState;
	}

	@Override
	public String getAction() {
		return "HARVEST";
	}

}
