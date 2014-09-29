package org.bitcoin.authenticator.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import org.bitcoin.authenticator.core.exceptions.NoSeedOrMnemonicsFound;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.bitcoin.core.Utils;
import com.google.bitcoin.crypto.MnemonicCode;
import com.google.bitcoin.crypto.MnemonicException.MnemonicChecksumException;
import com.google.bitcoin.crypto.MnemonicException.MnemonicLengthException;
import com.google.bitcoin.crypto.MnemonicException.MnemonicWordException;
import com.google.bitcoin.wallet.DeterministicSeed;

public class WalletCore {
	
	static public String SEED_FILE_NAME = "seed";
	static public String MNEMONIC_FILE_NAME = "mnemonic";

	/**
	 * This method implements BIP39 to generate a 512 bit seed from 128 bit checksummed entropy. The seed and the
	 * mnemonic encoded entropy are saved to internal storage.
	 */
	@SuppressLint("TrulyRandom")
	public DeterministicSeed generateSeed(Context c, boolean shouldSave){
		//Generate 128 bits entropy.
        SecureRandom secureRandom = null;
		try {
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] bytes = new byte[16];
		secureRandom.nextBytes(bytes);
		MnemonicCode ms = null;
		try {
			ms = new MnemonicCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> mnemonic = null;
		try {
		mnemonic = ms.toMnemonic(bytes);
		} catch (MnemonicLengthException e) {
			e.printStackTrace();
		}
		
		DeterministicSeed HDSeed = new DeterministicSeed(mnemonic, null, "", Utils.currentTimeSeconds());
		
		if(shouldSave){
			saveSeedBytes(c, MnemonicCode.toSeed(mnemonic, ""));
			saveMnemonic(c, mnemonic.toArray(new String[0]));
		}
		
		return HDSeed;
	}
	
	public void saveSeedBytes(Context c, byte[] seed){
		try
		{
		    //String FILENAME = "seed";
		    FileOutputStream outputStream = c.openFileOutput(SEED_FILE_NAME, Context.MODE_PRIVATE);
		    outputStream.write(seed);
		    outputStream.close();
		}
		catch (final Exception ex) { Log.e("JAVA_DEBUGGING", "Exception while creating save file!"); ex.printStackTrace(); }
	}
	
	public void saveMnemonic(Context c, String[] strArray){
		String strMnemonic = Arrays.toString(strArray);
		strMnemonic = strMnemonic.replace("[", "");
		strMnemonic = strMnemonic.replace("]", "");
		strMnemonic = strMnemonic.replace(",", "");
		//Save to private internal storage.
		try
		{
		    //String FILENAME = "mnemonic";
		    FileOutputStream outputStream = c.openFileOutput(MNEMONIC_FILE_NAME, Context.MODE_PRIVATE);
		    outputStream.write(strMnemonic.getBytes());
		    outputStream.close();
		}
		catch (final Exception ex) { Log.e("JAVA_DEBUGGING", "Exception while creating save file!"); ex.printStackTrace(); }
	}
	
	public List<String> getMnemonic(Context c) throws NoSeedOrMnemonicsFound{
		String mnemonicStr = getMnemonicString(c);
		return Arrays.asList(mnemonicStr.split(" "));
	}
	public String getMnemonicString(Context c) throws NoSeedOrMnemonicsFound{
		FileInputStream fin = null;
		try {
			fin = c.openFileInput(MNEMONIC_FILE_NAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new NoSeedOrMnemonicsFound("Failed to find generated seed or mnemonics");
		}
		int chr;
		String temp="";
		try {
			while( (chr = fin.read()) != -1){
			   temp = temp + Character.toString((char)chr);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//String temp contains all the data of the file.
		try {
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return temp;
	}
	
	public DeterministicSeed getDeterministicSeed(Context c) throws Exception{
		List<String> mnemonic = getMnemonic(c);
		MnemonicCode ms = new MnemonicCode();
		byte[] entropy = ms.toEntropy(mnemonic);
		DeterministicSeed seed = new DeterministicSeed(entropy, "", Utils.currentTimeSeconds());
		return seed;
	}
		
}
