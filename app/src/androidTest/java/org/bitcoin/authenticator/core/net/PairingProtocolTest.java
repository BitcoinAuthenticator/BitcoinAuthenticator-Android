package org.bitcoin.authenticator.core.net;

import junit.framework.TestCase;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by alonmuroch on 1/24/15.
 */
public class PairingProtocolTest extends TestCase {
    @Test
    public void testParseValidQRCode() {
        String walletIdx = Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array());
        String qrStr = "AESKey=AESKey&PublicIP=publicIP&LocalIP=localIP&pairingName=pairingName&WalletType=1&NetworkType=1&index=" + walletIdx;

        PairingProtocol.PairingQRData result = PairingProtocol.parseQRString(qrStr);
        assertEquals(result.AESKey, "AESKey");
        assertEquals(result.PublicIP, "publicIP");
        assertEquals(result.LocalIP, "localIP");
        assertEquals(result.pairingName, "pairingName");
        assertEquals(result.walletType, "1");
        assertEquals(result.networkType, 1);
        assertEquals(result.walletIndex, (long)123456);
    }

    @Test
    public void testParseInValidQRCode() {
        String walletIdx = Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array());
        String qrStr = "AESKey=AESKeyPublicIP=publicIP&LocalIP=localIP&pairingName=pairingName&WalletType=1&NetworkType=1&index=" + walletIdx;
        PairingProtocol.PairingQRData result = PairingProtocol.parseQRString(qrStr);
        assertNull(result);

        /**
         * the QR string parsing separates the key-value members by '&', removing it should fail .
         */

        qrStr = "AESKey=AESKey&PublicIP=publicIPLocalIP=localIP&pairingName=pairingName&WalletType=1&NetworkType=1&index=" + walletIdx;
        result = PairingProtocol.parseQRString(qrStr);
        assertNull(result);

        qrStr = "AESKey=AESKey&PublicIP=publicIP&LocalIP=localIPpairingName=pairingName&WalletType=1&NetworkType=1&index=" + walletIdx;
        result = PairingProtocol.parseQRString(qrStr);
        assertNull(result);

        qrStr = "AESKey=AESKey&PublicIP=publicIP&LocalIP=localIP&pairingName=pairingNameWalletType=1&NetworkType=1&index=" + walletIdx;
        result = PairingProtocol.parseQRString(qrStr);
        assertNull(result);

        qrStr = "AESKey=AESKey&PublicIP=publicIP&LocalIP=localIP&pairingName=pairingName&WalletType=1NetworkType=1&index=" + walletIdx;
        result = PairingProtocol.parseQRString(qrStr);
        assertNull(result);

        qrStr = "AESKey=AESKey&PublicIP=publicIP&LocalIP=localIP&pairingName=pairingName&WalletType=1&NetworkType=1index=" + walletIdx;
        result = PairingProtocol.parseQRString(qrStr);
        assertNull(result);
    }

    @Test
    public void testGetWalletIndexFromString() {
        for(int i=0; i<1000; i++) {
            Random rand = new Random();
            Long randomNum = rand.nextLong();

            String hex = Hex.toHexString(ByteBuffer.allocate(8).putLong(randomNum).array());
            Long result = PairingProtocol.getWalletIndexFromString(hex);
            assertEquals(randomNum, result);
        }

        boolean didThrow = false;
        try {
            PairingProtocol.getWalletIndexFromString(null);
        }
        catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            didThrow = true;
        }
        assertTrue(didThrow);

        long result = PairingProtocol.getWalletIndexFromString("not a hex string");
        assertEquals(result, (long)-1);
    }
}
