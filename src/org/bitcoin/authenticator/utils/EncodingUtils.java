package org.bitcoin.authenticator.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Formatter;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.DeterministicKey;

public class EncodingUtils {
	
    private static String hexEncode(byte[] bytes) {
        return new String(Hex.encode(bytes));
    }
    
    /**Converts a byte array to a hex string*/
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**Converts a hex string to a byte array*/
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static String getStringTransaction(Transaction tx)
	{
		//Convert tx to byte array for sending.
		String formatedTx = null;
		final StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		try {
		    ByteArrayOutputStream os = new ByteArrayOutputStream();
		    tx.bitcoinSerialize(os);
		    byte[] bytes = os.toByteArray();
		    for (byte b : bytes) {
		        formatter.format("%02x", b);  
		    }
		    formatedTx = sb.toString();
		}catch (IOException e) {
		} finally {
		    formatter.close();
		}
		return formatedTx;
	}
}
