package wallet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;
import com.google.common.collect.ImmutableList;

import dispacher.Device;
import dispacher.Dispacher;
import dispacher.MessageType;


/**
 * This class is a collection of methods for creating and sending a transaction over to the Authenticator
 */
public class WalletOperation {
	
	static String unsignedTx;
	static Transaction spendtx;
	static int numInputs;
	static ArrayList<byte[]> publickeys;
	static ArrayList<Integer> childkeyindex;
	static Boolean testnet;
	
	public WalletOperation() throws IOException{
		String filePath = new java.io.File( "." ).getCanonicalPath() + "/wallet.json";
		File f = new File(filePath);
		if(f.exists() && !f.isDirectory()) {
			WalletFile file = new WalletFile();
			testnet = file.getTestnet();
		}
	}
	
	/**
	 * Sends a transaction message over to the Authenticator.
	 * The message form is as follows:
	 * 1 Byte -- Version (01 is the only version right now)
	 * 2 Byes -- Number of inputs
	 * <---For Each Input --->
	 * 4 Bytes -- Child key index
	 * 33 Bytes -- Public key the wallet used to create the P2SH address
	 * <---End For Each --->
	 * ? Bytes -- Raw unsigned transaction
	 * 32 Bytes -- HMAC-SHA256 of the above
	 * */
	void sendTX() throws Exception {
		//Create the payload
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		byte[] version = hexStringToByteArray("01");
		outputStream.write(version);
		byte[] tempArr = ByteBuffer.allocate(4).putInt(numInputs).array();
		byte[] numIns = Arrays.copyOfRange(tempArr, 2, 4);
		outputStream.write(numIns);
		for (int a=0; a<numInputs; a++){
			byte[] index = ByteBuffer.allocate(4).putInt(childkeyindex.get(a)).array();
			outputStream.write(index);
			byte[] pubkey = publickeys.get(a);
			outputStream.write(pubkey);
		}
		byte[] transaction = hexStringToByteArray(unsignedTx);
		outputStream.write(transaction);
		byte payload[] = outputStream.toByteArray( );
		//Calculate the HMAC and concatenate it to the payload
		WalletFile file = new WalletFile();
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKey secretkey = new SecretKeySpec(hexStringToByteArray(file.getAESKey()), "AES");
		mac.init(secretkey);
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
			cipher.init(Cipher.ENCRYPT_MODE, secretkey);
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
		
		// Init dispacher
		byte[] cipherKeyBytes;
		Dispacher disp;
		if (PairingProtocol.out == null){
			DataOutputStream out = new DataOutputStream(OpenPort.socket.getOutputStream());
			DataInputStream in = new DataInputStream(OpenPort.socket.getInputStream());
			disp = new Dispacher(out,in);
			/*out.writeInt(cipherBytes.length);
			out.write(cipherBytes);
			System.out.println("Sent transaction");
			int keysize = in.readInt();
		    cipherKeyBytes = new byte[keysize];
		    in.read(cipherKeyBytes);*/
		}
		else {
			disp = new Dispacher(PairingProtocol.out,PairingProtocol.in);
			/*PairingProtocol.out.writeInt(cipherBytes.length);
			PairingProtocol.out.write(cipherBytes);
			System.out.println("Sent transaction");
			int keysize = PairingProtocol.in.readInt();
		    cipherKeyBytes = new byte[keysize];
		    PairingProtocol.in.read(cipherKeyBytes);*/
		}
		
		//Send the encrypted payload over to the Authenticator and wait for the response.
		//disp.write(cipherBytes.length, cipherBytes);
		ArrayList<String> keyandchain = file.getPubAndChain();
		byte[] gcmID = file.getGCMRegID().getBytes();
		Device d = new Device(keyandchain.get(1).getBytes(),
				keyandchain.get(0).getBytes(),
				gcmID,
				hexStringToByteArray("00000001"),
				secretkey);
		disp.dispachMessage(MessageType.signTx, cipherBytes, d);
		System.out.println("Sent transaction");
		int keysize = disp.readInt();
		cipherKeyBytes = new byte[keysize];
		disp.read(cipherKeyBytes);
		
		//Decrypt the response
	    cipher.init(Cipher.DECRYPT_MODE, secretkey);
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
		//Prep the keys needed for signing
		byte[] key = hexStringToByteArray(keyandchain.get(0));
		byte[] chain = hexStringToByteArray(keyandchain.get(1));
		List<TransactionInput> inputs = spendtx.getInputs();
		//Break apart the signature array sent over from the authenticator
		String sigstr = bytesToHex(testsig);
		ArrayList<byte[]> AuthSigs =  new ArrayList<byte[]>();
		int pos = 4;
		for (int b=0; b<numInputs; b++){
			String strlen = sigstr.substring(pos, pos+2);
			int intlen = Integer.parseInt(strlen, 16)*2;
			pos = pos + 2;
			AuthSigs.add(hexStringToByteArray(sigstr.substring(pos, pos+intlen)));
			pos = pos + intlen;
		}
		//Loop to create a signature for each input
		for (int z=0; z<numInputs; z++){
			HDKeyDerivation HDKey = null;
			DeterministicKey mPubKey = HDKey.createMasterPubKeyFromBytes(key, chain);
			DeterministicKey childKey = HDKey.deriveChildKey(mPubKey, childkeyindex.get(z));
			byte[] childpublickey = childKey.getPubKey();
			ECKey authKey = new ECKey(null, childpublickey);
			//Create second signature and build the final transaction
			BigInteger privatekey = new BigInteger(1, hexStringToByteArray(file.getPrivKeyFromIndex(childkeyindex.get(z))));
			byte[] addressPublicKey = ECKey.publicKeyFromPrivate(privatekey, true);
			ECKey walletKey = new ECKey(privatekey, addressPublicKey, true);
			List<ECKey> keys = ImmutableList.of(authKey, walletKey);
			Script scriptpubkey = ScriptBuilder.createMultiSigOutputScript(2,keys);
			byte[] program = scriptpubkey.getProgram();
			TransactionSignature sig1 = TransactionSignature.decodeFromBitcoin(AuthSigs.get(z), true);
			TransactionSignature sig2 = spendtx.calculateSignature(z, walletKey, scriptpubkey, Transaction.SigHash.ALL, false);
			List<TransactionSignature> sigs = ImmutableList.of(sig1, sig2);
			Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(sigs, program);
			TransactionInput input = inputs.get(z);
			input.setScriptSig(inputScript);
		}
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
	
	/**
	 * Derives a child public key from the master public key. Generates a new local key pair.
	 * Uses the two public keys to create a 2of2 multisig address. Saves key and address to json file.
	 */
	String genAddress() throws NoSuchAlgorithmException, JSONException{
		//Derive the child public key from the master public key.
		WalletFile file = new WalletFile();
		ArrayList<String> keyandchain = file.getPubAndChain();
		byte[] key = hexStringToByteArray(keyandchain.get(0));
		byte[] chain = hexStringToByteArray(keyandchain.get(1));
		int index = (int) file.getKeyNum()+1;
		HDKeyDerivation HDKey = null;
  		DeterministicKey mPubKey = HDKey.createMasterPubKeyFromBytes(key, chain);
  		DeterministicKey childKey = HDKey.deriveChildKey(mPubKey, index);
  		byte[] childpublickey = childKey.getPubKey();
  		//Select network parameters
  		NetworkParameters params = null;
        if (testnet==false){
        	params = MainNetParams.get();
        } 
        else {
        	params = TestNet3Params.get();
        }
		ECKey childPubKey = new ECKey(null, childpublickey);
		//Create a new key pair which will kept in the wallet.
		ECKey walletKey = new ECKey();
		byte[] privkey = walletKey.getPrivKeyBytes();
		List<ECKey> keys = ImmutableList.of(childPubKey, walletKey);
		//Create a 2-of-2 multisig output script.
		byte[] scriptpubkey = Script.createMultiSigOutputScript(2,keys);
		Script script = ScriptBuilder.createP2SHOutputScript(Utils.sha256hash160(scriptpubkey));
		//Create the address
		Address multisigaddr = Address.fromP2SHScript(params, script);
		//Save keys to file
		file.writeToFile(bytesToHex(privkey),multisigaddr.toString());
		return multisigaddr.toString();
	}
	
	/**
	 * Gets the unspent outputs JSON for the wallet from blockr.io. Returns enough unspent outputs to 
	 * cover the total transaction output. There is a problem here with the way blockr handles unspent outputs. 
	 * It only shows unspent outputs for confirmed txs. For unconfirmed you can only get all transactions. 
	 * So if all unconfirmed outputs are unspent, it will work correctly, but if you spent an unconfirmed output, 
	 * and try to make another transaction before it confirms, you could get an error. 
	 */
	ArrayList<UnspentOutput> getUnspentOutputs(long outAmount) throws JSONException, IOException{
		numInputs=0;
		childkeyindex = new ArrayList<Integer>();
		ArrayList<UnspentOutput> outList = new ArrayList<UnspentOutput>();
		publickeys = new ArrayList<byte[]>();
		WalletFile file = new WalletFile();
		ArrayList<String> addrs = file.getAddresses();
		long inAmount = 0;
		//First add the confirmed unspent outputs
		for (int i=0; i<addrs.size(); i++){
			if (inAmount < (outAmount + 10000)){
				JSONObject json;
				UnspentOutput out = null;
				if (testnet){json = readJsonFromUrl("http://tbtc.blockr.io/api/v1/address/unspent/" + addrs.get(i));}
				else {json = readJsonFromUrl("http://btc.blockr.io/api/v1/address/unspent/" + addrs.get(i));}
				JSONObject data = json.getJSONObject("data");
				JSONArray unspent = data.getJSONArray("unspent");
				if (unspent.length()!=0){
					for (int x=0; x<unspent.length(); x++){
						if (inAmount < outAmount + 10000){
							JSONObject txinfo = unspent.getJSONObject(x);
							double amount = Double.parseDouble((String) txinfo.get("amount"));
							out = new UnspentOutput(txinfo.get("tx").toString(), txinfo.get("n").toString(), (long)(amount*100000000));
							outList.add(out);
							inAmount = (long) (inAmount + (amount*100000000));
							//Add the public key and index for this address to the respective ArrayLists
							BigInteger privatekey = new BigInteger(1, hexStringToByteArray(file.getPrivKey(addrs.get(i))));
							byte[] publickey = ECKey.publicKeyFromPrivate(privatekey, true);
							publickeys.add(publickey);
							childkeyindex.add((int) file.getAddrIndex(addrs.get(i)));
							numInputs++;
						}
					}
				}
			}
		}		
		//If we still don't have enough outputs move on to adding the unconfirmed
		for (int j=0; j<addrs.size(); j++){
			if (inAmount < (outAmount + 10000)){
				JSONObject json;
				UnspentOutput out = null;
				if (testnet){json = readJsonFromUrl("http://tbtc.blockr.io/api/v1/address/unconfirmed/" + addrs.get(j));}
				else {json = readJsonFromUrl("http://btc.blockr.io/api/v1/address/unconfirmed/" + addrs.get(j));}
				JSONObject data1 = json.getJSONObject("data");
				JSONArray unconfirmed = data1.getJSONArray("unconfirmed");
				if (unconfirmed.length()!=0){
					for (int x=0; x<unconfirmed.length(); x++){
						if (inAmount < outAmount + 10000){
							JSONObject tx = unconfirmed.getJSONObject(x);
							double amount = (double) tx.get("amount");
							out = new UnspentOutput(tx.get("tx").toString(), tx.get("n").toString(), (long)(amount*100000000));
							outList.add(out);
							inAmount = (long) (inAmount + amount*100000000);
							//Add the public key and index for this address to the respective ArrayLists
							BigInteger privatekey = new BigInteger(1, hexStringToByteArray(file.getPrivKey(addrs.get(j))));
							byte[] publickey = ECKey.publicKeyFromPrivate(privatekey, true);
							publickeys.add(publickey);
							numInputs++;
							childkeyindex.add((int) file.getAddrIndex(addrs.get(j)));
						}
					}
				}
			}
		}
		if (inAmount < outAmount + 10000) System.out.println("Insufficient funds");
		System.out.println(numInputs);
		System.out.println(inAmount);
		return outList;
	}
	
	/**Builds a raw unsigned transaction*/
	void mktx(ArrayList<String> MILLI, ArrayList<String> to) throws AddressFormatException, JSONException, IOException, NoSuchAlgorithmException {
		//Gather the data needed to construct the inputs and outputs
		long totalouts=0; 
		for (int i=0; i<MILLI.size(); i++){
			totalouts = totalouts + Long.parseLong(MILLI.get(i));
		}
		ArrayList<UnspentOutput> out = getUnspentOutputs(totalouts); 
		//Set the network parameters
		NetworkParameters params = null;
        if (testnet==false){
        	params = MainNetParams.get();
        } 
        else {
        	params = TestNet3Params.get();
        }
  		spendtx = new Transaction(params);
  		byte[] script = hexStringToByteArray("");
  		//Creates the inputs which reference a previous unspent output
  		long totalins = 0;
  		for (int x=0; x<out.size(); x++){
  			totalins = totalins + out.get(x).getAmount();
  			int index = Integer.parseInt(out.get(x).getIndex());
  	  		Sha256Hash txhash = new Sha256Hash(out.get(x).getTxid());
  			TransactionOutPoint outpoint = new TransactionOutPoint(params, index, txhash);
  			TransactionInput input = new TransactionInput(params, null, script, outpoint);
  			//Add the inputs
  			spendtx.addInput(input);
		}
		//Add the outputs
		for (int i=0; i<MILLI.size(); i++){
			Address outaddr = new Address(params, to.get(i));
			spendtx.addOutput(BigInteger.valueOf(Long.parseLong(MILLI.get(i))), outaddr);
		}
		//Add the change
		if (totalins > (totalouts + 10000)){
			Long changetotal = (totalins - (totalouts+10000));
			String changeaddr = genAddress();
			Address change = new Address(params, changeaddr);
			spendtx.addOutput(BigInteger.valueOf(changetotal), change);
		}
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
	
	/**Returns the balance of the addresses in the wallet using blockr api*/
	public long getBalance(ArrayList<String> addresses) throws JSONException, IOException{
		WalletFile file = new WalletFile();
		JSONObject json;
		JSONArray data;
		double addrbalance;
		long unconfirmedbalance = 0;
		String addr = "";
		if (file.getKeyNum()!=0){
			//Get confirmed Balance
			long balance = 0;
			int num = 0;
			int count = addresses.size();
			for (int a=0; a<(addresses.size()/19)+1; a++){	
				addr = "";
				if (((count/19)+1)>1){
					for (int i=num; i<num+19; i++){
						addr = addr + addresses.get(i) + ",";
					}
				num=num+19;
				count=count-19;
				}
				else {
					for (int i=num; i<num+count; i++){
						addr = addr + addresses.get(i) + ",";
					}
				}
				if (testnet){json = readJsonFromUrl("http://tbtc.blockr.io/api/v1/address/balance/" + addr);}
				else {json = readJsonFromUrl("http://btc.blockr.io/api/v1/address/balance/" + addr);}
				data = json.getJSONArray("data");
				addrbalance=0;
				for (int i=0; i<data.length(); i++){
					JSONObject info = data.getJSONObject(i);
					addrbalance = (double) info.getDouble("balance");
					balance = (long) (balance + (addrbalance)*100000000);
				}
			}
			return balance;
			}
		else {
			return 0;
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
        return hexEncode(pubKey.getPubKey());
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
    	long amount;
    	
    	public UnspentOutput(String id, String in, long amt){
    		this.txid = id;
    		this.index = in;
    		this.amount = amt;
    	}
    	
    	public String getTxid(){
    		return txid;
    	}
    	
    	public String getIndex(){
    		return index;
    	}
    	
    	public long getAmount(){
    		return amount;
    	}
    }
    
}


