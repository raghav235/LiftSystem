package com.oracle.processors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracle.LiftSystem;
import com.oracle.elevator.constants.LiftConstant;
import com.oracle.elevator.models.Lift;
import com.oracle.exceptions.RequestRejectionException;
import com.oracle.requests.Request;
import com.oracle.strategy.SchedulingStrategy;

public class LiftScheduler {
	
	Logger logger= LoggerFactory.getLogger(LiftScheduler.class);
	
	static LiftScheduler liftScheduler;
	
	SchedulingStrategy schedulingStrategy;
	
	int liftTakenLastTask=-1;
	
	private List<Lift> availableLifts;
	
	
	public SchedulingStrategy getSchedulingStrategy() {
		return schedulingStrategy;
	}

	public void setSchedulingStrategy(SchedulingStrategy schedulingStrategy) {
		this.schedulingStrategy = schedulingStrategy;
	}

	public List<Lift> getAvailableLifts() {
		if(availableLifts==null){
			availableLifts= new ArrayList<>();
		}
		return availableLifts;
	}
	
	public String submitRequest(Request request) {
		if(!validateRequest(request)){
			return LiftConstant.REQUEST_REJECTED.toString();
		}
//		Lift lift= getNextLift();
		Lift lift= schedulingStrategy.getNextLift(availableLifts);
		String response = null;
		try {
			response = lift.addRequest(request);
		} catch (RequestRejectionException e) {
			logger.info("Request is Rejected.");
		}
		if(response==null || LiftConstant.REQUEST_REJECTED.toString().equals(response)){
			schedulingStrategy.updateLiftSequenceForRequestFailure();
			//liftTakenLastTask=liftTakenLastTask-1;
		}
		
		return response;
		
	}
	
	/**
	 * At scheduler level request must be within range of floors.
	 * start floor and end floor shouldn't be same.
	 * @param request
	 * @return
	 */
	public boolean validateRequest(Request request){
		if(request.getStartFloor()<LiftConstant.MIN_FLOOR ||
				request.getStartFloor()>LiftConstant.MAX_FLOOR||
				request.getEndFloor()<LiftConstant.MIN_FLOOR||
				request.getEndFloor()>LiftConstant.MAX_FLOOR){
			return false;
		}
		
		if(request.getStartFloor()==request.getEndFloor()){
			return false;
		}
		return true;
	}
	
	/**
	 * Only one scheduler should be present for entire lift system
	 * @return
	 */
	public static LiftScheduler getScheduler(){
		if(liftScheduler==null){
			synchronized (LiftScheduler.class) {
				if(liftScheduler==null){
					liftScheduler= new LiftScheduler();
				}
				
			}
		}
		
		return liftScheduler;
	}
	/**
	 * To-Do remove hardcoding
	 * @return
	 */
	private Lift getNextLift(){
		int nextLift=(liftTakenLastTask==-1||liftTakenLastTask==Integer.parseInt(LiftSystem.prop.getProperty("lift.system.liftCount"))-1)?(liftTakenLastTask=0):(liftTakenLastTask=liftTakenLastTask+1);
		return this.availableLifts.get(nextLift);
	}

	public void removeNonWorkingLift(Lift lift) {
		this.getAvailableLifts().remove(lift);
	}

}
