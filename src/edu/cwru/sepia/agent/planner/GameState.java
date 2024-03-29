package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.agent.planner.actions.CreateAction;
import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 * This class is used to represent the state of the game after applying one of
 * the available actions. It will also track the A* specific information such as
 * the parent pointer and the cost and heuristic function. Remember that unlike
 * the path planning A* from the first assignment the cost of an action may be
 * more than 1. Specifically the cost of executing a compound action such as
 * move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2).
 * Implement the methods provided and add any other methods and member variables
 * you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState
 * in this class using whatever class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

	// The Action done to get to this state
	private ArrayList<StripsAction> parentAction = null;
	// The state of the parent prior to the parent Action
	private GameState parentState = null;

	private ArrayList<Peasant> peasants;
	private ArrayList<Forest> forests;
	private ArrayList<GoldMine> goldMines;
	private TownHall townHall;

	private int goalWood;
	private int goalGold;
	private int myWood = 0;
	private int myGold = 0;
	private int totalWoodOnMap = 0;
	private int totalGoldOnMap = 0;
	private boolean buildPeasants = false;

	private int totalFoodOnMap = 0;
	private int playerNum;
	private State.StateView state;

	private int myCost = 0;

	/**
	 * Construct a GameState from a stateview object. This is used to construct
	 * the initial search node. All other nodes should be constructed from the
	 * another constructor you create or by factory functions that you create.
	 *
	 * @param state
	 *            The current stateview at the time the plan is being created
	 * @param playernum
	 *            The player number of agent that is planning
	 * @param requiredGold
	 *            The goal amount of gold (e.g. 200 for the small scenario)
	 * @param requiredWood
	 *            The goal amount of wood (e.g. 200 for the small scenario)
	 * @param buildPeasants
	 *            True if the BuildPeasant action should be considered
	 */
	public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {

		this.state = state;
		this.playerNum = playernum;
		this.goalWood = requiredWood;
		this.goalGold = requiredGold;
		this.buildPeasants = buildPeasants;

		peasants = new ArrayList<Peasant>();
		forests = new ArrayList<Forest>();
		goldMines = new ArrayList<GoldMine>();

		// extracts the resources from the state
		for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {

			if (resource.getType().toString().equals("TREE")) {
				forests.add(new Forest(false, resource.getAmountRemaining(),
						new Position(resource.getXPosition(), resource.getYPosition())));

				totalWoodOnMap += resource.getAmountRemaining();

			} else if (resource.getType().toString().equals("GOLD_MINE")) {
				goldMines.add(new GoldMine(false, resource.getAmountRemaining(),
						new Position(resource.getXPosition(), resource.getYPosition())));

				totalGoldOnMap += resource.getAmountRemaining();
			}
		}

		// extracts the units from the state
		for (Unit.UnitView unit : state.getAllUnits()) {
			if (unit.getTemplateView().getName().toLowerCase().equals("peasant")) {
				this.peasants.add(new Peasant(null, 0, new Position(unit.getXPosition(), unit.getYPosition()),
						this.peasants.size()));
			}
			if (unit.getTemplateView().getName().toLowerCase().equals("townhall")) {
				this.townHall = new TownHall(true, unit, new Position(unit.getXPosition(), unit.getYPosition()));
			}
		}

		totalFoodOnMap = state.getSupplyCap(playernum);
	}

	public GameState(ArrayList<StripsAction> parentAction, GameState parentState, ArrayList<Peasant> peasant,
			ArrayList<Forest> forests, ArrayList<GoldMine> goldMines, TownHall townHall, int goalWood, int goalGold,
			int myWood, int myGold, int playerNum, State.StateView state, int costToState, int totalWoodOnMap,
			int totalGoldOnMap, boolean buildPeasants, int totalFoodOnMap) {

		this.parentAction = parentAction;
		this.parentState = parentState;
		this.peasants = peasant;
		this.forests = forests;
		this.goldMines = goldMines;
		this.townHall = townHall;
		this.goalWood = goalWood;
		this.goalGold = goalGold;
		this.myWood = myWood;
		this.myGold = myGold;
		this.playerNum = playerNum;
		this.state = state;
		this.myCost = costToState;
		this.totalWoodOnMap = totalWoodOnMap;
		this.totalGoldOnMap = totalGoldOnMap;
		this.buildPeasants = buildPeasants;
		this.totalFoodOnMap = totalFoodOnMap;
	}

	/**
	 * Unlike in the first A* assignment there are many possible goal states. As
	 * long as the wood and gold requirements are met the peasants can be at any
	 * location and the capacities of the resource locations can be anything.
	 * Use this function to check if the goal conditions are met and return true
	 * if they are.
	 *
	 * @return true if the goal conditions are met in this instance of game
	 *         state.
	 */
	public boolean isGoal() {
		return myWood >= goalWood && myGold >= goalGold;
	}

	/**
	 * The branching factor of this search graph are much higher than the
	 * planning. Generate all of the possible successor states and their
	 * associated actions in this method.
	 *
	 * @return A list of the possible successor states and their associated
	 *         actions
	 */
	public List<GameState> generateChildren() {

		List<ArrayList<GameState>> prelimaryChildren = new ArrayList<ArrayList<GameState>>();

		for (int i = 0; i < this.peasants.size(); i++) {
			ArrayList<GameState> children = getChildren(peasants.get(i));
			if (children != null) {
				prelimaryChildren.add(children);
			}
		}

		return mergeChildren(prelimaryChildren);
	}

	/**
	 * Once when there are more than 2 parent actions, we need to merge the
	 * children
	 * 
	 * @param listChildren
	 * @return
	 */
	private List<GameState> mergeChildren(List<ArrayList<GameState>> listChildren) {

		if (listChildren.size() == 1) {
			return listChildren.get(0);
		}

		List<GameState> children = new ArrayList<GameState>();

		if (listChildren.size() == 2) {

			for (GameState gamestate1 : listChildren.get(0)) {
				for (GameState gamestate2 : listChildren.get(1)) {

					GameState newChild = mergeState(gamestate1, gamestate2);
					if (newChild != null) {
						children.add(newChild);
					}
				}
			}

			for (int i = 0; i < children.size(); i++) {
				Position p1 = children.get(i).getPeasants().get(0).getPosition();
				Position p2 = children.get(i).getPeasants().get(1).getPosition();

				// to avoid SEPIA collision (which is full of bugs) we avoid
				// trying to go to the same place
				if (p1.x == p2.x && p1.y == p2.y) {

					if (children.size() > 1) {
						children.remove(i);
					}
				}
			}
		}

		else if (listChildren.size() == 3) {
			for (GameState gamestate1 : listChildren.get(0)) {
				for (GameState gamestate2 : listChildren.get(1)) {
					for (GameState gamestate3 : listChildren.get(2)) {

						GameState newChild = mergeState(gamestate1, gamestate2, gamestate3);
						if (newChild != null) {
							children.add(newChild);
						}
					}
				}
			}

			for (int i = 0; i < children.size(); i++) {
				Position p1 = children.get(i).getPeasants().get(0).getPosition();
				Position p2 = children.get(i).getPeasants().get(1).getPosition();
				Position p3 = children.get(i).getPeasants().get(2).getPosition();

				// To avoid collision, avoid going to the same place
				if ((p1.x == p2.x && p1.y == p2.y) || (p1.x == p3.x && p1.y == p3.y)
						|| (p3.x == p2.x && p3.y == p2.y)) {

					if (children.size() > 1) {
						children.remove(i);
					}
				}
			}
		}

		return children;

	}

	/**
	 * If action1 is to harvest from resource x, and action 2 and or action 3
	 * moves towards that resource (to harvest eventually), we need to make sure
	 * that resource has enough for 3 peasants
	 * 
	 * @param action1Name
	 * @param action2Name
	 * @param action3Name
	 * @param action1
	 * @param action2
	 * @param action3
	 * @return
	 */
	private boolean collisionCheck(String action1Name, String action2Name, String action3Name, StripsAction action1,
			StripsAction action2, StripsAction action3) {

		MapObject a1 = null;
		MapObject a2 = null;
		MapObject a3 = null;

		int r1 = 0;
		int r2 = 0;
		int r3 = 0;

		if (action1Name.toString().equals("HARVEST")) {
			HarvestAction harvest = (HarvestAction) action1;
			a1 = harvest.getResource();

			if (a1.getName().toString().equals("FOREST")) {
				Forest forest = (Forest) a1;

				for (Forest f : this.getForests()) {
					if (f.getPosition().x == forest.getPosition().x && f.getPosition().y == forest.getPosition().y) {
						r1 = f.getResourceQuantity();
						break;
					}
				}
			} else if (a1.getName().toString().equals("GOLDMINE")) {
				GoldMine goldmine = (GoldMine) a1;

				for (GoldMine g : this.getGoldMines()) {
					if (g.getPosition().x == goldmine.getPosition().x
							&& g.getPosition().y == goldmine.getPosition().y) {
						r1 = g.getResourceQuantity();
						break;
					}
				}
			}
		} else if (action1Name.toString().equals("MOVE")) {
			MoveAction move = (MoveAction) action1;
			a1 = move.getMapObject();

			if (a1.getName().toString().equals("FOREST")) {
				Forest forest = (Forest) a1;

				for (Forest f : this.getForests()) {
					if (f.getPosition().x == forest.getPosition().x && f.getPosition().y == forest.getPosition().y) {
						r1 = f.getResourceQuantity();
						break;
					}
				}
			} else if (a1.getName().toString().equals("GOLDMINE")) {
				GoldMine goldmine = (GoldMine) a1;

				for (GoldMine g : this.getGoldMines()) {
					if (g.getPosition().x == goldmine.getPosition().x
							&& g.getPosition().y == goldmine.getPosition().y) {
						r1 = g.getResourceQuantity();
						break;
					}
				}
			}
		} else if (action1Name.toString().equals("DEPOSIT")) {
			a1 = this.townHall;
		}

		if (action2Name.toString().equals("HARVEST")) {
			HarvestAction harvest = (HarvestAction) action2;
			a2 = harvest.getResource();

			if (a2.getName().toString().equals("FOREST")) {
				Forest forest = (Forest) a2;

				for (Forest f : this.getForests()) {
					if (f.getPosition().x == forest.getPosition().x && f.getPosition().y == forest.getPosition().y) {
						r2 = f.getResourceQuantity();
						break;
					}
				}
			} else if (a2.getName().toString().equals("GOLDMINE")) {
				GoldMine goldmine = (GoldMine) a2;

				for (GoldMine g : this.getGoldMines()) {
					if (g.getPosition().x == goldmine.getPosition().x
							&& g.getPosition().y == goldmine.getPosition().y) {
						r2 = g.getResourceQuantity();
						break;
					}
				}
			}
		} else if (action2Name.toString().equals("MOVE")) {
			MoveAction move = (MoveAction) action2;
			a2 = move.getMapObject();

			if (a2.getName().toString().equals("FOREST")) {
				Forest forest = (Forest) a2;

				for (Forest f : this.getForests()) {
					if (f.getPosition().x == forest.getPosition().x && f.getPosition().y == forest.getPosition().y) {
						r2 = f.getResourceQuantity();
						break;
					}
				}
			} else if (a2.getName().toString().equals("GOLDMINE")) {
				GoldMine goldmine = (GoldMine) a2;

				for (GoldMine g : this.getGoldMines()) {
					if (g.getPosition().x == goldmine.getPosition().x
							&& g.getPosition().y == goldmine.getPosition().y) {
						r2 = g.getResourceQuantity();
						break;
					}
				}
			}
		} else if (action2Name.toString().equals("DEPOSIT")) {
			a2 = this.townHall;
		}

		if (action3Name.toString().equals("HARVEST")) {
			HarvestAction harvest = (HarvestAction) action3;
			a3 = harvest.getResource();

			if (a3.getName().toString().equals("FOREST")) {
				Forest forest = (Forest) a3;

				for (Forest f : this.getForests()) {
					if (f.getPosition().x == forest.getPosition().x && f.getPosition().y == forest.getPosition().y) {
						r3 = f.getResourceQuantity();
						break;
					}
				}
			} else if (a3.getName().toString().equals("GOLDMINE")) {
				GoldMine goldmine = (GoldMine) a3;

				for (GoldMine g : this.getGoldMines()) {
					if (g.getPosition().x == goldmine.getPosition().x
							&& g.getPosition().y == goldmine.getPosition().y) {
						r3 = g.getResourceQuantity();
						break;
					}
				}
			}
		} else if (action3Name.toString().equals("MOVE")) {
			MoveAction move = (MoveAction) action3;
			a3 = move.getMapObject();

			if (a3.getName().toString().equals("FOREST")) {
				Forest forest = (Forest) a3;

				for (Forest f : this.getForests()) {
					if (f.getPosition().x == forest.getPosition().x && f.getPosition().y == forest.getPosition().y) {
						r3 = f.getResourceQuantity();
						break;
					}
				}
			} else if (a3.getName().toString().equals("GOLDMINE")) {
				GoldMine goldmine = (GoldMine) a3;

				for (GoldMine g : this.getGoldMines()) {
					if (g.getPosition().x == goldmine.getPosition().x
							&& g.getPosition().y == goldmine.getPosition().y) {
						r3 = g.getResourceQuantity();
						break;
					}
				}
			}
		} else if (action3Name.toString().equals("DEPOSIT")) {
			a3 = this.townHall;
		}

		// if 1, 2, 3 are the same
		// if 1, 2 are the same
		// if 2, 3 are the same
		// if 1, 3 are the same

		Position p1 = a1.getPosition();
		Position p2 = a2.getPosition();
		Position p3 = a3.getPosition();

		if (p1.x == p2.x && p1.y == p2.y && p2.x == p3.x && p2.y == p3.y) {
			if (!a1.getName().toString().equals("TOWNHALL") && r1 < 300) {
				return true;
			}
		} else if (p1.x == p2.x && p1.y == p2.y) {
			if (!a1.getName().toString().equals("TOWNHALL") && r1 < 200) {
				return true;
			}
		} else if (p2.x == p3.x && p2.y == p3.y) {
			if (!a2.getName().toString().equals("TOWNHALL") && r2 < 200) {
				return true;
			}
		} else if (p1.x == p3.x && p1.y == p3.y) {
			if (!a3.getName().toString().equals("TOWNHALL") && r3 < 200) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Merging the state of 3 actions into one.
	 * 
	 * @param state1
	 * @param state2
	 * @param state3
	 * @return
	 */
	private GameState mergeState(GameState state1, GameState state2, GameState state3) {
		StripsAction action1 = state1.parentAction.get(0);
		StripsAction action2 = state2.parentAction.get(0);
		StripsAction action3 = state3.parentAction.get(0);
		String action1Name = action1.getAction();
		String action2Name = action2.getAction();
		String action3Name = action3.getAction();
		Peasant peasant1 = null;
		Peasant peasant2 = null;
		Peasant peasant3 = null;

		if (collisionCheck(action1Name, action2Name, action3Name, action1, action2, action3)) {
			return null;
		}

		ArrayList<StripsAction> newAction = new ArrayList<StripsAction>();
		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();
		ArrayList<Forest> newForests = new ArrayList<Forest>();
		ArrayList<GoldMine> newGoldMines = new ArrayList<GoldMine>();
		TownHall newTownHall = state1.getTownHall();

		int newMyWood = state1.getMyWood();
		int newMyGold = state1.getMyGold();

		int newCost = state1.getMyCost();

		for (Peasant peasant : state1.peasants) {
			if (peasant.getUnitID() == action1.getPeasant().getUnitID()) {
				peasant1 = peasant;
			}
		}

		for (Peasant peasant : state2.peasants) {
			if (peasant.getUnitID() == action2.getPeasant().getUnitID()) {
				peasant2 = peasant;
			}
		}

		for (Peasant peasant : state3.peasants) {
			if (peasant.getUnitID() == action3.getPeasant().getUnitID()) {
				peasant3 = peasant;
			}
		}

		for (Forest forest : state1.getForests()) {
			newForests.add(forest);
		}

		for (GoldMine goldmine : state1.getGoldMines()) {
			newGoldMines.add(goldmine);
		}

		newAction.add(action1);
		newAction.add(action2);
		newAction.add(action3);
		newPeasants.add(peasant1);
		newPeasants.add(peasant2);
		newPeasants.add(peasant3);

		GameState newState = new GameState(newAction, this, newPeasants, newForests, newGoldMines, newTownHall,
				state1.getGoalWood(), state1.getGoalGold(), newMyWood, newMyGold, state1.getPlayerNum(),
				state1.getState(), newCost, state1.getTotalWoodOnMap(), state1.getTotalGoldOnMap(),
				state1.isBuildPeasants(), state1.getTotalFoodOnMap());

		switch (action2Name) {
		case ("MOVE"):
			newState.setMyCost(newState.getMyCost() + state2.getMyCost());
			break;
		case ("HARVEST"):
			newState.setMyCost(newState.getMyCost() + 1);

			HarvestAction harvest = (HarvestAction) action2;
			MapObject resource = harvest.getResource();

			if (resource.getName().equals("FOREST")) {
				for (int i = 0; i < newState.getForests().size(); i++) {
					Forest forest = newState.getForests().get(i);

					if (forest.getPosition().x == resource.getPosition().x
							&& forest.getPosition().y == resource.getPosition().y) {
						if (forest.getResourceQuantity() == 0) {
							return null;
						}

						forest.setResourceQuantity(forest.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
						// we don't consider it anymore
						if (forest.getResourceQuantity() <= 0) {
							ArrayList<Forest> newForest = newState.getForests();

							ArrayList<Forest> updateForest = new ArrayList<Forest>();
							for (Forest forests : newForest) {
								if (forests.getResourceQuantity() != 0) {
									updateForest.add(forests);
								}
							}
							newState.setForests(updateForest);
						}

						break;
					}
				}
			}

			else if (resource.getName().equals("GOLDMINE")) {
				for (int i = 0; i < newState.getGoldMines().size(); i++) {
					GoldMine goldmine = newState.getGoldMines().get(i);

					if (goldmine.getPosition().x == resource.getPosition().x
							&& goldmine.getPosition().y == resource.getPosition().y) {

						if (goldmine.getResourceQuantity() == 0) {
							return null;
						}

						goldmine.setResourceQuantity(goldmine.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
						// we don't consider it anymore
						if (goldmine.getResourceQuantity() <= 0) {
							ArrayList<GoldMine> newGoldMine = newState.getGoldMines();

							ArrayList<GoldMine> updateGoldMine = new ArrayList<GoldMine>();
							for (GoldMine goldmines : newGoldMine) {
								if (goldmines.getResourceQuantity() != 0) {
									updateGoldMine.add(goldmines);
								}
							}
							newState.setGoldMines(updateGoldMine);
						}

						break;
					}
				}
			}
			break;
		case ("DEPOSIT"):
			newState.setMyCost(newState.getMyCost() + 1);

			DepositAction deposit = (DepositAction) action2;

			if (deposit.getPeasant().getHoldingObject().toString().equals("WOOD")) {
				newState.setMyWood(newState.getMyWood() + deposit.getPeasant().getResourceQuantity());
			} else if (deposit.getPeasant().getHoldingObject().toString().equals("GOLD")) {
				newState.setMyGold(newState.getMyGold() + deposit.getPeasant().getResourceQuantity());
			}
			break;
		}

		switch (action3Name) {
		case ("MOVE"):
			newState.setMyCost(newState.getMyCost() + state3.getMyCost());
			break;
		case ("HARVEST"):
			newState.setMyCost(newState.getMyCost() + 1);

			HarvestAction harvest = (HarvestAction) action3;
			MapObject resource = harvest.getResource();

			if (resource.getName().equals("FOREST")) {
				for (int i = 0; i < newState.getForests().size(); i++) {
					Forest forest = newState.getForests().get(i);

					if (forest.getPosition().x == resource.getPosition().x
							&& forest.getPosition().y == resource.getPosition().y) {
						if (forest.getResourceQuantity() == 0) {
							return null;
						}

						forest.setResourceQuantity(forest.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
						// we don't consider it anymore
						if (forest.getResourceQuantity() <= 0) {
							ArrayList<Forest> newForest = newState.getForests();

							ArrayList<Forest> updateForest = new ArrayList<Forest>();
							for (Forest forests : newForest) {
								if (forests.getResourceQuantity() != 0) {
									updateForest.add(forests);
								}
							}
							newState.setForests(updateForest);
						}

						break;
					}
				}
			}

			else if (resource.getName().equals("GOLDMINE")) {
				for (int i = 0; i < newState.getGoldMines().size(); i++) {
					GoldMine goldmine = newState.getGoldMines().get(i);

					if (goldmine.getPosition().x == resource.getPosition().x
							&& goldmine.getPosition().y == resource.getPosition().y) {

						if (goldmine.getResourceQuantity() == 0) {
							return null;
						}

						goldmine.setResourceQuantity(goldmine.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
						// we don't consider it anymore
						if (goldmine.getResourceQuantity() <= 0) {
							ArrayList<GoldMine> newGoldMine = newState.getGoldMines();

							ArrayList<GoldMine> updateGoldMine = new ArrayList<GoldMine>();
							for (GoldMine goldmines : newGoldMine) {
								if (goldmines.getResourceQuantity() != 0) {
									updateGoldMine.add(goldmines);
								}
							}
							newState.setGoldMines(updateGoldMine);
						}
						break;
					}
				}
			}

			break;
		case ("DEPOSIT"):
			newState.setMyCost(newState.getMyCost() + 1);

			DepositAction deposit = (DepositAction) action3;

			if (deposit.getPeasant().getHoldingObject().toString().equals("WOOD")) {
				newState.setMyWood(newState.getMyWood() + deposit.getPeasant().getResourceQuantity());
			} else if (deposit.getPeasant().getHoldingObject().toString().equals("GOLD")) {
				newState.setMyGold(newState.getMyGold() + deposit.getPeasant().getResourceQuantity());
			}
			break;
		}

		return newState;
	}

	/**
	 * Merging the state of 2 actions into 1
	 * 
	 * @param state1
	 * @param state2
	 * @return
	 */
	private GameState mergeState(GameState state1, GameState state2) {

		StripsAction action1 = state1.parentAction.get(0);
		StripsAction action2 = state2.parentAction.get(0);
		String action1Name = action1.getAction();
		String action2Name = action2.getAction();

		if (action1Name.equals(action2Name) && action1Name.equals("CREATE")) {
			return null;
		}

		Peasant peasant1 = null;
		Peasant peasant2 = null;

		ArrayList<StripsAction> newAction = new ArrayList<StripsAction>();
		ArrayList<Peasant> newPeasants = new ArrayList<Peasant>();
		ArrayList<Forest> newForests = new ArrayList<Forest>();
		ArrayList<GoldMine> newGoldMines = new ArrayList<GoldMine>();
		TownHall newTownHall = state1.getTownHall();

		int newMyWood = state1.getMyWood();
		int newMyGold = state1.getMyGold();

		int newCost = state1.getMyCost();

		for (Peasant peasant : state1.peasants) {
			if (peasant.getUnitID() == action1.getPeasant().getUnitID()) {
				peasant1 = peasant;
			}
		}

		for (Peasant peasant : state2.peasants) {
			if (peasant.getUnitID() == action2.getPeasant().getUnitID()) {
				peasant2 = peasant;
			}
		}

		for (Forest forest : state1.getForests()) {
			newForests.add(forest);
		}

		for (GoldMine goldmine : state1.getGoldMines()) {
			newGoldMines.add(goldmine);
		}

		newAction.add(action1);
		newAction.add(action2);
		newPeasants.add(peasant1);
		newPeasants.add(peasant2);

		GameState newState = new GameState(newAction, this, newPeasants, newForests, newGoldMines, newTownHall,
				state1.getGoalWood(), state1.getGoalGold(), newMyWood, newMyGold, state1.getPlayerNum(),
				state1.getState(), newCost, state1.getTotalWoodOnMap(), state1.getTotalGoldOnMap(),
				state1.isBuildPeasants(), state1.getTotalFoodOnMap());

		switch (action2Name) {
		case ("MOVE"):
			newState.setMyCost(newState.getMyCost() + state2.getMyCost());
			break;
		case ("HARVEST"):
			newState.setMyCost(newState.getMyCost() + 1);

			HarvestAction harvest = (HarvestAction) action2;
			MapObject resource = harvest.getResource();

			if (resource.getName().equals("FOREST")) {
				for (int i = 0; i < newState.getForests().size(); i++) {
					Forest forest = newState.getForests().get(i);

					if (forest.getPosition().x == resource.getPosition().x
							&& forest.getPosition().y == resource.getPosition().y) {
						if (forest.getResourceQuantity() == 0) {
							return null;
						}

						forest.setResourceQuantity(forest.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
						// we don't consider it anymore
						if (forest.getResourceQuantity() <= 0) {
							ArrayList<Forest> newForest = newState.getForests();

							ArrayList<Forest> updateForest = new ArrayList<Forest>();
							for (Forest forests : newForest) {
								if (forests.getResourceQuantity() != 0) {
									updateForest.add(forests);
								}
							}
							newState.setForests(updateForest);
						}

						break;
					}
				}
			}

			else if (resource.getName().equals("GOLDMINE")) {
				for (int i = 0; i < newState.getGoldMines().size(); i++) {
					GoldMine goldmine = newState.getGoldMines().get(i);

					if (goldmine.getPosition().x == resource.getPosition().x
							&& goldmine.getPosition().y == resource.getPosition().y) {

						if (goldmine.getResourceQuantity() == 0) {
							return null;
						}

						goldmine.setResourceQuantity(goldmine.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
						// we don't consider it anymore
						if (goldmine.getResourceQuantity() <= 0) {
							ArrayList<GoldMine> newGoldMine = newState.getGoldMines();

							ArrayList<GoldMine> updateGoldMine = new ArrayList<GoldMine>();
							for (GoldMine goldmines : newGoldMine) {
								if (goldmines.getResourceQuantity() != 0) {
									updateGoldMine.add(goldmines);
								}
							}
							newState.setGoldMines(updateGoldMine);
						}

						break;
					}
				}
			}

			break;
		case ("DEPOSIT"):
			newState.setMyCost(newState.getMyCost() + 1);

			DepositAction deposit = (DepositAction) action2;

			if (deposit.getPeasant().getHoldingObject().toString().equals("WOOD")) {
				newState.setMyWood(newState.getMyWood() + deposit.getPeasant().getResourceQuantity());
			} else if (deposit.getPeasant().getHoldingObject().toString().equals("GOLD")) {
				newState.setMyGold(newState.getMyGold() + deposit.getPeasant().getResourceQuantity());
			}
			break;

		case ("CREATE"):

			if (newState.getPeasants().size() == newState.getTotalFoodOnMap()) {
				return null;
			}

			newState.setMyCost(newState.getMyCost() + 1);

			for (Peasant peasant : state2.getPeasants()) {
				if (peasant.getUnitID() != newState.getPeasants().get(0).getUnitID()
						&& peasant.getUnitID() != newState.getPeasants().get(1).getUnitID()) {
					newState.getPeasants().add(peasant);
				}
			}

			newState.setMyGold(newState.getMyGold() - 400);
			break;
		}

		return newState;
	}

	/**
	 * Gets all possible children of a peasant
	 * 
	 * @param peasant
	 * @return
	 */
	private ArrayList<GameState> getChildren(Peasant peasant) {

		ArrayList<GameState> children = new ArrayList<GameState>();

		StripsAction actionOfInterest = null;

		if (parentAction == null || parentAction.isEmpty() || parentAction.size() == 0) {

		} else {
			for (StripsAction action : parentAction) {
				if (peasant.getUnitID() == action.getPeasant().getUnitID()) {
					actionOfInterest = action;
				}

				else {
					if (action.getAction().equals("CREATE")) {
						actionOfInterest = action;
					}
				}
			}
		}
		// If peasant isn't holding anything, it should move to a resource, or
		// harvest at a resource
		if (peasant.getIsEmpty()) {
			for (GoldMine goldmine : goldMines) {
				HarvestAction harvest = new HarvestAction(peasant, goldmine);

				if (harvest.preconditionsMet(this)) {
					children.add(harvest.apply(this));
				}

				if (parentAction == null || actionOfInterest.getAction() != "MOVE") {
					MoveAction move = new MoveAction(peasant, goldmine, goldmine.getPosition());
					children.add(move.apply(this));
				}
			}

			for (Forest forest : forests) {
				HarvestAction harvest = new HarvestAction(peasant, forest);

				if (harvest.preconditionsMet(this)) {
					children.add(harvest.apply(this));
				}

				if (parentAction == null || actionOfInterest.getAction() != "MOVE") {
					MoveAction move = new MoveAction(peasant, forest, forest.getPosition());
					children.add(move.apply(this));
				}
			}
		}

		// If a peasant is holding something, it should move towards the
		// TownHall, or deposit at TownHall
		if (!peasant.getIsEmpty()) {

			DepositAction deposit = new DepositAction(peasant);

			if (deposit.preconditionsMet(this)) {
				children.add(deposit.apply(this));
			}

			if (parentAction == null || actionOfInterest == null || actionOfInterest.getAction() != "MOVE") {
				MoveAction move = new MoveAction(peasant, this.townHall, townHall.getPosition());
				children.add(move.apply(this));
			}
		}

		if (peasant.getPosition().isAdjacent(this.getTownHall().getPosition())) {

			CreateAction create = new CreateAction(peasant);

			if (create.preconditionsMet(this)) {
				children.add(create.apply(this));
			}
		}

		return children;
	}

	/**
	 * Write your heuristic function here. Remember this must be admissible for
	 * the properties of A* to hold. If you can come up with an easy way of
	 * computing a consistent heuristic that is even better, but not strictly
	 * necessary.
	 *
	 * Add a description here in your submission explaining your heuristic.
	 *
	 * @return The value estimated remaining cost to reach a goal state from
	 *         this state.
	 */
	public double heuristic() {
		double heuristic = 0;

		ArrayList<StripsAction> lastAction = parentAction;
		ArrayList<StripsAction> ancestorAction = this.getParentState().getParentAction();

		if (ancestorAction == null) {
			return heuristic;
		}

		if (lastAction.size() == 1) {
			heuristic += heuristicPerPeasant(lastAction.get(0), ancestorAction.get(0));
		}

		if (lastAction.size() == 2) {
			heuristic += 1000;
			StripsAction lAct1 = lastAction.get(0);
			StripsAction lAct2 = null;

			StripsAction aAct1 = ancestorAction.get(0);
			StripsAction aAct2 = null;

			if (lastAction.size() > 1) {
				lAct2 = lastAction.get(1);
			}
			if (ancestorAction.size() > 1) {
				aAct2 = ancestorAction.get(1);
			}

			if (lAct2 == null) {
				heuristic += 1000;
				return heuristic;
			}

			// This means that the ancestor just did a create action
			if (aAct2 == null) {
				heuristic += 1000;
				heuristic += heuristicPerPeasant(lAct1, aAct1);
				heuristic += heuristicPerPeasant(lAct2, aAct1);
			} else {
				heuristic += heuristicPerPeasant(lAct1, aAct1);
				heuristic += heuristicPerPeasant(lAct2, aAct2);
			}
		}

		if (lastAction.size() == 3) {
			heuristic += 2000;

			StripsAction lAct1 = lastAction.get(0);
			StripsAction lAct2 = lastAction.get(1);
			StripsAction lAct3 = lastAction.get(2);

			StripsAction aAct1 = ancestorAction.get(0);
			StripsAction aAct2 = null;
			StripsAction aAct3 = null;

			if (ancestorAction.size() > 1) {
				aAct2 = ancestorAction.get(1);
			}
			if (ancestorAction.size() > 2) {
				aAct3 = ancestorAction.get(2);
			}

			if (aAct2 == null) {
				heuristic += 2000;
				return heuristic;
			}

			// This means that the ancestor just did a create action
			if (aAct3 == null) {
				heuristic += 1000;
				if (lAct1.getAction().toString().equals("CREATE")) {
					heuristic += heuristicPerPeasant(lAct1, aAct1);
					heuristic += heuristicPerPeasant(lAct2, aAct2);
					heuristic += heuristicPerPeasant(lAct3, aAct1);
				} else {
					heuristic += heuristicPerPeasant(lAct1, aAct1);
					heuristic += heuristicPerPeasant(lAct2, aAct2);
					heuristic += heuristicPerPeasant(lAct3, aAct2);
				}
			} else {
				heuristic += heuristicPerPeasant(lAct1, aAct1);
				heuristic += heuristicPerPeasant(lAct2, aAct2);
				heuristic += heuristicPerPeasant(lAct3, aAct3);
			}
		}

		return heuristic;

	}

	/**
	 * The heuristic such that it prioritizes getting gold first since we need
	 * gold to build peasants and it is more advantageous to build peasants
	 * early on than later.
	 * 
	 * Also punishes the peasant if it tries to get more gold if we have already
	 * reached gold's goal. Likewise for wood.
	 * 
	 * @param parentAction
	 * @param ancestorsAction
	 * @return
	 */
	private double heuristicPerPeasant(StripsAction parentAction, StripsAction ancestorsAction) {

		double heuristic = 0;

		String lastAction = parentAction.getAction();
		String ancestorAction = ancestorsAction.getAction();

		switch (ancestorAction) {
		case "MOVE":
			switch (lastAction) {
			case "HARVEST":

				HarvestAction harvest = (HarvestAction) parentAction;

				if (harvest.getResource().getName().equals("FOREST")) {
					if (myWood < goalWood) {
						heuristic += 200;
					}
					if (myWood > goalWood) {
						heuristic -= 1000;
					}
				}

				else if (harvest.getResource().getName().equals("GOLDMINE")) {
					if (myGold < goalGold) {
						heuristic += 400;
					}

					if (myGold > goalGold) {
						heuristic -= 1000;
					}
				}

				break;

			case "DEPOSIT":
				heuristic += 100;
				break;
			}
			break;

		case "HARVEST":
			switch (lastAction) {
			case "MOVE":
				heuristic += 500;
				break;
			}
			break;

		case "DEPOSIT":
			switch (lastAction) {
			case "MOVE":

				MoveAction moveAction = (MoveAction) parentAction;
				String moveToResourceType = moveAction.getMapObject().getName();

				switch (moveToResourceType) {
				case "FOREST":

					if (myWood < goalWood) {
						heuristic += 200;
					}
					if (myWood > goalWood) {
						heuristic -= 1000;
					}
					break;
				case "GOLDMINE":

					if (myGold < goalGold) {
						heuristic += 400;
					}

					if (myGold > goalGold) {
						heuristic -= 1000;
					}
					break;

				case "CREATE":
					heuristic += (goalWood - myWood) * 10;
					heuristic += (goalGold - myGold) * 10;

					break;
				}
				break;
			case "CREATE":
				heuristic += 500;
				break;
			}
			break;
		case "CREATE":
			switch (lastAction) {
			case "MOVE":

				MoveAction moveAction = (MoveAction) parentAction;
				String moveToResourceType = moveAction.getMapObject().getName();

				switch (moveToResourceType) {
				case "FOREST":

					if (myWood < goalWood) {
						heuristic += 200;
					}
					if (myWood > goalWood) {
						heuristic -= 1000;
					}
					break;
				case "GOLDMINE":

					if (myGold < goalGold) {
						heuristic += 400;
					}

					if (myGold > goalGold) {
						heuristic -= 1000;
					}
					break;
				}
				break;
			}
			break;
		}

		return heuristic;
	}

	/**
	 *
	 * Write the function that computes the current cost to get to this node.
	 * This is combined with your heuristic to determine which actions/states
	 * are better to explore.
	 *
	 * @return The current cost to reach this goal
	 */
	public double getCost() {
		return this.myCost + heuristic();
	}

	/**
	 * This is necessary to use your state in the Java priority queue. See the
	 * official priority queue and Comparable interface documentation to learn
	 * how this function should work.
	 *
	 * @param o
	 *            The other game state to compare
	 * @return 1 if this state costs more than the other, 0 if equal, -1
	 *         otherwise
	 */
	@Override
	public int compareTo(GameState o) {
		// TODO: Implement me!
		return new Double(this.getCost()).compareTo(o.getCost());
	}

	/**
	 * This will be necessary to use the GameState as a key in a Set or Map.
	 *
	 * @param o
	 *            The game state to compare
	 * @return True if this state equals the other state, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {

		if (!o.getClass().equals(GameState.class)) {
			return false;
		}

		GameState otherState = (GameState) o;

		if (goalWood != otherState.getGoalWood() || goalGold != otherState.getGoalGold()
				|| myWood != otherState.getMyWood() || myGold != otherState.getMyGold()) {
			return false;
		}

		if (!parentAction.equals(otherState.getParentAction())) {
			return false;
		}
		return true;
	}

	/**
	 * This is necessary to use the GameState as a key in a HashSet or HashMap.
	 * Remember that if two objects are equal they should hash to the same
	 * value.
	 *
	 * @return An integer hashcode that is equal for equal states.
	 */
	@Override
	public int hashCode() {
		// TODO: Implement me!
		return 0;
	}

	public ArrayList<StripsAction> getParentAction() {
		return parentAction;
	}

	public void setParentAction(ArrayList<StripsAction> parentAction) {
		this.parentAction = parentAction;
	}

	public GameState getParentState() {
		return parentState;
	}

	public void setParentState(GameState parentState) {
		this.parentState = parentState;
	}

	public ArrayList<Peasant> getPeasants() {
		return peasants;
	}

	public void setPeasants(ArrayList<Peasant> peasants) {
		this.peasants = peasants;
	}

	public ArrayList<Forest> getForests() {
		return forests;
	}

	public void setForests(ArrayList<Forest> forests) {
		this.forests = forests;
	}

	public ArrayList<GoldMine> getGoldMines() {
		return goldMines;
	}

	public void setGoldMines(ArrayList<GoldMine> goldMines) {
		this.goldMines = goldMines;
	}

	public TownHall getTownHall() {
		return townHall;
	}

	public void setTownHall(TownHall townHall) {
		this.townHall = townHall;
	}

	public int getGoalWood() {
		return goalWood;
	}

	public void setGoalWood(int goalWood) {
		this.goalWood = goalWood;
	}

	public int getGoalGold() {
		return goalGold;
	}

	public void setGoalGold(int goalGold) {
		this.goalGold = goalGold;
	}

	public int getMyWood() {
		return myWood;
	}

	public void setMyWood(int myWood) {
		this.myWood = myWood;
	}

	public int getMyGold() {
		return myGold;
	}

	public void setMyGold(int myGold) {
		this.myGold = myGold;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public void setPlayerNum(int playerNum) {
		this.playerNum = playerNum;
	}

	public State.StateView getState() {
		return state;
	}

	public void setState(State.StateView state) {
		this.state = state;
	}

	public int getTotalWoodOnMap() {
		return totalWoodOnMap;
	}

	public void setTotalWoodOnMap(int totalWoodOnMap) {
		this.totalWoodOnMap = totalWoodOnMap;
	}

	public int getTotalGoldOnMap() {
		return totalGoldOnMap;
	}

	public void setTotalGoldOnMap(int totalGoldOnMap) {
		this.totalGoldOnMap = totalGoldOnMap;
	}

	public boolean isBuildPeasants() {
		return buildPeasants;
	}

	public int getTotalFoodOnMap() {
		return totalFoodOnMap;
	}

	public void setTotalFoodOnMap(int totalFoodOnMap) {
		this.totalFoodOnMap = totalFoodOnMap;
	}

	public void setMyCost(int cost) {
		this.myCost = cost;
	}

	public int getMyCost() {
		return this.myCost;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Peasant peasant : peasants) {
			sb.append("[PEASANT: " + peasant.getPosition().toString() + "] \n");
		}

		sb.append("[Action: " + this.parentAction.toString() + "] \n [HEURISTIC: " + heuristic() + "]");
		return sb.toString();
	}

}
