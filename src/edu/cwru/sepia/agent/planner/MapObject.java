package edu.cwru.sepia.agent.planner;

public abstract class MapObject {

	private boolean isEmpty;
	private String name;
	private Position position;

	public MapObject(boolean isEmpty, String name, Position position) {
		this.isEmpty = isEmpty;
		this.name = name;
		this.position = position;
	}

	public void setIsEmpty(boolean newVal) {
		this.isEmpty = newVal;
	}

	public boolean getIsEmpty() {
		return isEmpty;
	}

	public String getName() {
		return name;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
