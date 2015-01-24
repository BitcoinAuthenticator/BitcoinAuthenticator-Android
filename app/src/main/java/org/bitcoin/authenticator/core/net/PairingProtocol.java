package org.bitcoin.authenticator.core.net;


import static com.google.common.base.Preconditions.checkArgument;
import java.math.BigInteger;
import javax.crypto.*;
import org.bitcoin.authenticator.core.utils.CryptoUtils;
import android.util.Log;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.spongycastle.util.encoders.Hex;
import com.google.common.primitives.Ints;
import com.subgraph.orchid.encoders.DecoderException;

/**
 *	Opens a TCP socket connection to the wallet, derives a new master public key, encrypts it
 *  and sends it over to the wallet.
 */
public class PairingProtocol {
 
    private String[] ips;
    
    /**	Constructor creates a new connection object to connect to the wallet. */
    public PairingProtocol(String[] ips) {
    	this.ips = ips;
    }
    
    /**	
     * Takes in Authenticator seed and uses it to derive the master public key and chaincode.
     * Uses the AES key to calculate the message authentication code for the payload and concatenates 
     * it with the master public key and chaincode. The payload is encrypted and sent over to the wallet.
     * @throws CouldNotPairToWalletException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	public void run(byte[] seed, SecretKey AESKey, byte[] regID, long walletIndex) throws CouldNotPairToWalletException {
    	try {
    		//Derive the key and chaincode from the seed.
    		Log.i("asdf", "Pairing: Deriving Wallet Account");
        	HDKeyDerivation HDKey = null;
        	DeterministicKey masterkey = HDKey.createMasterPrivateKey(seed);
        	DeterministicKey childkey = HDKey.deriveChildKey(masterkey,Ints.checkedCast(walletIndex));
        	byte[] chaincode = childkey.getChainCode(); // 32 bytes
        	byte[] mpubkey = childkey.getPubKey(); // 32 bytes
        	
        	//Format data into a JSON object
        	Log.i("asdf", "Pairing: creating payload");
    		byte[] jsonBytes = new PairingPayload(1, mpubkey, chaincode, regID).toString().getBytes();
    		
        	byte[] cipherBytes = CryptoUtils.encryptPayloadWithChecksum(AESKey, jsonBytes);

        	//Send the payload over to the wallet
        	Connection.getInstance().writeAndClose(ips, cipherBytes);
    	}
    	catch (Exception e) {
    		throw new CouldNotPairToWalletException("Could not pair to wallet: " + e.getMessage());
    	}
    	
	  }
    
    public static class PairingQRData {
    	public String AESKey;
    	public String PublicIP;
    	public String LocalIP;
    	public String walletType;
    	public String pairingName;
    	public int networkType;
    	public long walletIndex;
    }
    
    /**
     * Takes payload parsed from the QR image andreturns a {@link PairingProtocol.PairingQRData PairingQRData} object
     * 
     * @param QRInput
     * @return
     */
    public static PairingQRData parseQRString(String QRInput) {
    	if(!checkQRDataValidity(QRInput))	
    		return null;
    	
    	PairingQRData ret = new PairingQRData();
    	
    	ret.AESKey = QRInput.substring(QRInput.indexOf("AESKey=")+7, QRInput.indexOf("&PublicIP="));
    	ret.PublicIP = QRInput.substring(QRInput.indexOf("&PublicIP=")+10, QRInput.indexOf("&LocalIP="));
    	ret.LocalIP = QRInput.substring(QRInput.indexOf("&LocalIP=")+9, QRInput.indexOf("&pairingName="));
    	ret.pairingName = QRInput.substring(QRInput.indexOf("&pairingName=")+13, QRInput.indexOf("&WalletType="));
    	ret.walletType = QRInput.substring(QRInput.indexOf("&WalletType=")+12, QRInput.indexOf("&NetworkType="));
		/**
		 * 1 for main net, 0 for testnet
		 */
    	ret.networkType = Integer.parseInt(QRInput.substring(QRInput.indexOf("&NetworkType=")+13, QRInput.indexOf("&index=")));
		
		/**
		 * get index
		 */
		String walletIndexHex = QRInput.substring(QRInput.indexOf("&index=")+7, QRInput.length());
		ret.walletIndex = getWalletIndexFromString(walletIndexHex);
    	
    	return ret;
    }

    /**
     * Will return -1 if hex is not a real hex string
     * @param hex
     * @return
     */
    public static long getWalletIndexFromString(String hex) {
        checkArgument(hex != null);
        try {
            return new BigInteger(Hex.decode(hex)).longValue();
        }
        catch (Exception e) { return -1; }
    }
    
    /**
     * Checks to see all necessary data elements are present
     * 
     * @param data
     * @return
     */
    private static boolean checkQRDataValidity(String data) {
    	if(!data.contains("AESKey"))
    		return false;
    	
    	if(!data.contains("&PublicIP"))
    		return false;
    	
    	if(!data.contains("&LocalIP"))
    		return false;

        if(!data.contains("&pairingName"))
            return false;
    	
    	if(!data.contains("&WalletType"))
    		return false;
    	
    	if(!data.contains("&NetworkType"))
    		return false;
    	
    	if(!data.contains("&index"))
    		return false;
    	
    	return true;
    }
    
    static public class CouldNotPairToWalletException extends Exception {
		public CouldNotPairToWalletException(String str) {
			super(str);
		}
	}
   
}