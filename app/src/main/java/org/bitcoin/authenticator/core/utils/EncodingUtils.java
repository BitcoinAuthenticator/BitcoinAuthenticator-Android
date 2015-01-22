package org.bitcoin.authenticator.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Formatter;

import org.bitcoinj.core.Transaction;

public class EncodingUtils {
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
