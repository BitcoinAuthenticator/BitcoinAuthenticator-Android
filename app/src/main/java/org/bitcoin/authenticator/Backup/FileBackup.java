package org.bitcoin.authenticator.Backup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.crypto.SecretKey;

import org.apache.commons.io.IOUtils;
import org.bitcoin.authenticator.Backup.Exceptions.CannotBackupToFileException;
import org.bitcoin.authenticator.Backup.Exceptions.CannotRestoreBackupFileException;
import org.bitcoin.authenticator.core.utils.CryptoUtils;

import android.os.Environment;

public class FileBackup {
	static public String FILE_BACKUP_NAME = "Bitcoin_Authenticator_Seed_Backup";
	
	public static void backupToFile(String mnemonic, String passwordStr) throws CannotBackupToFileException {
		try {
			File file = new File(getBackupFileAbsolutePath());
	    	FileOutputStream f = new FileOutputStream(file);
			f.write(prepareBackupFileContent(mnemonic, passwordStr));
			f.close();
		}
		catch (Exception e) {
			throw new CannotBackupToFileException("Failed to backup wallet seed to file");
		}		
	}
	
	public static byte[] prepareBackupFileContent(String mnemonic, String passwordStr) {
		SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString(passwordStr);
    	byte[] cipherBytes = CryptoUtils.encryptPayload(sk, mnemonic.getBytes());
    	return cipherBytes;
	}
	
	public static String getAndDecryptBackupFileContent(String path, String passwordStr) throws CannotRestoreBackupFileException {
		try {
			File file = new File(path);
	        InputStream in = new BufferedInputStream(new FileInputStream(file));
	     	final byte[] cipherbytes = IOUtils.toByteArray(in);
	     	in.close();
	     	
	     	SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString(passwordStr);
	     	return new String(CryptoUtils.decryptPayload(sk, cipherbytes));
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new CannotRestoreBackupFileException("Failed to restore backup file");
		}
		    
	}
	/**
	 * also makes the dir if not existing
	 * @return
	 */
	public static String getBackupFileFolderPath() {
		File storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File dir = new File (storage.getAbsolutePath() + "/backups/");
		if(!dir.exists())
			dir.mkdir();
		return dir.getAbsolutePath();
	}
	
	public static String getBackupFileAbsolutePath() {
		File dir = new File(getBackupFileFolderPath());
		return new File(dir, FILE_BACKUP_NAME).getAbsolutePath();
	}
}
