package edu.cwru.sepia.agent.planner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.CreateAction;
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
	private Stack<StripsAction> plan = new Stack<StripsAction>();

	// maps the real unit Ids to the plan's unit ids
	// when you're planning you won't know the true unit IDs that sepia assigns.
	// So you'll use placeholders (1, 2, 3).
	// this maps those placeholders to the actual unit IDs.
	private Map<Integer, Integer> peasantIdMap;
	private int townhallId;
	private int peasantTemplateId;
	private int stripsNewPId = -1;
	// integer = unit id, position = desired dest.
	private Map<Integer, Position> desiredDestination = new HashMap<Integer, Position>();
	private Map<Integer, Action> desiredAction = new HashMap<Integer, Action>();

	private Unit.UnitView townHall;

	public PEAgent(int playernum, Stack<StripsAction> plan) {
		super(playernum);
		peasantIdMap = new HashMap<Integer, Integer>();
		this.plan = plan;
	}

	@Override
	public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
		// gets the townhall ID and the peasant ID
		int myIndex = 0;
		for (int unitId : stateView.getUnitIds(playernum)) {
			Unit.UnitView unit = stateView.getUnit(unitId);
			String unitType = unit.getTemplateView().getName().toLowerCase();
			if (unitType.equals("townhall")) {
				townhallId = unitId;
				townHall = stateView.getUnit(unitId);
			} else if (unitType.equals("peasant")) {
				peasantIdMap.put(myIndex, unitId);
				myIndex++;
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

		if (!plan.empty()) {

			checkCreatePeasant(stateView);

			StripsAction stripsAction = plan.pop();
			peasantID = this.peasantIdMap.get(getPeasantID(stripsAction));

			if (lastAction.get(peasantID) != null) {
				if (lastAction.get(peasantID).getFeedback() == ActionFeedback.FAILED) {
					if (lastAction.get(peasantID).getAction().getType() == ActionType.COMPOUNDMOVE) {
						Position desiredPos = desiredDestination.get(peasantID);
						for (Position pos : desiredPos.getAdjacentPositions()) {

							if (pos.inBounds(stateView.getXExtent(), stateView.getYExtent())
									&& !stateView.isResourceAt(pos.x, pos.y)) {
								Action moveAction = Action.createCompoundMove(peasantID, pos.x, pos.y);
								sepiaAction.put(peasantID, moveAction);
								plan.push(stripsAction);
								return sepiaAction;
							}
						}
					}
					if (lastAction.get(peasantID).getAction().getType() == ActionType.PRIMITIVEGATHER) {
						sepiaAction.put(peasantID, desiredAction.get(peasantID));
					}
					if (lastAction.get(peasantID).getAction().getType() == ActionType.PRIMITIVEGATHER) {
				}

				isPrevActionComplete = lastAction.get(peasantID).getFeedback() == ActionFeedback.COMPLETED;
			}

			if (!isPrevActionComplete) {
				plan.push(stripsAction);
				System.out.println("a move action ... ");
				return null;
			}

			if (stripsAction.getAction().equals("CREATE")) {
				Action action = createSepiaAction(stripsAction, peasantID);
				sepiaAction.put(this.townhallId, action);
				this.desiredAction.put(this.townhallId, action);
			} else {
				Action action = createSepiaAction(stripsAction, peasantID);
				sepiaAction.put(peasantID, action);
				this.desiredAction.put(peasantID, action);
			}
		}

		return sepiaAction;
	}

	private void checkCreatePeasant(State.StateView stateView) {
		for (int unitId : stateView.getUnitIds(playernum)) {
			Unit.UnitView unit = stateView.getUnit(unitId);
			String unitType = unit.getTemplateView().getName().toLowerCase();

			if (unitType.equals("peasant")) {

				for (Integer key : peasantIdMap.keySet()) {
					if (stripsNewPId != -1 && peasantIdMap.get(key) != unitId) {
						peasantIdMap.put(stripsNewPId, unitId);
						stripsNewPId = -1;
					}
				}
			}
		}
	}

	/**
	 * Returns a SEPIA version of the specified Strips Action.
	 * 
	 * @param action
	 *            StripsAction
	 * @return SEPIA representation of same action
	 */
	private Action createSepiaAction(StripsAction action, int peasantID) {

		switch (action.getAction()) {
		case "MOVE":
			MoveAction move = (MoveAction) action;
			Action moveAction = Action.createCompoundMove(peasantID, move.getBestPosition().x,
					move.getBestPosition().y);

			desiredDestination.put(peasantID, move.getMapObject().getPosition());
			System.out.println(moveAction.toString());
			return moveAction;

		case "HARVEST":
			HarvestAction harvest = (HarvestAction) action;
			Action harvestAction = Action.createPrimitiveGather(peasantID,
					harvest.getPeasant().getPosition().getDirection(harvest.getResource().getPosition()));

			desiredDestination.put(peasantID, harvest.getResource().getPosition());
			System.out.println(harvestAction.toString());
			return harvestAction;

		case "DEPOSIT":
			DepositAction deposit = (DepositAction) action;
			Action depositAction = Action.createPrimitiveDeposit(peasantID, deposit.getPeasant().getPosition()
					.getDirection(new Position(this.townHall.getXPosition(), this.townHall.getYPosition())));

			desiredDestination.put(peasantID, new Position(this.townHall.getXPosition(), this.townHall.getYPosition()));
			System.out.println(depositAction.toString());
			return depositAction;

		case "CREATE":
			CreateAction create = (CreateAction) action;
			Action createAction = Action.createPrimitiveProduction(this.townhallId, this.peasantTemplateId);

			stripsNewPId = create.getCreatedPeasant().getUnitID();

			System.out.println(create.toString());
			return createAction;
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

		case "CREATE":
			CreateAction create = (CreateAction) action;

			return create.getPeasant().getUnitID();
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
