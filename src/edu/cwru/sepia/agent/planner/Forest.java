package edu.cwru.sepia.agent.planner;

/**
 * Represents forests on the map. It has a location (super) and a quantity of
 * how much wood it has
 * 
 * @author Anna He
 *
 */
public class Forest extends MapObject {

	private int resourceQuantity;

	public Forest(boolean isEmpty, int quantity, Position pos) {
		super(isEmpty, "FOREST", pos);
		this.resourceQuantity = quantity;
	}

	public void setResourceQuantity(int newQuantity) {
		this.resourceQuantity = newQuantity;
	}

	public int getResourceQuantity() {
		return resourceQuantity;
	}

}
