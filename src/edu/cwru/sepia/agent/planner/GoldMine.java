package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;

public class GoldMine extends MapObject {

	private int resourceQuantity;
	private ResourceNode.ResourceView unit;

	public GoldMine(boolean isEmpty, int quantity, ResourceNode.ResourceView unit, Position pos) {
		super(isEmpty, "GOLDMINE", pos);
		this.resourceQuantity = quantity;
		this.unit = unit;
	}

	public void setResourceQuantity(int newQuantity) {
		this.resourceQuantity = newQuantity;
	}

	public int getResourceQuantity() {
		return resourceQuantity;
	}

	public ResourceNode.ResourceView getUnit() {
		return unit;
	}

	public void setUnit(ResourceNode.ResourceView unit) {
		this.unit = unit;
	}

}
