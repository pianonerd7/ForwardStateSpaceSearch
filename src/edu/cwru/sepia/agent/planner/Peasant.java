package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;

public class Peasant extends MapObject {

	private ResourceType holdingObject;
	private int resourceQuantity;
	private boolean isNextToGoldMine;
	private boolean isNextToForest;
	private boolean isNextToTownHall;

	public Peasant(ResourceType object, int quantity) {
		super(true);

		holdingObject = object;
		this.resourceQuantity = quantity;
	}

	public void setResourceType(ResourceType type) {
		this.holdingObject = type;
	}

	public ResourceType getResourceType() {
		return holdingObject;
	}

	public void setResourceQuantity(int newQuantity) {
		this.resourceQuantity = newQuantity;
	}

	public int getResourceQuantity() {
		return resourceQuantity;
	}

	private void setIsNextToGoldMine(boolean newVal) {
		this.isNextToGoldMine = newVal;
	}

	private boolean getIsNextToGoldMine() {
		return isNextToGoldMine;
	}

	private void setIsNextToForest(boolean newVal) {
		this.isNextToForest = newVal;
	}

	private boolean getIsNextToForest() {
		return isNextToForest;
	}

	private void setIsNextToTownHall(boolean newVal) {
		this.isNextToTownHall = newVal;
	}

	private boolean getIsNextToTownHall() {
		return isNextToTownHall;
	}
}
