package com.oracle.exceptions;
/**
 * This exception will be thrown when an error occurs while setting up the lift system.
 * @author user
 *
 */
public class RequestRejectionException extends Exception{
	
	public RequestRejectionException(String message) {
		super(message);
	}

}
