package org.bitcoin.authenticator.core.utils;

import javax.crypto.SecretKey;

import junit.framework.TestCase;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.GeneralSecurityException;

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
    public void testEncryptPayloadWithChecksum() {
        /* valide test */
        String expected = "901611d3908460ebc2c4dae4a59d9690a59bf24c470a79986125574b74b66383bf6553955c9cd84dc7a1a3f08abe6003cb90993853ef7ff593fbadc0798e1271";
        String result;

        byte[] payload = "I am the payload".getBytes();
        SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString("password");

        byte[] encryptedPayload = new byte[0];
        try {
            encryptedPayload = CryptoUtils.encryptPayloadWithChecksum(sk, payload);
            result = Hex.toHexString(encryptedPayload);
            assertTrue(expected.equals(result));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            /* wrong password*/
            sk = CryptoUtils.deriveSecretKeyFromPasswordString("passwor");
            encryptedPayload = CryptoUtils.encryptPayloadWithChecksum(sk, payload);
            result = Hex.toHexString(encryptedPayload);
            assertFalse (expected.equals(result));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

		/* illegal arguments*/
        Exception e = null;
        try {
            CryptoUtils.encryptPayload(sk, new byte[0]);
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
	
	@Test
	public void testDecryptPayload() {
		String expected = "I am the payload";
		String pw = "password";
		
		byte[] payload = expected.getBytes();
		SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString(pw);
		byte[] encryptedPayload = CryptoUtils.encryptPayload(sk, payload);
		byte[] decryptedPayload = CryptoUtils.decryptPayload(sk, encryptedPayload);
		
		assertTrue(expected.equals(new String(decryptedPayload)));
		
		/* Wrong password */
		sk = CryptoUtils.deriveSecretKeyFromPasswordString("passwor");
		decryptedPayload = CryptoUtils.decryptPayload(sk, encryptedPayload);
		assertTrue(decryptedPayload == null);
		
		/* illegal arguments */
		Exception e = null;
		try {
			CryptoUtils.decryptPayload(sk, new byte[0]);
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
		
		e = null;
		try {
			CryptoUtils.decryptPayload(sk, null);
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
		
		e = null;
		try {
			sk = null;
			CryptoUtils.decryptPayload(sk, payload);
		} catch (Exception ex) {
		    e = ex;
		}
		assertTrue(e instanceof IllegalArgumentException);
	}

    @Test
    public void testDecryptPayloadWithChecksum() {
        String expected = "I am the payload";
        String pw = "password";
        byte[] payload = expected.getBytes();
        SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString(pw);
        byte[] encryptedPayload = Hex.decode("901611d3908460ebc2c4dae4a59d9690a59bf24c470a79986125574b74b66383bf6553955c9cd84dc7a1a3f08abe6003cb90993853ef7ff593fbadc0798e1271");

        try {
            byte[] decryptedPayload = CryptoUtils.decryptPayloadWithChecksum(sk, encryptedPayload);
            assertTrue(expected.equals(new String(decryptedPayload)));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        /* Wrong password */
        boolean didThrow = false;
        try {
            sk = CryptoUtils.deriveSecretKeyFromPasswordString("passwor");
            byte[]decryptedPayload = CryptoUtils.decryptPayloadWithChecksum(sk, encryptedPayload);
        } catch (Exception e) {
            didThrow = true;
            assertTrue(e instanceof GeneralSecurityException);
        }
        assertTrue(didThrow);


		/* illegal arguments */
        Exception e = null;
        try {
            CryptoUtils.decryptPayload(sk, new byte[0]);
        } catch (Exception ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalArgumentException);
    }
}
