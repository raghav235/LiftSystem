package com.oracle.elevator.monitoring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.oracle.elevator.models.Lift;
import com.oracle.processors.LiftScheduler;

public class LiftMonitoringDaemonThread extends Thread{
	
	static Logger logger= LoggerFactory.getLogger(LiftMonitoringDaemonThread.class);
	
	public LiftMonitoringDaemonThread(String name){
			this.setName(name);
	        setDaemon(true);
	        logger.info("Daemon thread has been created to monitor application..");
	}
	 
	public void run() {
		try {
			MDC.put("logFileName", getName());
			List<Lift> lifts = LiftScheduler.getScheduler().getAvailableLifts();
			logger.info("Monitoring All lifts :" + lifts);
			while (true) {
				for (Lift lift : lifts) {
					if (!lift.isAlive()) {
						logger.error(lift.getName() + " is not working.");
					}
				}
			}
		} finally {
			MDC.remove("logFileName");
		}
	}

}
