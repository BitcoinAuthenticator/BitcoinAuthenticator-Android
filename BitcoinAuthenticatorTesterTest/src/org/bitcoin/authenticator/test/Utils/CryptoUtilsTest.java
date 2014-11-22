package org.bitcoin.authenticator.test.Utils;

import static org.junit.Assert.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.TestCase;

import org.bitcoin.authenticator.utils.CryptoUtils;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class CryptoUtilsTest extends TestCase {
	@Test
	public void testDeriveSecretKeyFromPasswordString() {
		/* check legal */
		String expected = "990d5bcf1effcefe1b1821a446cc1edf7f6e5d9e2c565b64593c6da560e326ce";
		String result;
		
		String pw = "1234";
		SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString(pw);
		result = Hex.toHexString(sk.getEncoded());
		assertTrue (expected.equals(result));
		
		/* null password */
		Exception e = null;
		try {
			CryptoUtils.deriveSecretKeyFromPasswordString(null);
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
		
		/* zero length password */
		e = null;
		try {
			CryptoUtils.deriveSecretKeyFromPasswordString("");
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
	}
	
	@Test
	public void testEncryptPayload() {
		/* valide test */
		String expected = "901611d3908460ebc2c4dae4a59d9690cb90993853ef7ff593fbadc0798e1271";
		String result;
		
		byte[] payload = "I am the payload".getBytes();
		SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString("password");
		
		byte[] encryptedPayload = CryptoUtils.encryptPayload(sk, payload);
		result = Hex.toHexString(encryptedPayload);
		assertTrue (expected.equals(result));
		
		/* wrong password*/
		sk = CryptoUtils.deriveSecretKeyFromPasswordString("passwor");
		encryptedPayload = CryptoUtils.encryptPayload(sk, payload);
		result = Hex.toHexString(encryptedPayload);
		assertFalse (expected.equals(result));
		
		/* illegal arguments*/
		Exception e = null;
		try {
			CryptoUtils.encryptPayload(sk, new byte[0]);
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
		
		e = null;
		try {
			CryptoUtils.encryptPayload(sk, null);
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
		
		e = null;
		try {
			sk = null;
			CryptoUtils.encryptPayload(sk, payload);
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
		
	}
}
