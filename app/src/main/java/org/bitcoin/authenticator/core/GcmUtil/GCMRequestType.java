package org.bitcoin.authenticator.core.GcmUtil;

public enum GCMRequestType {
	test 								( 1 ),
	signTx 								( 2 ),
	updateIpAddressesForPreviousMessage ( 4 ),
	CoinsReceived						( 6 );
	;
	private final int id;
	GCMRequestType(int id) { this.id = id; }
    public int getValue() { return id; }

    public static GCMRequestType fromInt(int type) {
        switch(type){
            case 1:
                return GCMRequestType.test;
            case 2:
                return GCMRequestType.signTx;
            case 4:
                return GCMRequestType.updateIpAddressesForPreviousMessage;
            case 6:
                return GCMRequestType.CoinsReceived;
        }
        return null;
    }
}
