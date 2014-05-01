import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutPoint;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;
import com.google.bitcoin.crypto.TransactionSignature;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;
import com.google.common.collect.ImmutableList;


/**
 * This class is a collection of methods for creating and sending a transaction over to the Authenticator
 */
public class WalletOperation {
	
	static String unsignedTx;
	static ECKey walletKey;
	static Transaction spendtx;
	static ECKey childPubKey;
	static TransactionInput input;

	/**
	 * Sends a transaction message over to the Authenticator.
	 * The message form is as follows:
	 * 1 Byte -- Version (01 is the only version right now)
	 * 4 Bytes -- Child key index
	 * 33 Bytes -- Public key wallet used to create the P2SH address
	 * ? Bytes -- Raw unsigned transaction
	 * 32 Bytes -- HMAC-SHA256 of the above
	 * */
	void sendTX() throws Exception {
		//Create the payload
		byte[] version = hexStringToByteArray("01");
		byte[] childkeyindex = ByteBuffer.allocate(4).putInt(Main.childkeyindex).array();
		byte[] pubkey = walletKey.getPubKey();
		byte[] transaction = hexStringToByteArray(unsignedTx);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(version);
		outputStream.write(childkeyindex);
		outputStream.write(pubkey);
		outputStream.write(transaction);
		byte payload[] = outputStream.toByteArray( );
		//Calculate the HMAC and concatenate it to the payload
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(PairingProtocol.sharedsecret);
		byte[] macbytes = mac.doFinal(payload);
		outputStream.write(macbytes);
		payload = outputStream.toByteArray( );
		//Encrypt the payload
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
      try {
			cipher.init(Cipher.ENCRYPT_MODE, PairingProtocol.sharedsecret);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
      byte[] cipherBytes = null;
		try {
			cipherBytes = cipher.doFinal(payload);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		//Send the encrypted payload over to the Authenticator and wait for the response.
		byte[] cipherKeyBytes;
		if (PairingProtocol.out == null){
			DataOutputStream out = new DataOutputStream(OpenPort.socket.getOutputStream());
			DataInputStream in = new DataInputStream(OpenPort.socket.getInputStream());
			out.writeInt(cipherBytes.length);
			out.write(cipherBytes);
			System.out.println("Sent transaction");
			int keysize = in.readInt();
		    cipherKeyBytes = new byte[keysize];
		    in.read(cipherKeyBytes);
		}
		else {
			PairingProtocol.out.writeInt(cipherBytes.length);
			PairingProtocol.out.write(cipherBytes);
			System.out.println("Sent transaction");
			int keysize = PairingProtocol.in.readInt();
		    cipherKeyBytes = new byte[keysize];
		    PairingProtocol.in.read(cipherKeyBytes);
		}
		//Decrypt the response
	    cipher.init(Cipher.DECRYPT_MODE, PairingProtocol.sharedsecret);
	    String message = bytesToHex(cipher.doFinal(cipherKeyBytes));
	    String sig = message.substring(0,message.length()-64);
	    String HMAC = message.substring(message.length()-64,message.length());
	    byte[] testsig = hexStringToByteArray(sig);
	    byte[] hash = hexStringToByteArray(HMAC);
	    //Calculate the HMAC of the message and verify it is valid
		macbytes = mac.doFinal(testsig);
		if (Arrays.equals(macbytes, hash)){
			System.out.println("Received Signature: " + bytesToHex(testsig));
		    System.out.println("Building Transaction...");
		}
		else {
			System.out.println("Message authentication code is invalid");
		}
		//Create second signature and build the final transaction
	    List<ECKey> keys = ImmutableList.of(childPubKey, walletKey);
		Script scriptpubkey = ScriptBuilder.createMultiSigOutputScript(2,keys);
		byte[] program = scriptpubkey.getProgram();
		TransactionSignature sig1 = TransactionSignature.decodeFromBitcoin(testsig, true);
		TransactionSignature sig2 = spendtx.calculateSignature(0, walletKey, scriptpubkey, Transaction.SigHash.ALL, false);
		List<TransactionSignature> sigs = ImmutableList.of(sig1, sig2);
	    Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(sigs, program);
		input.setScriptSig(inputScript);
		//Convert tx to byte array for sending.
		final StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				spendtx.bitcoinSerialize(os);
				byte[] bytes = os.toByteArray();
				for (byte b : bytes) {
				     formatter.format("%02x", b);  
				}
				System.out.println("Signed Transaction: " + sb.toString());
				//Push the transaction to the network
				pushTx(sb.toString());
				}catch (IOException e) {
					System.out.println("Couldn't serialize to hex string.");
				} finally {
				    formatter.close();
				}
	}
	
	/**Pushes the raw transaction the the Eligius mining pool*/
	void pushTx(String tx) throws IOException{
		System.out.println("Broadcasting to network...");
		String urlParameters = "transaction="+ tx + "&send=Push";
		String request = "http://eligius.st/~wizkid057/newstats/pushtxn.php";
		URL url = new URL(request); 
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod("POST"); 
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
		connection.setUseCaches (false);
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		int responseCode = connection.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		//Get reponse 
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		connection.disconnect();
		//Print txid
		System.out.println("Success!");
		System.out.println("txid: " + response.substring(response.indexOf("string(64) ")+12, response.indexOf("string(64) ")+76));
	}
	
	/**Derive a child public key from the master public key*/
	void childPubKey() throws NoSuchAlgorithmException{
		Main.childkeyindex += 1;
		HDKeyDerivation HDKey = null;
  		DeterministicKey mPubKey = HDKey.createMasterPubKeyFromBytes(PairingProtocol.mPubKey, PairingProtocol.chaincode);
  		DeterministicKey childKey = HDKey.deriveChildKey(mPubKey,Main.childkeyindex);
  		byte[] childpublickey = childKey.getPubKeyBytes();
  		genMultiSigAddr(childpublickey);
  	}
	
	/**Generate a new P2SH address using the master public key from the Authenticator*/
	void genMultiSigAddr(byte[] childkey) throws NoSuchAlgorithmException{
		NetworkParameters params = MainNetParams.get();
		childPubKey = new ECKey(null, childkey);
		//Create a new key pair which will kept in the wallet.
		walletKey = new ECKey();
		byte[] pkey = walletKey.getPubKey();
		List<ECKey> keys = ImmutableList.of(childPubKey, walletKey);
		//Create a 2-of-2 multisig output script.
		byte[] scriptpubkey = Script.createMultiSigOutputScript(2,keys);
		Script script = ScriptBuilder.createP2SHOutputScript(Utils.sha256hash160(scriptpubkey));
		//Create the address
		Address multisigaddr = Address.fromP2SHScript(params, script);
		System.out.println("Child Public Key: " + bytesToHex(childkey));
		System.out.println("Wallet Public Key: " + bytesToHex(pkey));
		System.out.println(" ");
		System.out.println("Address: " + multisigaddr.toString());
	}
	
	/**Gets the unspent outputs JSON for an address from blockchain.info*/
	UnspentOutput getUnspentOutputs(String addr) throws JSONException, IOException{
	    JSONObject json = readJsonFromUrl("http://blockchain.info/address/" + addr + "?format=json");
	    JSONArray array = json.getJSONArray("txs");
	    JSONObject output = array.getJSONObject(0);
	    JSONObject json2 = readJsonFromUrl("http://blockchain.info/unspent?address=" + addr);
	    JSONArray array2 = json2.getJSONArray("unspent_outputs");
	  	JSONObject output2 = array2.getJSONObject(0);
	    UnspentOutput out = new UnspentOutput(output.get("hash").toString(), output2.get("tx_output_n").toString());
		return out;
	}
	
	/**Builds a raw unsigned transaction*/
	void mktx(ArrayList<String> MILLI, String from, ArrayList<String> to) throws AddressFormatException, JSONException, IOException {
		//Gather the data needed to construct the inputs and outputs
		UnspentOutput out = getUnspentOutputs(from);
		int index = Integer.parseInt(out.getIndex());
  		Sha256Hash txhash = new Sha256Hash(out.getTxid());
  		NetworkParameters params = MainNetParams.get();
  		spendtx = new Transaction(params);
  		byte[] script = hexStringToByteArray("");
  		//Creates the input which refrences a previous unspent output
  		TransactionOutPoint outpoint = new TransactionOutPoint(params, index, txhash);
		input = new TransactionInput(params, null, script, outpoint);
		//Add the outputs
		for (int i=0; i<MILLI.size(); i++){
			Address outaddr = new Address(params, to.get(i));
			spendtx.addOutput(BigInteger.valueOf(Integer.parseInt(MILLI.get(i))), outaddr);
		}
		//Add the inputs
		spendtx.addInput(input);
		//Convert tx to byte array for sending.
		final StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		try {
		    ByteArrayOutputStream os = new ByteArrayOutputStream();
		    spendtx.bitcoinSerialize(os);
		    byte[] bytes = os.toByteArray();
		    for (byte b : bytes) {
		        formatter.format("%02x", b);  
		    }
		    System.out.println("Raw Unsigned Transaction: " + sb.toString());
		    unsignedTx = sb.toString();
		}catch (IOException e) {
			System.out.println("Couldn't serialize to hex string.");
		} finally {
		    formatter.close();
		}
	}
    
	/**For reading the JSON*/
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }

	/**Reads JSON object from a URL*/
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException { 
	    URL urladdr = new URL(url);
        URLConnection conn = urladdr.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        BufferedReader rd = null;
	    try {
	      rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      rd.close();
	    }
	  }

	/**Hex encodes a DeterministicKey object*/
	private static String hexEncodePub(DeterministicKey pubKey) {
        return hexEncode(pubKey.getPubKeyBytes());
    }
    private static String hexEncode(byte[] bytes) {
        return new String(Hex.encode(bytes));
    }
    
    /**Converts a byte array to a hex string*/
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**Converts a hex string to a byte array*/
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
    
	/**Defines and object of data that makes up an unspent output*/
    class UnspentOutput {
    	String txid;
    	String index;
    	
    	public UnspentOutput(String id, String in){
    		this.txid = id;
    		this.index = in;
    	}
    	
    	public String getTxid(){
    		return txid;
    	}
    	
    	public String getIndex(){
    		return index;
    	}
    }
    
}


