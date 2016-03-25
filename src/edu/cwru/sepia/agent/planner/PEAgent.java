package edu.cwru.sepia.agent.planner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may
 * add your own methods and members.
 */
public class PEAgent extends Agent {

	// The plan being executed
	private Stack<StripsAction> plan = null;

	// maps the real unit Ids to the plan's unit ids
	// when you're planning you won't know the true unit IDs that sepia assigns.
	// So you'll use placeholders (1, 2, 3).
	// this maps those placeholders to the actual unit IDs.
	private Map<Integer, Integer> peasantIdMap;
	private int townhallId;
	private int peasantTemplateId;

	private Unit.UnitView townHall;

	public PEAgent(int playernum, Stack<StripsAction> plan) {
		super(playernum);
		peasantIdMap = new HashMap<Integer, Integer>();
		this.plan = plan;
	}

	@Override
	public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
		// gets the townhall ID and the peasant ID
		for (int unitId : stateView.getUnitIds(playernum)) {
			Unit.UnitView unit = stateView.getUnit(unitId);
			String unitType = unit.getTemplateView().getName().toLowerCase();
			if (unitType.equals("townhall")) {
				townhallId = unitId;
				townHall = stateView.getUnit(unitId);
			} else if (unitType.equals("peasant")) {
				peasantIdMap.put(unitId, unitId);
			}
		}

		// Gets the peasant template ID. This is used when building a new
		// peasant with the townhall
		for (Template.TemplateView templateView : stateView.getTemplates(playernum)) {
			if (templateView.getName().toLowerCase().equals("peasant")) {
				peasantTemplateId = templateView.getID();
				break;
			}
		}

		return middleStep(stateView, historyView);
	}

	/**
	 * This is where you will read the provided plan and execute it. If your
	 * plan is correct then when the plan is empty the scenario should end with
	 * a victory. If the scenario keeps running after you run out of actions to
	 * execute then either your plan is incorrect or your execution of the plan
	 * has a bug.
	 *
	 * You can create a SEPIA deposit action with the following method
	 * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
	 *
	 * You can create a SEPIA harvest action with the following method
	 * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
	 *
	 * You can create a SEPIA build action with the following method
	 * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
	 *
	 * You can create a SEPIA move action with the following method
	 * Action.createCompoundMove(int peasantId, int x, int y)
	 *
	 * these actions are stored in a mapping between the peasant unit ID
	 * executing the action and the action you created.
	 *
	 * For the compound actions you will need to check their progress and wait
	 * until they are complete before issuing another action for that unit. If
	 * you issue an action before the compound action is complete then the
	 * peasant will stop what it was doing and begin executing the new action.
	 *
	 * To check an action's progress you can call getCurrentDurativeAction on
	 * each UnitView. If the Action is null nothing is being executed. If the
	 * action is not null then you should also call getCurrentDurativeProgress.
	 * If the value is less than 1 then the action is still in progress.
	 *
	 * Also remember to check your plan's preconditions before executing!
	 */
	@Override
	public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {

		Map<Integer, ActionResult> lastAction = historyView.getCommandFeedback(playernum,
				stateView.getTurnNumber() - 1);

		boolean isPrevActionComplete = true;

		Map<Integer, Action> sepiaAction = new HashMap<Integer, Action>();
		int peasantID = -1;

		if (!this.plan.empty()) {
			StripsAction stripsAction = this.plan.pop();
			peasantID = getPeasantID(stripsAction);

			if (lastAction.get(peasantID) != null) {
				isPrevActionComplete = lastAction.get(peasantID).getFeedback() == ActionFeedback.COMPLETED;
			}

			if (!isPrevActionComplete) {
				this.plan.push(stripsAction);
				return null;
			}

			sepiaAction.put(peasantID, createSepiaAction(stripsAction));
		}

		return sepiaAction;
	}

	/**
	 * Returns a SEPIA version of the specified Strips Action.
	 * 
	 * @param action
	 *            StripsAction
	 * @return SEPIA representation of same action
	 */
	private Action createSepiaAction(StripsAction action) {

		switch (action.getAction()) {
		case "MOVE":
			MoveAction move = (MoveAction) action;
			Action moveAction = Action.createCompoundMove(move.getPeasant().getUnitID(), move.getBestPosition().x,
					move.getBestPosition().y);

			return moveAction;

		case "HARVEST":
			HarvestAction harvest = (HarvestAction) action;
			Action harvestAction = Action.createPrimitiveGather(harvest.getPeasant().getUnitID(),
					harvest.getPeasant().getPosition().getDirection(harvest.getResource().getPosition()));

			return harvestAction;

		case "DEPOSIT":
			DepositAction deposit = (DepositAction) action;
			Action depositAction = Action.createPrimitiveDeposit(deposit.getPeasant().getUnitID(),
					deposit.getPeasant().getPosition()
							.getDirection(new Position(this.townHall.getXPosition(), this.townHall.getYPosition())));

			return depositAction;

		default:
			return null;

		}
	}

	private int getPeasantID(StripsAction action) {

		switch (action.getAction()) {
		case "MOVE":
			MoveAction move = (MoveAction) action;

			return move.getPeasant().getUnitID();

		case "HARVEST":
			HarvestAction harvest = (HarvestAction) action;

			return harvest.getPeasant().getUnitID();

		case "DEPOSIT":
			DepositAction deposit = (DepositAction) action;

			return deposit.getPeasant().getUnitID();

		default:
			return -1;
		}
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
}
