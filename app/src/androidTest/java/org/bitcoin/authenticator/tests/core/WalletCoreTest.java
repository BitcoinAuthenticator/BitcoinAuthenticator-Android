package org.bitcoin.authenticator.tests.core;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.core.exceptions.NoSeedOrMnemonicsFound;
import org.bitcoin.authenticator.tests.TestUtil;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContext;

public class WalletCoreTest extends AndroidTestCase{

	@Test
	public void testSaveAndRetreiveSeedBytes() {
		WalletCore wc = new WalletCore();
		MockingContext c = new MockingContext(getContext());
		
		String seedStr = "i am the seed";
		byte[] seedBytes = seedStr.getBytes();
		
		wc.saveSeedBytes(c, seedBytes);
		byte[] resultBytes = wc.getSeedBytes(c);
		String result = new String(resultBytes);
		assertTrue(seedStr.equals(result));
	}
	
	@Test
	public void testSaveAndRetreiveMnemonic() {
		String mnemonicStr = "device seven always major morning present level order decline pizza order economy";
		String[] mnemonic = mnemonicStr.split(" ");
		List<String> mnemonicList = new ArrayList<String>(Arrays.asList(mnemonic));
		WalletCore wc = new WalletCore();
		MockingContext c = new MockingContext(getContext());
		
		wc.saveMnemonic(c, mnemonic);
		List<String> mnemonicListResult = null;
		String mnemonicStrResult = null;
		DeterministicSeed dsResult = null;
		try {
			mnemonicListResult = wc.getMnemonic(c);
			mnemonicStrResult = wc.getMnemonicString(c);
			dsResult = wc.getDeterministicSeed(c);
		} catch (NoSeedOrMnemonicsFound e) {
			e.printStackTrace();
			assertTrue(false);
		} 
		
		assertTrue(TestUtil.listEquals(mnemonicList, mnemonicListResult));
		assertTrue(mnemonicStrResult.equals(mnemonicStr));
		assertTrue(TestUtil.listEquals(dsResult.getMnemonicCode(), mnemonicList));
	}

	class MockingContext extends RenamingDelegatingContext {
        private static final String PREFIX = "test.";
        
       public MockingContext(Context context) {
        	super(context, PREFIX);
       }
       
       @Override
       public FileInputStream openFileInput(String name) throws FileNotFoundException {
           return super.openFileInput(name);
       }
       
       @Override
       public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
           return super.openFileOutput(name, mode);
       }
	}
}
