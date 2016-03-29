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
			ArrayList<GameState> individualChildren = getChildren(peasants.get(i));

			prelimaryChildren.add(individualChildren);
		}

		return mergeChildren(prelimaryChildren);
	}

	private List<GameState> mergeChildren(List<ArrayList<GameState>> listChildren) {

		if (listChildren.size() == 1) {
			return listChildren.get(0);
		}

		List<GameState> children = new ArrayList<GameState>();

		if (listChildren.size() == 2) {

			for (GameState gamestate1 : listChildren.get(0)) {
				for (GameState gamestate2 : listChildren.get(1)) {
					if (gamestate1.getParentAction().get(0).getPeasant().getUnitID() == 0) {
						children.add(mergeState(gamestate1, gamestate2));
					} else {
						children.add(mergeState(gamestate1, gamestate2));
					}
				}
			}

			for (int i = 0; i < children.size(); i++) {
				Position p1 = children.get(i).getPeasants().get(0).getPosition();
				Position p2 = children.get(i).getPeasants().get(1).getPosition();
				if (p1.x == p2.x && p1.y == p2.y) {
					children.remove(i);
				}
			}
		}

		else if (listChildren.size() == 3) {

		}

		return children;
	}

	private GameState mergeState(GameState state1, GameState state2) {

		StripsAction action1 = state1.parentAction.get(0);
		StripsAction action2 = state2.parentAction.get(0);
		String action1Name = action1.getAction();
		String action2Name = action2.getAction();

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
			newCost += 1;

			HarvestAction harvest = (HarvestAction) action2;
			MapObject resource = harvest.getResource();

			if (resource.getName().equals("WOOD")) {
				for (Forest forest : newForests) {
					if (forest.getPosition().x == resource.getPosition().x
							&& forest.getPosition().y == resource.getPosition().y) {

						forest.setResourceQuantity(forest.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
						// we don't consider it anymore
						if (forest.getResourceQuantity() <= 0) {
							ArrayList<Forest> newForest = newState.getForests();
							newForest.remove(forest);
							newState.setForests(newForest);
						}
					}
				}
			}

			else if (resource.getName().equals("GOLDMINE")) {
				for (GoldMine goldmine : newState.getGoldMines()) {
					if (goldmine.getPosition().x == resource.getPosition().x
							&& goldmine.getPosition().y == resource.getPosition().y) {

						goldmine.setResourceQuantity(goldmine.getResourceQuantity() - 100);

						// If the amount of wood at that forest is less than 0,
						// then
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

			break;
		case ("DEPOSIT"):
			newCost += 1;

			DepositAction deposit = (DepositAction) action2;

			if (deposit.getPeasant().getHoldingObject().toString().equals("WOOD")) {
				newState.setMyWood(newState.getMyWood() + deposit.getPeasant().getResourceQuantity());
			} else if (deposit.getPeasant().getHoldingObject().toString().equals("GOLD")) {
				newState.setMyGold(newState.getMyGold() + deposit.getPeasant().getResourceQuantity());
			}
			break;

		case ("CREATE"):
			newCost += 1;

			ArrayList<Peasant> temp = state2.getPeasants();
			for (Peasant newP : newState.getPeasants()) {
				for (int j = 0; j < temp.size(); j++) {
					if (newP.getUnitID() == temp.get(j).getUnitID()) {
						temp.remove(j);
					}
				}
			}
			ArrayList<Peasant> peasants = newState.getPeasants();
			peasants.add(temp.get(0));
			newState.setPeasants(peasants);

			break;
		}

		return newState;
	}

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
		}

		// If a peasant is holding something, it should move towards the
		// TownHall, or deposit at TownHall
		if (!peasant.getIsEmpty()) {

			DepositAction deposit = new DepositAction(peasant);

			if (deposit.preconditionsMet(this)) {
				children.add(deposit.apply(this));
			}

			if (parentAction == null || actionOfInterest.getAction() != "MOVE") {
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

		ArrayList<StripsAction> action = parentAction;

		if (action.size() == 1) {
			String lastAction = action.get(0).getAction();

			if (parentState.parentAction == null) {

				return heuristic;
			}

			String ancestorAction = parentState.parentAction.get(0).getAction();

			switch (ancestorAction) {
			case "MOVE":
				switch (lastAction) {
				case "HARVEST":

					HarvestAction harvest = (HarvestAction) parentAction.get(0);

					if (harvest.getResource().getName().equals("FOREST")) {
						if (myWood <= goalWood) {
							heuristic += 200;
						}
						if (myWood > goalWood) {
							heuristic -= 1000;
						}
					}

					else if (harvest.getResource().getName().equals("GOLDMINE")) {
						if (myGold <= goalGold) {
							heuristic += 200;
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
					heuristic += 100;
					break;
				}
				break;

			case "DEPOSIT":
				switch (lastAction) {
				case "MOVE":

					MoveAction moveAction = (MoveAction) parentAction.get(0);
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
							heuristic += 200;
						}

						if (myGold > goalGold) {
							heuristic -= 1000;
						}
						break;

					case "CREATE":
						// heuristic += ((goalWood - myWood) / 100) * 10;
						// heuristic += ((goalGold - myGold) / 100) * 10;

						if ((this.getTotalFoodOnMap() - this.getPeasants().size()) > 0) {
							heuristic += 100;
						}

						if (this.getMyGold() >= 0) {
							heuristic += 400;
						}
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

					MoveAction moveAction = (MoveAction) parentAction.get(0);
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
							heuristic += 200;
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
		}

		if (action.size() == 2) {

		}

		if (action.size() == 3) {
			System.out.println("three actions");
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
		// return this.cost;
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
