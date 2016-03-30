package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;

public class Peasant extends MapObject {

	private ResourceType holdingObject;
	private int resourceQuantity;
	private boolean isNextToGoldMine;
	private boolean isNextToForest;
	private boolean isNextToTownHall;
	private int unitID;

	public Peasant(ResourceType object, int quantity, Position pos, int unitID) {

		super(true, "PEASANT", pos);

		if (quantity > 0) {
			super.setIsEmpty(false);
		}

		holdingObject = object;
		this.resourceQuantity = quantity;
		this.unitID = unitID;
	}

	public Peasant(Peasant peasant) {
		super(peasant.resourceQuantity == 0, "PEASANT", new Position(peasant.getPosition().x, peasant.getPosition().y));
		this.holdingObject = peasant.getHoldingObject();
		this.resourceQuantity = peasant.getResourceQuantity();
		this.isNextToGoldMine = peasant.isNextToGoldMine();
		this.isNextToForest = peasant.isNextToForest();
		this.isNextToTownHall = peasant.isNextToTownHall();
		this.unitID = peasant.getUnitID();
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

	public int getUnitID() {
		return this.unitID;
	}

	public void resetNextTo() {
		isNextToGoldMine = false;
		isNextToForest = false;
		isNextToTownHall = false;
	}

	public String toString() {
		return this.getPosition().toString();
	}
}
