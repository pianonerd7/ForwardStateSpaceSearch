package edu.cwru.sepia.agent.planner;

/**
 * Represents GoldMine on the map. Has a location and a quantity
 * 
 * @author Pianonerd77
 *
 */
public class GoldMine extends MapObject {

	private int resourceQuantity;

	public GoldMine(boolean isEmpty, int quantity, Position pos) {
		super(isEmpty, "GOLDMINE", pos);
		this.resourceQuantity = quantity;
	}

	public void setResourceQuantity(int newQuantity) {
		this.resourceQuantity = newQuantity;
	}

	public int getResourceQuantity() {
		return resourceQuantity;
	}
}
