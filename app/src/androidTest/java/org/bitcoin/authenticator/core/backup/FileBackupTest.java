package org.bitcoin.authenticator.core.backup;

import java.io.File;

import junit.framework.TestCase;

import org.bitcoin.authenticator.core.backup.FileBackup;
import org.bitcoin.authenticator.core.backup.Exceptions.CannotBackupToFileException;
import org.bitcoin.authenticator.core.backup.Exceptions.CannotRestoreBackupFileException;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class FileBackupTest extends TestCase {

	@Test
	public void testPrepareBackupFileContent() {
		String expected = "43f9f0b1a04b5aac860081c4492ee829137ddf8185b85aa0e8211c82a7a5f002eb2b942ebfab08b063f188f959b2e25d";
		String result;
		
		String pw = "password";
		String mnemonic = "one two three four five six seven";
		byte[] bytes = FileBackup.prepareBackupFileContent(mnemonic, pw);
		result = Hex.toHexString(bytes);
		assertTrue(expected.equals(result));
	}
	
	@Test
	public void testBackupAndRestoreContent() {
		String mnemonic = "one two three four five six seven";
		String pw = "password";
		
		/* backup */
		try {
			FileBackup.backupToFile(mnemonic, pw);
		} catch (CannotBackupToFileException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		/* restore */
		String result = null;
		try {
			result = FileBackup.getAndDecryptBackupFileContent(FileBackup.getBackupFileAbsolutePath(), pw);
		} catch (CannotRestoreBackupFileException e) {
			e.printStackTrace();
		}
		assertTrue(result.equals(mnemonic));
		
		/* wrong password */
		Exception ex = null;
		try {
			result = FileBackup.getAndDecryptBackupFileContent(FileBackup.getBackupFileAbsolutePath(), "passwor");
		} catch (CannotRestoreBackupFileException e) {
			e.printStackTrace();
			ex = e;
		}
		assertTrue(ex instanceof CannotRestoreBackupFileException);
		
		/* dispose of backup file */
		File f = new File(FileBackup.getBackupFileAbsolutePath());
		if(f.exists()) {
			f.delete();
		}
	}
}
