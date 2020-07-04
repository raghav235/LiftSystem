package com.oracle.strategy;

import java.util.List;

import com.oracle.elevator.models.Lift;

public interface SchedulingStrategy {
	public Lift getNextLift(List<Lift> lifts);
	public void updateLiftSequenceForRequestFailure(); 
	
}
