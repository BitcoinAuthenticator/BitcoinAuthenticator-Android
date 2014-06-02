package org.bitcoin.authenticator.Events;

public class GlobalEvents {

	static GlobalEvents shared = null;
	
	static public GlobalEvents SharedGlobal()
	{
		if(shared == null){
			shared = new GlobalEvents();
		}
		return shared;
	}
	
	public Event onSetPendingGCMRequestToSeen = new Event();
}
