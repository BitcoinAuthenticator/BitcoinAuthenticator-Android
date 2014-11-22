package org.bitcoin.authenticator.test.backup;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.bitcoin.authenticator.Backup.FileBackup;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class FileBackupTest extends TestCase{

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

}
