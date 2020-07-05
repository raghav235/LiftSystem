package com.oracle.elevator.models;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.oracle.LiftSystem;
import com.oracle.elevator.request.handlers.LiftRequestHandler;
import com.oracle.exceptions.RequestRejectionException;
import com.oracle.requests.Request;

/**
 * Lift will be treated as Thread
 * @author user
 *
 */
public class Lift extends Thread{
	
	Logger logger= LoggerFactory.getLogger(Lift.class);

	//This Lift will start from startFloor
	private int startFloor;
	
	//This lift will go max upto endFloor
	private int endFloor;
	
	//Lift current state
	public volatile Direction currentDirection;
	
	// Running lift's floor
	public volatile AtomicInteger currentFloor;
	
	public volatile Queue<Integer> currentRunningQueue;
		
	//this priority queue contains all the requests which can not be accepted by running lift
	public volatile Queue<Request> upwardRequestsForNextRun;
	
	//this priority queue contains all the requests when lift going in upward direction
	public volatile Queue<Request> downwardRequestsForNextRun;
	
	private volatile boolean shutDownRequested=false;

	LiftRequestHandler liftRequestHandler;
	
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

	public AtomicInteger getCurrentFloor() {
		return currentFloor;
	}

	public void setCurrentFloor(AtomicInteger currentFloor) {
		this.currentFloor = currentFloor;
	}

	public Lift(){
		
	}
	
	//create a Lift - constructor 
	public Lift(String name, int startFloor, int endFloor, int currentFloor) {
		this.setName(name);
		this.startFloor=startFloor;
		this.endFloor=endFloor;
		this.currentDirection=Direction.MOVINGUP;
		this.currentFloor=new AtomicInteger(currentFloor);
		upwardRequestsForNextRun = new PriorityBlockingQueue <>(endFloor);
		downwardRequestsForNextRun = new PriorityBlockingQueue <>(endFloor, Collections.reverseOrder());
		this.liftRequestHandler=new LiftRequestHandler();
	}
	
	public boolean isShutDownRequested() {
		return shutDownRequested;
	}

	public void setShutDownRequested(boolean shutDownRequested) {
		this.shutDownRequested = shutDownRequested;
	}

	/**
	 * This method will help to change the 
	 * Lift state.
	 * @param updatedState
	 */
	public void changeState(Direction updatedState){
		this.currentDirection=updatedState;
	}

	
	public String addRequest(Request request) throws RequestRejectionException {
		if(LiftSystem.isShutdownRequested){
			logger.info("Request can not be accepted, as lift system is request for shutdown.");
			throw new RequestRejectionException("New Request can not be accepted.");
		}
		logger.info("User Request: "+request.toString());
		return liftRequestHandler.addRequst(this, request);
	}		

	
	//process request
	public void run(){
		MDC.put("logFileName", getName());
		logger.info("Started");
		while(!isShutDownRequested()){
			while(this.currentRunningQueue !=null && !this.currentRunningQueue.isEmpty()){
				logger.info(this.getName()+"->"+"Running Queue: "+this.currentRunningQueue);
				logger.info(this.getName()+"->"+"Current Floor :"+this.currentFloor);
				
				Integer nextStop= this.currentRunningQueue.poll();
				
				logger.info(this.getName()+"->"+"Next Stop is :"+nextStop);
				int waitTimeSlot= nextStop-this.currentFloor.get();
				
				for(int i=1;i <=Math.abs(waitTimeSlot);i++){
					try {
						Thread.sleep(1000);
						if(waitTimeSlot>0){
							this.currentDirection=Direction.MOVINGUP;
							this.currentFloor.getAndIncrement();
							Integer interruptedNextStop=currentRunningQueue.peek();//8
							if(interruptedNextStop!=null && interruptedNextStop > this.currentFloor.get() && interruptedNextStop<nextStop ){
								if(!this.currentRunningQueue.contains(nextStop)){
									this.currentRunningQueue.add(nextStop);
								}
								nextStop=this.currentRunningQueue.poll();
								logger.info(this.getName()+"->"+"Interrupted Next Stop is :"+nextStop);
								waitTimeSlot=nextStop-this.currentFloor.get();
								i=0;
							}
						} else {
							this.currentDirection=Direction.MOVINGDOWN;
							this.currentFloor.getAndDecrement();
							Integer interruptedNextStop=currentRunningQueue.peek();
							if(interruptedNextStop!=null && interruptedNextStop < this.currentFloor.get() && interruptedNextStop>nextStop ){
								
								if(!this.currentRunningQueue.contains(nextStop)){
									this.currentRunningQueue.add(nextStop);
								}
								
								nextStop=this.currentRunningQueue.poll();
								logger.info(this.getName()+"->"+"Interrupted Next Stop is :"+nextStop);
								waitTimeSlot=nextStop-this.currentFloor.get();
								i=0;
							}
						}
						
						logger.info(this.getName()+"->"+ this.currentDirection.toString()+"  ["+this.currentFloor.get()+"]");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				logger.info(this.getName()+"->"+"Door is Opening.." + " ["+this.currentFloor.get()+"]");
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				logger.info(this.getName()+"->"+"Door is closing.." + " ["+this.currentFloor.get()+"]");
				
				logger.info(this.getName()+"->"+"Current Floor :"+this.currentFloor);
			}
			
			if(!this.upwardRequestsForNextRun.isEmpty()){
				logger.info(this.getName()+"->"+"Next Run Queue for upward Direction: "+this.upwardRequestsForNextRun);
			}
			if(!this.downwardRequestsForNextRun.isEmpty()){
				logger.info(this.getName()+"->"+"Next Run Queue for downward Direction: "+this.downwardRequestsForNextRun);
			}
			liftRequestHandler.loadNextSetOfRequests(this);
		}
		
		MDC.remove("logFileName");
		
	}
	
	public void moveLiftToPickFirstRequestOfNextRun(){
		int nextSTop=this.currentRunningQueue.peek();
		logger.info("Moving "+this.getName()+" to pick next request..");
		
		if(this.currentFloor.get()>nextSTop){
			this.currentDirection=Direction.MOVINGDOWN;
			for(int i=this.currentFloor.get(); i>nextSTop;i--){
				this.currentFloor.getAndDecrement();
				logger.info(this.getName()+"->"+ this.currentDirection.toString()+"  ["+this.currentFloor.get()+"]");
			}
		}
		
		if(this.currentFloor.get()<nextSTop){
			this.currentDirection=Direction.MOVINGUP;
			for(int i=this.currentFloor.get(); i<nextSTop;i++){
				this.currentFloor.getAndIncrement();
				logger.info(this.getName()+"->"+ this.currentDirection.toString()+"  ["+this.currentFloor.get()+"]");
			}
		}
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
