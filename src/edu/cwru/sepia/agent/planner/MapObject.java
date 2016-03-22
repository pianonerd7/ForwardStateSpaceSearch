package edu.cwru.sepia.agent.planner;

public abstract class MapObject {

	private boolean isEmpty;

	public MapObject(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	public void setIsEmpty(boolean newVal) {
		this.isEmpty = newVal;
	}

	public boolean getIsEmpty() {
		return isEmpty;
	}
}
