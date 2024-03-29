package edu.cwru.sepia.agent.planner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

public class PlannerAgent extends Agent {

	final int requiredWood;
	final int requiredGold;
	final boolean buildPeasants;

	// Your PEAgent implementation. This prevents you from having to parse the
	// text file representation of your plan.
	PEAgent peAgent;

	public PlannerAgent(int playernum, String[] params) {
		super(playernum);

		if (params.length < 3) {
			System.err.println(
					"You must specify the required wood and gold amounts and whether peasants should be built");
		}

		requiredWood = Integer.parseInt(params[0]);
		requiredGold = Integer.parseInt(params[1]);
		buildPeasants = Boolean.parseBoolean(params[2]);

		System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: "
				+ buildPeasants);
	}

	@Override
	public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

		Stack<StripsAction> plan = AstarSearch(
				new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

		if (plan == null) {
			System.err.println("No plan was found");
			System.exit(1);
			return null;
		}

		// write the plan to a text file
		savePlan(plan);

		// Instantiates the PEAgent with the specified plan.
		peAgent = new PEAgent(playernum, plan);

		return peAgent.initialStep(stateView, historyView);
	}

	@Override
	public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
		if (peAgent == null) {
			System.err.println("Planning failed. No PEAgent initialized.");
			return null;
		}

		return peAgent.middleStep(stateView, historyView);
	}

	@Override
	public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

	}

	@Override
	public void savePlayerData(OutputStream outputStream) {

	}

	@Override
	public void loadPlayerData(InputStream inputStream) {

	}

	/**
	 * Perform an A* search of the game graph. This should return your plan as a
	 * stack of actions. This is essentially the same as your first assignment.
	 * The implementations should be very similar. The difference being that
	 * your nodes are now GameState objects not MapLocation objects.
	 *
	 * @param startState
	 *            The state which is being planned from
	 * @return The plan or null if no plan is found.
	 */
	private Stack<StripsAction> AstarSearch(GameState startState) {

		ArrayList<GameState> openList = new ArrayList<GameState>();

		openList.add(startState);

		while (!openList.isEmpty()) {
			Collections.sort(openList, Collections.reverseOrder());

			GameState curState = openList.get(0);
			openList.remove(0);

			if (curState.getParentAction() != null) {
				System.out.println(curState.getParentAction().toString());
			}

			List<GameState> children = curState.generateChildren();

			openList = new ArrayList<GameState>();
			for (GameState child : children) {
				if (curState.isGoal()) {
					System.out.println("PATH FOUND \n");
					return getPath(curState);
				}

				if (canAddToOpenList(child, openList)) {
					openList.add(child);
				}
			}
		}
		System.exit(0);
		return null;
	}

	/**
	 * Checks to see whether the utility of a node is good enough
	 * 
	 * @param neighbor
	 * @param openList
	 * @return
	 */
	private boolean canAddToOpenList(GameState neighbor, ArrayList<GameState> openList) {

		for (GameState node : openList) {
			if (neighbor.heuristic() < node.heuristic()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Based on the last child, we can back track based on the parent action
	 * 
	 * @param child
	 * @return
	 */
	private Stack<StripsAction> getPath(GameState child) {

		Stack<StripsAction> actions = new Stack<StripsAction>();

		while (child != null) {
			if (child.getParentAction() != null) {
				for (StripsAction action : child.getParentAction()) {
					actions.add(action);
				}
			}
			child = child.getParentState();
		}

		while (actions.peek() == null) {
			actions.pop();
		}

		return actions;
	}

	/**
	 * This has been provided for you. Each strips action is converted to a
	 * string with the toString method. This means each class implementing the
	 * StripsAction interface should override toString. Your strips actions
	 * should have a form matching your included Strips definition writeup. That
	 * is <action name>(<param1>, ...). So for instance the move action might
	 * have the form of Move(peasantID, X, Y) and when grounded and written to
	 * the file Move(1, 10, 15).
	 *
	 * @param plan
	 *            Stack of Strips Actions that are written to the text file.
	 */
	private void savePlan(Stack<StripsAction> plan) {
		if (plan == null) {
			System.err.println("Cannot save null plan");
			return;
		}

		File outputDir = new File("saves");
		outputDir.mkdirs();

		File outputFile = new File(outputDir, "plan.txt");

		PrintWriter outputWriter = null;
		try {
			outputFile.createNewFile();

			outputWriter = new PrintWriter(outputFile.getAbsolutePath());

			Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
			while (!tempPlan.isEmpty()) {
				outputWriter.println(tempPlan.pop().toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputWriter != null)
				outputWriter.close();
		}
	}
}
