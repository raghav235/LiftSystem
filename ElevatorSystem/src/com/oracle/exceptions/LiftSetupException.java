package com.oracle.exceptions;
/**
 * This exception will be thrown when an error occurs while setting up the lift system.
 * @author user
 *
 */
public class LiftSetupException extends Exception{
	
	public LiftSetupException(String message) {
		super(message);
	}

}
