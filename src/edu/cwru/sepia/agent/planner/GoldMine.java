package edu.cwru.sepia.agent.planner;

public class GoldMine extends MapObject {

	private int resourceQuantity;

	public GoldMine(boolean isEmpty, int quantity) {
		super(isEmpty);
		this.resourceQuantity = quantity;
	}

	public void setResourceQuantity(int newQuantity) {
		this.resourceQuantity = newQuantity;
	}

	public int getResourceQuantity() {
		return resourceQuantity;
	}
}
