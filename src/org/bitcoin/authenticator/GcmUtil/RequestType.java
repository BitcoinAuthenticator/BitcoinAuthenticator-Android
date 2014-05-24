package org.bitcoin.authenticator.GcmUtil;

public enum RequestType {
	test (1 << 0), // 1
	signTx (1 << 1) // 2
	;
	;
	private final int id;
	RequestType(int id) { this.id = id; }
    public int getValue() { return id; }
}
