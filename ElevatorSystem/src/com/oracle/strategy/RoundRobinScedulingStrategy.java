package com.oracle.strategy;

import java.util.List;

import com.oracle.LiftSystem;
import com.oracle.elevator.models.Lift;

/**
 * As per round robin strategy, each new request will be assigned to next lift in cyclic order.
 * @author user
 *
 */
public class RoundRobinScedulingStrategy implements SchedulingStrategy{
	
	private int liftTakenLastRequest=-1;
	
	/**
	 * Round robin bais Lift pickup
	 * Ex: 4 lift
	 * Lift -1, Lift2-Lift3, Lift-4, Lift-1,Lift-2,.........
	 */
	@Override
	public Lift getNextLift(List<Lift> lifts) {
		int nextLift = (liftTakenLastRequest == -1
						|| liftTakenLastRequest == Integer.parseInt(LiftSystem.prop.getProperty("lift.system.liftCount")) - 1)
						? (liftTakenLastRequest = 0) : (liftTakenLastRequest = liftTakenLastRequest + 1);
		return lifts.get(nextLift);
	}

	/**
	 * For the failed Request, assign the same lift for next request.
	 */
	@Override
	public void updateLiftSequenceForRequestFailure() {
		if(liftTakenLastRequest==-1){
		}
		else liftTakenLastRequest=liftTakenLastRequest-1;
	}

}
