package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.List;

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
	private StripsAction parentAction = null;
	// The state of the parent prior to the parent Action
	private GameState parentState = null;

	private Peasant peasant;
	private ArrayList<Forest> forests;
	private ArrayList<GoldMine> goldMines;
	private TownHall townHall;

	private int goalWood;
	private int goalGold;
	private int myWood = 0;
	private int myGold = 0;

	private int playerNum;
	private State.StateView state;

	private int cost = 0;

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

		forests = new ArrayList<Forest>();
		goldMines = new ArrayList<GoldMine>();

		for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {

			if (resource.getType().toString().equals("TREE")) {
				forests.add(new Forest(false, resource.getAmountRemaining(),
						new Position(resource.getXPosition(), resource.getYPosition())));
			} else if (resource.getType().toString().equals("GOLD_MINE")) {
				goldMines.add(new GoldMine(false, resource.getAmountRemaining(),
						new Position(resource.getXPosition(), resource.getYPosition())));
			}
		}

		for (Unit.UnitView unit : state.getAllUnits()) {
			if (unit.getTemplateView().getName().toLowerCase().equals("peasant")) {
				this.peasant = new Peasant(null, 0, new Position(unit.getXPosition(), unit.getYPosition()),
						unit.getID());
			}
			if (unit.getTemplateView().getName().toLowerCase().equals("townhall")) {
				this.townHall = new TownHall(true, unit, new Position(unit.getXPosition(), unit.getYPosition()));
			}
		}
	}

	public GameState(StripsAction parentAction, GameState parentState, Peasant peasant, ArrayList<Forest> forests,
			ArrayList<GoldMine> goldMines, TownHall townHall, int goalWood, int goalGold, int myWood, int myGold,
			int playerNum, State.StateView state, int costToState) {

		this.parentAction = parentAction;
		this.parentState = parentState;
		this.peasant = peasant;
		this.forests = forests;
		this.goldMines = goldMines;
		this.townHall = townHall;
		this.goalWood = goalWood;
		this.goalGold = goalGold;
		this.myWood = myWood;
		this.myGold = myGold;
		this.playerNum = playerNum;
		this.state = state;
		this.cost = costToState;
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

		List<GameState> children = new ArrayList<GameState>();

		// If peasant isn't holding anything, it should move to a resource, or
		// harvest at a resource
		if (peasant.getIsEmpty()) {
			for (Forest forest : forests) {
				HarvestAction harvest = new HarvestAction(this.peasant, forest);

				if (harvest.preconditionsMet(this)) {
					children.add(harvest.apply(this));
				}

				if (parentAction == null || parentAction.getAction() != "MOVE") {
					MoveAction move = new MoveAction(this.peasant, forest, forest.getPosition());
					children.add(move.apply(this));
				}
			}

			for (GoldMine goldmine : goldMines) {
				HarvestAction harvest = new HarvestAction(this.peasant, goldmine);

				if (harvest.preconditionsMet(this)) {
					children.add(harvest.apply(this));
				}

				if (parentAction == null || parentAction.getAction() != "MOVE") {
					MoveAction move = new MoveAction(this.peasant, goldmine, goldmine.getPosition());
					children.add(move.apply(this));
				}
			}

		}

		// If a peasant is holding something, it should move towards the
		// TownHall, or deposit at TownHall
		if (!peasant.getIsEmpty()) {

			DepositAction deposit = new DepositAction(this.peasant);

			if (deposit.preconditionsMet(this)) {
				children.add(deposit.apply(this));
			}

			if (parentAction == null || parentAction.getAction() != "MOVE") {
				MoveAction move = new MoveAction(this.peasant, this.townHall, townHall.getPosition());
				children.add(move.apply(this));
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

		String lastAction = parentAction.getAction();

		if (parentState.parentAction == null) {

			return heuristic;
		}

		String ancestorAction = parentState.parentAction.getAction();

		switch (ancestorAction) {
		case "MOVE":
			switch (lastAction) {
			case "HARVEST":

				HarvestAction harvest = (HarvestAction) parentAction;

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
		return this.cost + heuristic();
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

	public StripsAction getParentAction() {
		return parentAction;
	}

	public void setParentAction(StripsAction parentAction) {
		this.parentAction = parentAction;
	}

	public GameState getParentState() {
		return parentState;
	}

	public void setParentState(GameState parentState) {
		this.parentState = parentState;
	}

	public Peasant getPeasant() {
		return peasant;
	}

	public void setPeasant(Peasant peasant) {
		this.peasant = peasant;
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

	public String toString() {
		return "[PEASANT: " + this.peasant.getPosition().toString() + "] \n [Action: " + this.parentAction.toString()
				+ "] \n [HEURISTIC: " + heuristic() + "]";
	}

}
