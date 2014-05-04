package dispacher;

import javax.crypto.SecretKey;

public class Device {
	public  byte[] chaincode;
	public  byte[] mPubKey;
	public  byte[] gcmRegId;
	public  SecretKey sharedsecret;
	
	public Device(){ }
	public Device(byte[] chain,byte[] pubKey, byte[] gcm,SecretKey secret)
	{
		chaincode = chain;
		mPubKey = pubKey;
		gcmRegId = gcm;
		sharedsecret = secret;
	}
}
