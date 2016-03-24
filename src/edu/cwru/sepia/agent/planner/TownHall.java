package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.Unit;

public class TownHall extends MapObject {

	private Unit.UnitView unit;
	private Position position;

	public TownHall(boolean isEmpty, Unit.UnitView unit, Position pos) {
		super(isEmpty);

		this.unit = unit;
		this.position = pos;
	}

	public Unit.UnitView getUnit() {
		return unit;
	}

	public void setUnit(Unit.UnitView unit) {
		this.unit = unit;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
}
