package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;

public class Peasant extends MapObject {

	private ResourceType holdingObject;
	private int resourceQuantity;
	private boolean isNextToGoldMine;
	private boolean isNextToForest;
	private boolean isNextToTownHall;

	public Peasant(ResourceType object, int quantity, Position pos) {

		super(true, "PEASANT", pos);

		if (quantity > 0) {
			super.setIsEmpty(false);
		}

		holdingObject = object;
		this.resourceQuantity = quantity;
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

	public void resetNextTo() {
		isNextToGoldMine = false;
		isNextToForest = false;
		isNextToTownHall = false;
	}

	public String toString() {
		return this.getPosition().toString();
	}
}
