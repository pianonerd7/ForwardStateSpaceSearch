package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.Unit;

/**
 * Represents townhall on the map, has a location
 * 
 * @author Anna He
 *
 */
public class TownHall extends MapObject {

	private Unit.UnitView unit;

	public TownHall(boolean isEmpty, Unit.UnitView unit, Position pos) {
		super(isEmpty, "TOWNHALL", pos);

		this.unit = unit;
	}

	public Unit.UnitView getUnit() {
		return unit;
	}

	public void setUnit(Unit.UnitView unit) {
		this.unit = unit;
	}

}
