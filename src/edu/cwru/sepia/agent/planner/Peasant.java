package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;

public class Peasant extends MapObject {

	private ResourceType holdingObject;
	private int resourceQuantity;
	private boolean isNextToGoldMine;
	private boolean isNextToForest;
	private boolean isNextToTownHall;
	private Unit.UnitView unit;

	public Peasant(ResourceType object, int quantity, Unit.UnitView unit, Position pos) {

		super(true, "PEASANT", pos);

		if (quantity > 0) {
			super.setIsEmpty(false);
		}

		holdingObject = object;
		this.resourceQuantity = quantity;
		this.unit = unit;
	}

	public ResourceType getHoldingObject() {
		return holdingObject;
	}

	public void setHoldingObject(ResourceType holdingObject) {
		this.holdingObject = holdingObject;
	}

	public int getResourceQuantity() {
		return resourceQuantity;
	}

	public void setResourceQuantity(int resourceQuantity) {
		this.resourceQuantity = resourceQuantity;
	}

	public boolean isNextToGoldMine() {
		return isNextToGoldMine;
	}

	public void setNextToGoldMine(boolean isNextToGoldMine) {
		this.isNextToGoldMine = isNextToGoldMine;
	}

	public boolean isNextToForest() {
		return isNextToForest;
	}

	public void setNextToForest(boolean isNextToForest) {
		this.isNextToForest = isNextToForest;
	}

	public boolean isNextToTownHall() {
		return isNextToTownHall;
	}

	public void setNextToTownHall(boolean isNextToTownHall) {
		this.isNextToTownHall = isNextToTownHall;
	}

	public Unit.UnitView getUnit() {
		return unit;
	}

	public void setUnit(Unit.UnitView unit) {
		this.unit = unit;
	}

	public void resetNextTo() {
		isNextToGoldMine = false;
		isNextToForest = false;
		isNextToTownHall = false;
	}

}
