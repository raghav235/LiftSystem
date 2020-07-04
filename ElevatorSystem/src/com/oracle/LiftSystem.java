package com.oracle;

import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oracle.elevator.constants.LiftConstant;
import com.oracle.elevator.models.Lift;
import com.oracle.elevator.monitoring.LiftMonitoringDaemonThread;
import com.oracle.exceptions.LiftSetupException;
import com.oracle.processors.LiftScheduler;
import com.oracle.requests.Request;
import com.oracle.strategy.RoundRobinScedulingStrategy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * This is the main class to start Lift system.
 * Responsibilities:
 * 1. Setup lift system
 * 2. Get Request from user.
 * @author user
 *
 */
public class LiftSystem {

	static Logger logger= LoggerFactory.getLogger(LiftSystem.class);
	public static Properties prop = null;
	public static volatile boolean isShutdownRequested;
	static{
	        InputStream is = null;
	        try {
	            prop = new Properties();
	            is = LiftSystem.class.getResourceAsStream("/application.properties");
	            prop.load(is);
	            logger.info("lift.system.liftCount: "+prop.getProperty("lift.system.liftCount"));
	            logger.info("lift.system.scheduling.startegy: "+prop.getProperty("lift.system.scheduling.startegy"));
	            logger.info("building.minfloor: "+prop.getProperty("building.minfloor"));
	            logger.info("building.maxfloor: "+prop.getProperty("building.maxfloor"));
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}


	public static void main(String[] args) throws InterruptedException, LiftSetupException {
		setupLiftSystem();
		addSchedulingStrategy();
		addMonitoringThread();
		while (!LiftSystem.isShutdownRequested) {
			displayWelcomeMessage();
			getRequestFromUser();
			stopLiftSystem();
		}
	}

	private static void stopLiftSystem() {
		logger.info("Press Y to stop Any other key to continue Lift system..");
		Scanner sc= new Scanner(System.in);
		String input= sc.next();
		if("Y".equalsIgnoreCase(input)){
			logger.info("Shutdown requested..");
			logger.info(" all lifts..");
			for(Lift lift : LiftScheduler.getScheduler().getAvailableLifts()){
				lift.setShutDownRequested(true);
				logger.info(lift.getName()+" is shutting down..");
			}
			
			LiftSystem.isShutdownRequested=true;
		}
	}

	private static void addMonitoringThread() {

		LiftMonitoringDaemonThread monitoringThread= new LiftMonitoringDaemonThread("Lift-System-Monitoring");
		monitoringThread.start();
	}

	private static void addSchedulingStrategy() throws LiftSetupException {
		if (prop.getProperty("lift.system.scheduling.startegy") == null) {
			throw new LiftSetupException("Missing scheduling.startegy.");
		} else if (LiftConstant.ROUND_ROBIN_STRATEGY
				.equalsIgnoreCase(prop.getProperty("lift.system.scheduling.startegy"))) {
			LiftScheduler.getScheduler().setSchedulingStrategy(new RoundRobinScedulingStrategy());
		} else {
			throw new LiftSetupException("Unknown scheduling.startegy.");
		}
	}

	private static void setupLiftSystem() throws InterruptedException {
		logger.info("Intializing Oracle Lift System.Please wait......");
		int liftCount = Integer.parseInt(prop.getProperty("lift.system.liftCount"));
		int minFloor = Integer.parseInt(prop.getProperty("building.minfloor"));
		int maxFloor = Integer.parseInt(prop.getProperty("building.maxfloor"));

		for (int i = 1; i <= liftCount; i++) {
			logger.info("Initializing Lift : " + i);
			Lift lift = new Lift("Lift- " + i, minFloor, maxFloor, LiftConstant.DEFAULT_MIN_FLOOR);
			lift.start();
			LiftScheduler.getScheduler().getAvailableLifts().add(lift);
			Thread.sleep(1000);
		}

		logger.info("Done.");
	}

	private static void getRequestFromUser() {
		logger.info("Request Lift");
		logger.info("Enter your current floor : ");
		Scanner sc = new Scanner(System.in);
		int startFloor = sc.nextInt();
		logger.info("" + startFloor);

		logger.info("Enter your destination floor : ");
		int endFloor = sc.nextInt();
		logger.info("" + endFloor);
		Request request = new Request(startFloor, endFloor);
		logger.info("Request Created: " + request);
		procesRequest(request);
	}

	private static void procesRequest(Request request) {
		logger.info("Submitting request: " + request + " to Lift Schedular.");
		String response = LiftScheduler.getScheduler().submitRequest(request);
		logger.info("Response: " + response);
	}

	private static void displayWelcomeMessage() {
		System.out.println("******* Welcome to Oracle Lift System **********\n");
	}
}