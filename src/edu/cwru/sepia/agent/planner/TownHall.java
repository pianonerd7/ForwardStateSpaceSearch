package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.Unit;

public class TownHall extends MapObject {

	private Unit.UnitView unit;

	public TownHall(boolean isEmpty, Unit.UnitView unit) {
		super(isEmpty);

		this.unit = unit;
	}

	public Unit.UnitView getUnit() {
		return unit;
	}

	public void setUnit(Unit.UnitView unit) {
		this.unit = unit;
	}
}
