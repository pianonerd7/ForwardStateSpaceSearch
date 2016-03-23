package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;

public class Forest extends MapObject {

	private int resourceQuantity;
	private ResourceNode.ResourceView unit;

	public Forest(boolean isEmpty, int quantity, ResourceNode.ResourceView resource) {
		super(isEmpty);
		this.resourceQuantity = quantity;
		this.unit = resource;
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
