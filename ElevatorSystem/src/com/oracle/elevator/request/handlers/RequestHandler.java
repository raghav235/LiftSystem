package com.oracle.elevator.request.handlers;

import com.oracle.elevator.models.Lift;
import com.oracle.requests.Request;

public interface RequestHandler {

	public boolean validateRequest(Lift lift, Request request);
	public String addRequst(Lift lift, Request  request);
	public void loadNextSetOfRequests(Lift lift);
}
