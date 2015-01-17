package org.bitcoin.authenticator.net;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bitcoin.authenticator.TxData;
import org.bitcoin.authenticator.Utils;
import org.bitcoin.authenticator.net.exceptions.CouldNotGetTransactionException;
import org.bitcoin.authenticator.net.exceptions.CouldNotSendEncryptedException;
import org.bitcoin.authenticator.net.exceptions.CouldNotSendRequestIDException;
import org.bitcoin.authenticator.utils.CryptoUtils;
import org.json.simple.JSONObject;

/**
 * This class handles the communication messages sent between the Authenticator and the wallet.
 */
public class Message {
	
	private String[] ips;
	
	/**Constructor takes in active Connection object to the wallet*/
	public Message(String[] ips){
		if (ips == null || ips.length == 0)
			throw new IllegalArgumentException("No ips were provided");
		setIPs(ips);
	}

    public byte[] getSendRequestIDPayload(String requestID, String walletID) {
        JSONObject jo = new JSONObject();
        jo.put("requestID", requestID);
        jo.put("pairingID", walletID); // the walletID in the authenticator is the pairing id in the wallet
        return jo.toString().getBytes();
    }

	public Socket sendRequestID(String requestID, String walletID) throws CouldNotSendRequestIDException {
		try {
			byte[] payload = getSendRequestIDPayload(requestID, walletID);
			return Connection.getInstance().writeContinuous(getIPs(), payload);
		}
		catch(Exception e) {
            e.printStackTrace();
			throw new CouldNotSendRequestIDException("Couldn't send request ID to wallet");
		}
	}
	
	/**
	 * Method to receive a transaction from the wallet.
	 *
	 */
	public TxData receiveTX(SecretKey sharedsecret, Socket s) throws CouldNotGetTransactionException {
        try {
            byte[] payload = Connection.getInstance().readContinuous(s);
            return parseTxPayload(sharedsecret, payload);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new CouldNotGetTransactionException(e.getMessage());
        }
    }

    /**
     * Returns a TxData object containing the number of inputs, child key indexes, public keys from the wallet, and
     * raw unsigned transaction.
     *
     * @param sharedsecret
     * @param cipherBytes
     * @return
     * @throws CouldNotGetTransactionException
     */
    public TxData parseTxPayload(SecretKey sharedsecret, byte[] cipherBytes) throws CouldNotGetTransactionException {
        try {
            byte[] payload = CryptoUtils.decryptPayloadWithChecksum(sharedsecret, cipherBytes);

            // in case wallet couldn't process request
            String response = CannotProcessRequestPayload.isCannotBeProcessedPayload(payload);
            if(response != null)
                throw new CouldNotGetTransactionException(response);

            return new TxData(payload);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new CouldNotGetTransactionException(e.getMessage());
        }
	}

    /**
     * Method to send the transaction signature back to the wallet.
     * It calculates the HMAC of the signature, concatenates it, and encrypts it with AES.
     *
     * @param payload
     * @param sharedsecret
     * @param s
     * @throws CouldNotSendEncryptedException
     */
	public void sendEncrypted (byte[] payload, SecretKey sharedsecret, Socket s) throws CouldNotSendEncryptedException{
		try {
			byte[] cipherBytes = CryptoUtils.encryptPayloadWithChecksum(sharedsecret, payload);
			Connection.getInstance().writeAndClose(s, cipherBytes);
		}
		catch(Exception e) {
			throw new CouldNotSendEncryptedException("Couldn't send encrypted payload");
		}
		
    }

    public void setIPs(String[] ips) {
        this.ips = ips;
    }

    public String[] getIPs() {
        return this.ips;
    }
}
