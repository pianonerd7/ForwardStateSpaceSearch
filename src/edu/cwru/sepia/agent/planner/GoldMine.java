package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;

public class GoldMine extends MapObject {

	private int resourceQuantity;
	private ResourceNode.ResourceView unit;
	private Position position;

	public GoldMine(boolean isEmpty, int quantity, ResourceNode.ResourceView unit, Position pos) {
		super(isEmpty);
		this.resourceQuantity = quantity;
		this.unit = unit;
		this.position = pos;
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

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
