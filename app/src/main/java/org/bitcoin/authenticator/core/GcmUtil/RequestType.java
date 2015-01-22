package org.bitcoin.authenticator.core.GcmUtil;

public enum RequestType {
	test 								( 1 ), // 1
	signTx 								( 2 ), // 2
	updateIpAddressesForPreviousMessage ( 4 ), // 4
	CoinsReceived						( 6 );
	;
	private final int id;
	RequestType(int id) { this.id = id; }
    public int getValue() { return id; }
}
