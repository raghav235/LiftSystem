package com.oracle.elevator.request.handlers;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracle.elevator.constants.LiftConstant;
import com.oracle.elevator.models.Direction;
import com.oracle.elevator.models.Lift;
import com.oracle.requests.Request;

/**
 * This class will be used to manage all the requests for a lift 
 * 1. add request
 * 2. valid request 
 * 3. load next run request etc..
 * @author user
 *
 */
public class LiftRequestHandler implements RequestHandler {

	Logger logger = LoggerFactory.getLogger(LiftRequestHandler.class);

	/**
	 * Validate request
	 * Request should be within lift range.
	 */
	@Override
	public boolean validateRequest(Lift lift, Request request) {
		logger.info("Validating Request: " + request.toString());
		if (!((lift.getStartFloor() <= request.getStartFloor() && request.getStartFloor() <= lift.getEndFloor())
				&& (lift.getStartFloor() <= request.getEndFloor() && request.getEndFloor() <= lift.getEndFloor()))) {
			logger.error("Invalid source/destination.  " + request.toString());
			return false;
		}

		logger.info("Successfull Request: " + request.toString());
		return true;
	}
	
	/**
	 * 1st try to add request to running queue of lift
	 * if request can not be accommodated, add it to next run queue.
	 */
	@Override
	public String addRequst(Lift lift, Request request) {
		return validateRequest(lift, request)
				? (addToRunningQueue(lift, request) ? LiftConstant.REQUEST_SUCCESS : addToNextRun(lift, request))
				: LiftConstant.REQUEST_REJECTED;
	}

	/**
	 * Add request to next run queue of lift 
	 * @param lift
	 * @param request
	 * @return
	 */
	private String addToNextRun(Lift lift, Request request) {
		if (Direction.MOVINGUP.equals(request.getRequestDirection())) {
			lift.upwardRequestsForNextRun.add(request);
			logger.info("Successfully Added.." + request.toString() + "'s to upward queue for next run. "
					+ lift.upwardRequestsForNextRun);
		} else {
			lift.downwardRequestsForNextRun.add(request);
			logger.info("Request :" + request.toString() + " successfully added to " + lift.getName()
					+ "'s downward queue for next run. " + lift.downwardRequestsForNextRun);
		}
		return LiftConstant.REQUEST_SUCCESS;
	}


	/**
	 * Add request to running queue if
	 * 1. Request Direction and Lift direction same
	 * 2. Lift current floor can take next request start floor
	 * @param lift
	 * @param request
	 * @return
	 */
	private boolean addToRunningQueue(Lift lift, Request request) {
		// if lift direction and request direction same
		if (request.getRequestDirection().equals(lift.currentDirection)) {
			logger.info("Trying to add request " + request.toString() + " to current running queue of :" + lift.getName());
			if (lift.currentDirection.equals(Direction.MOVINGUP) && lift.currentFloor.get() < request.getStartFloor()) {
				if (lift.currentRunningQueue == null) {
					lift.currentRunningQueue = new PriorityBlockingQueue<>(lift.getEndFloor());
				}
				mergeRequest(lift, request);
				logger.info("Successfully Added.." + request.toString() + " to running queue. "+ lift.currentRunningQueue + " of " + lift.getName());
				return true;
			} else if (lift.currentDirection.equals(Direction.MOVINGDOWN) && lift.currentFloor.get() > request.getStartFloor()) {
				if (lift.currentRunningQueue == null) {
					lift.currentRunningQueue = new PriorityBlockingQueue<>(lift.getEndFloor(), Collections.reverseOrder());
				}
				mergeRequest(lift, request);
				logger.info("Successfully Added.." + request.toString() + " to running downward queue. "+ lift.currentRunningQueue + " of " + lift.getName());
				return true;
			}

		}
		logger.info("Failed to add request to current running queue. " + request.toString() + " of " + lift.getName());
		return false;
	}

	
	public void mergeRequest(Lift lift, Request request) {
		if (request == null) {
			return;
		}
		if (!lift.currentRunningQueue.contains(request.getStartFloor())) {
			lift.currentRunningQueue.add(request.getStartFloor());
		}
		if (!lift.currentRunningQueue.contains(request.getEndFloor())) {
			lift.currentRunningQueue.add(request.getEndFloor());
		}
	}

	public void mergeRequests(Lift lift, Queue<Request> requests) {
		if (requests == null || requests.isEmpty()) {
			return;
		}
		for (Request request : requests) {
			mergeRequest(lift, request);
		}
	}

	/**
	 * Once lift processed all requests for a single direction, then
	 * next set of request will get loaded to current q.
	 */
	@Override
	public void loadNextSetOfRequests(Lift lift) {
		if (lift.currentDirection.equals(Direction.MOVINGUP)) {//if lift was moving up, for next run,
																//Load downward requests first, if no downward requests , load upward requests.
			boolean isLoaded=loadNextSetOfDownwardRequests(lift);
			if(!isLoaded){
				loadNextSetOfUpwardRequests(lift);
			}
		} else if (lift.currentDirection.equals(Direction.MOVINGDOWN)) {//if lift was moving down,  for next run,
																		//Load upward requests first, if no upward requests , load downward requests.
			boolean isLoaded=loadNextSetOfUpwardRequests(lift);
			if(!isLoaded){
				loadNextSetOfDownwardRequests(lift);
			}
		}
	}
	
	public boolean loadNextSetOfDownwardRequests(Lift lift){
		if(lift.downwardRequestsForNextRun.isEmpty()){
			return false;
		}
		else {
			lift.currentRunningQueue = new PriorityBlockingQueue<>(lift.getEndFloor(), Collections.reverseOrder());
			mergeRequests(lift, lift.downwardRequestsForNextRun);
			lift.downwardRequestsForNextRun.clear();
			lift.moveLiftToPickFirstRequestOfNextRun();
			lift.currentDirection = Direction.MOVINGDOWN;
			return true;
		}
	}
	
	public boolean loadNextSetOfUpwardRequests(Lift lift){
		if (lift.upwardRequestsForNextRun.isEmpty()) {
			return false;
		}else{
			lift.currentRunningQueue = new PriorityBlockingQueue<>(lift.getEndFloor());
			mergeRequests(lift, lift.upwardRequestsForNextRun);
			lift.upwardRequestsForNextRun.clear();
			lift.moveLiftToPickFirstRequestOfNextRun();
			lift.currentDirection = Direction.MOVINGUP;
			return true;
		}
	}

}
