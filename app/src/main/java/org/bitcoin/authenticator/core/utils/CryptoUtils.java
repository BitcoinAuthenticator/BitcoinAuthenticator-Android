package org.bitcoin.authenticator.core.utils;

import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
	public static SecretKey deriveSecretKeyFromPasswordString(String s) {
		if(s == null || s.length() == 0)
			throw new IllegalArgumentException("No password passed");
		char[] password = new char[s.length()];
		for (int i=0; i < s.length(); i++){
			password[i] = s.charAt(i);
		}
		byte[] salt = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		SecretKeyFactory kf = null;
		try {kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");} 
		catch (NoSuchAlgorithmException e1) {e1.printStackTrace();	}
		PBEKeySpec spec = new PBEKeySpec(password, salt, 8192, 256);
		SecretKey tmp = null;
		try {
			tmp = kf.generateSecret(spec);
			return tmp;
		} 
		catch (InvalidKeySpecException e1) {e1.printStackTrace();}
		return null;
	}

    public static byte[] encryptPayloadWithChecksum(SecretKey secretKey, byte[] payload) throws IOException, GeneralSecurityException {
        if(payload.length == 0)
            throw new IllegalArgumentException("payload of size 0");
        //Calculate the HMAC
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] macbytes = mac.doFinal(payload);
        //Concatenate it with the payload
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(payload);
        outputStream.write(macbytes);
        byte payloadWithChecksum[] = outputStream.toByteArray();

        return encryptPayload(secretKey, payloadWithChecksum);
    }

	public static byte[] encryptPayload(SecretKey secretKey, byte[] payload) {
		if(secretKey == null || payload == null || payload.length == 0)
			throw new IllegalArgumentException("Illegal encryption parameters");
		SecretKey AESKey = new SecretKeySpec(secretKey.getEncoded(), "AES");
		Cipher cipher = null;
    	try {cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");} 
    	catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
    	catch (NoSuchPaddingException e) {e.printStackTrace();}
    	try {cipher.init(Cipher.ENCRYPT_MODE, AESKey);} 
    	catch (InvalidKeyException e) {e.printStackTrace();}
    	
    	return encryptPayload(cipher, payload);
	}
	
	public static byte[] encryptPayload(Cipher cipher, byte[] payload) {
		if(cipher == null || payload == null || payload.length == 0)
			throw new IllegalArgumentException("Illegal encryption parameters");
		
		byte[] cipherBytes = null;
		try {
			cipherBytes = cipher.doFinal(payload);
			return cipherBytes;
		} 
    	catch (IllegalBlockSizeException e) {e.printStackTrace();} 
    	catch (BadPaddingException e) {e.printStackTrace();}
		return null;
	}
	
	public static byte[] decryptPayload(SecretKey secretKey, byte[] payload) {
		if(secretKey == null || payload == null || payload.length == 0)
			throw new IllegalArgumentException("Illegal encryption parameters");
		
		SecretKey AESKey = new SecretKeySpec(secretKey.getEncoded(), "AES");
		Cipher cipher = null;
		try {cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");} 
		catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
		catch (NoSuchPaddingException e) {e.printStackTrace();}
	    try {cipher.init(Cipher.DECRYPT_MODE, AESKey);} 
	    catch (InvalidKeyException e) {e.printStackTrace();}
	    
	    return decryptPayload(cipher, payload);
	}
	
	public static byte[] decryptPayload(Cipher cipher, byte[] payload) {
		if(cipher == null || payload == null || payload.length == 0)
			throw new IllegalArgumentException("Illegal encryption parameters");
		
		try {
			return new String(cipher.doFinal(payload)).getBytes();
		} catch (IllegalBlockSizeException e) { e.printStackTrace(); }
		catch (BadPaddingException e) { e.printStackTrace(); }
		return null;
	}

    public static byte[] decryptPayloadWithChecksum(SecretKey secretKey, byte[] cipherBytes) throws GeneralSecurityException {
        if(cipherBytes.length == 0)
            throw new IllegalArgumentException("payload of size 0");

        //Decrypt the payload
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        //Split the payload into it's parts.
        String payload = Hex.toHexString(cipher.doFinal(cipherBytes));
        byte[] testpayload = Hex.decode(payload.substring(0,payload.length()-64));
        byte[] hash = Hex.decode(payload.substring(payload.length()-64,payload.length()));

        //Verify the HMAC
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] macbytes = mac.doFinal(testpayload);
        if (Arrays.equals(macbytes, hash))
            return testpayload;
        else
            throw new GeneralSecurityException("Checksum was not verified");
    }
}
