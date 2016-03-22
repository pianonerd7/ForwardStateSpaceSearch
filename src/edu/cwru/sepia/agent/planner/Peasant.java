package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;

public class Peasant extends MapObject {

	private ResourceType holdingObject;
	private int resourceQuantity;

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
}
