package org.bitcoin.authenticator.GcmUtil;

public enum RequestType {
	test 								( 1 ), // 1
	signTx 								( 2 ), // 2
	updateIpAddressesForPreviousMessage ( 4 ) // 4
	;
	;
	private final int id;
	RequestType(int id) { this.id = id; }
    public int getValue() { return id; }
}
