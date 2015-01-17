package org.bitcoin.authenticator.tests.utils;

import junit.framework.TestCase;

import org.bitcoin.authenticator.utils.EncodingUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by alonmuroch on 1/17/15.
 */
public class EncodingUtilsTest extends TestCase {
    @Test
    public void testTxEncoding() {
        Transaction tx = new Transaction(MainNetParams.get());
        String expected = "01000000000000000000";
        String result = EncodingUtils.getStringTransaction(tx);
        assertTrue(result.equals(expected));


        try {
            tx = new Transaction(MainNetParams.get());
            Address add = new Address(MainNetParams.get(), "1AWaHyKMurhNoiUvtrcQMWhvzttqZpXSbi");
            TransactionOutput out = new TransactionOutput(MainNetParams.get(),
                    tx,
                    Coin.COIN,
                    add);
            tx.addOutput(out);

            expected = "01000000000100e1f505000000001976a9146850fc785883e1bf343b257303a5d5f9d8ba7f0788ac00000000";
            result = EncodingUtils.getStringTransaction(tx);
            assertTrue(result.equals(expected));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
