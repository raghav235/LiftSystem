package com.oracle.requests;

import com.oracle.elevator.models.Direction;

public class Request implements Comparable<Request> {
	private int startFloor;
	private int endFloor;
	private Direction requestDirection;
	
	public Request(int requestStartFloor, int requestEndFloor) {
		this.startFloor=requestStartFloor;
		this.endFloor=requestEndFloor;
		requestDirection=(requestEndFloor>requestStartFloor)?Direction.MOVINGUP:Direction.MOVINGDOWN;
	}
	
	public int getStartFloor() {
		return startFloor;
	}

	public void setStartFloor(int startFloor) {
		this.startFloor = startFloor;
	}

	public int getEndFloor() {
		return endFloor;
	}

	public void setEndFloor(int endFloor) {
		this.endFloor = endFloor;
	}

	public Direction getRequestDirection() {
		return requestDirection;
	}

	public void setRequestDirection(Direction requestDirection) {
		this.requestDirection = requestDirection;
	}

	@Override
	public String toString() {
		return "["+this.startFloor+" -> "+this.endFloor+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endFloor;
		result = prime * result + ((requestDirection == null) ? 0 : requestDirection.hashCode());
		result = prime * result + startFloor;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (endFloor != other.endFloor)
			return false;
		if (requestDirection != other.requestDirection)
			return false;
		if (startFloor != other.startFloor)
			return false;
		return true;
	}

	@Override
	public int compareTo(Request req) {
		// return this.getId().compareTo(emp.getId());
		return this.endFloor-req.endFloor;
	}
}
