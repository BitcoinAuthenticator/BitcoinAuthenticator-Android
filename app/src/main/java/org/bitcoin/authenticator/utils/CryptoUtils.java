package org.bitcoin.authenticator.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
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
}
