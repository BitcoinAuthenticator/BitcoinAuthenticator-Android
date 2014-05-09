package org.bitcoin.authenticator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;
import com.google.bitcoin.crypto.TransactionSignature;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;
import com.google.common.collect.ImmutableList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class creates the Wallet_list activity which serves as the main activity after the user pairs a wallet.
 * It loads the wallet metadata from shared preferences and creates a listview of paired wallets.
 * In the background it opens a connection to the wallet and waits for transactions. When a transaction is received
 * it displays a dialog box to the user asking for authorization. If it's authorized, it calculates the signature
 * and sends it back to the wallet.
 */
public class Wallet_list extends Activity {
	public static String PublicIP;
	public static String LocalIP;
	public static String IPAddress;
	Connection conn = null;
 
	/**Creates the listview component and defines behavior to a long press on a list item.*/
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        //Get the wallet metadata and add it to a listview.
        ArrayList walletList = getListData();
        final ListView lv1 = (ListView) findViewById(R.id.custom_list);
        lv1.setAdapter(new CustomListAdapter(this, walletList));
        //Click listener for list items.
        lv1.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                WalletItem newsData = (WalletItem) o;
                Toast.makeText(Wallet_list.this, "Selected :" + " " + newsData, Toast.LENGTH_LONG).show();
            }
        });
        //Load the IPs for each wallet from shared preferences.
        SharedPreferences prefs = getSharedPreferences("WalletData1", 0);	
        IPAddress = prefs.getString("ExternalIP", "null");
        LocalIP = prefs.getString("LocalIP","null");
        //Launch task to get public IP to determine if the device is on the same network as the wallet.
        new getIPtask().execute("");
    }
	
	/**Inflates the menu and adds it to the action bar*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**This method handles the clicks in the option menu*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_pair_wallet){
			startActivity (new Intent(Wallet_list.this, Pair_wallet.class));;
		}
		if (id == R.id.action_how_it_works){
			startActivity (new Intent(Wallet_list.this, How_it_works.class));;
		}
		if (id == R.id.action_show_seed){
			startActivity (new Intent(Wallet_list.this, Show_seed.class));
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Disable the back button because the device is now paired and any activity the user needs can be 
	 * accessed from the menu. 
	 */
	@Override
	public void onBackPressed() {
	}
	
	/**
	 * Creates a dialog box to show to the user when the Authenticator receives a transaction from the wallet.
	 * Loads the seed from internal storage to derive the private key needed for signing.
	 * Asks user for authorization. If yes, it signs the transaction and sends it back to the wallet.
	 */
	public void showDialogBox(final TxData tx) throws InterruptedException{
		if (tx.equals("error")){
			Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
		}
		else {
			//Load walletID from Shared Preferences
			SharedPreferences data = getSharedPreferences("WalletData1", 0);
			String name = data.getString("ID", "null");
			//Load AES Key from file
        	byte [] key = null;
    		String FILENAME = "AESKey1";
    		File file = new File(getFilesDir(), FILENAME);
    		int size = (int)file.length();
    		if (size != 0)
    		{
    			FileInputStream inputStream = null;
    			try {
    				inputStream = openFileInput(FILENAME);
    			} catch (FileNotFoundException e1) {
    				e1.printStackTrace();
    			}
    			key = new byte[size];
    			try {
    				inputStream.read(key, 0, size);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			try {
    				inputStream.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		final byte[] AESKey = key;
    		final SecretKey sharedsecret = new SecretKeySpec(AESKey, "AES");
			//Load the seed from internal storage
			byte [] seed = null;
    		String FILENAME2 = "seed";
    		File file2 = new File(getFilesDir(), FILENAME2);
    		int size2 = (int)file2.length();
    		if (size2 != 0)
    		{
    			FileInputStream inputStream = null;
    			try {
    				inputStream = openFileInput(FILENAME2);
    			} catch (FileNotFoundException e1) {
    				e1.printStackTrace();
    			}
    			seed = new byte[size2];
    			try {
    				inputStream.read(seed, 0, size2);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			try {
    				inputStream.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		final byte[] authseed = seed;
			//Parse through the transaction message and rebuild the transaction.
			NetworkParameters params = MainNetParams.get();
			byte[] transaction = tx.getTransaction();
			final Transaction unsignedTx = new Transaction(params, transaction);
			//Get the output address and the amount from the transaction so we can display it to the user.
			List<TransactionOutput> outputs = unsignedTx.getOutputs();
			String display = "";
			for (int i = 0; i<outputs.size(); i++){
				TransactionOutput multisigOutput = unsignedTx.getOutput(i);
				String strOutput = multisigOutput.toString();
				String addr = strOutput.substring(strOutput.indexOf("to ")+3, strOutput.indexOf(" script"));
				String amount = strOutput.substring(9, strOutput.indexOf(" to"));
				display = display + addr + " <font color='#009933'>" + amount + " BTC</font>";
				if (i<outputs.size()-1){display = display + "<br>";}
			}
		//Create the dialog box
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
			//Set title
			alertDialogBuilder.setTitle("Authorize Transaction");
			//Set dialog message
			alertDialogBuilder
				.setMessage(Html.fromHtml("Bitcoin Authenticator has received a transaction<br><br>From: <br>" + name + "<br><br>To:<br>" + display))
				.setCancelable(false)
				.setPositiveButton("Authorize",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						//Close the dialog box first since the signature operations will create a little lag
						//and we can do them in the background.
						dialog.cancel();
						//Prep the byte array we will fill with the signatures.
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						byte[] tempArr = ByteBuffer.allocate(4).putInt(tx.numInputs).array();
						byte[] numSigs = Arrays.copyOfRange(tempArr, 2, 4);
						try {
							outputStream.write(numSigs);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						//Loop creating a signature for each input
						for (int j=0; j<tx.numInputs; j++){
							//Derive the private key needed to sign the transaction
							ArrayList<Integer> index = tx.getIndexes();
							ArrayList<byte[]> walpubkeys = tx.getPublicKeys();
							HDKeyDerivation HDKey = null;
							DeterministicKey masterKey = HDKey.createMasterPrivateKey(authseed);
							DeterministicKey walletMasterKey = HDKey.deriveChildKey(masterKey,1);
							DeterministicKey childKey = HDKey.deriveChildKey(walletMasterKey,index.get(j));
							byte[] privKey = childKey.getPrivKeyBytes();
							byte[] pubKey = childKey.getPubKey();
							ECKey authenticatorKey = new ECKey(privKey, pubKey);
							ECKey walletPubKey = new ECKey(null, walpubkeys.get(j)); 
							
							//Print keys used to create signature
							System.out.println("Index: " + index.get(j));
							System.out.println("wallet pubkey: " + Utils.bytesToHex(walletPubKey.getPubKey()));
							System.out.println("Auth pubkey: " + Utils.bytesToHex(authenticatorKey.getPubKey()));
							System.out.println("Auth privkey: " + Utils.bytesToHex(authenticatorKey.getPrivKeyBytes()));
							
							List<ECKey> keys = ImmutableList.of(authenticatorKey, walletPubKey);
							//Create the multisig script we will be using for signing. 
							Script scriptpubkey = ScriptBuilder.createMultiSigOutputScript(2,keys);
							//Create the signature.
							TransactionSignature sig2 = unsignedTx.calculateSignature(0, authenticatorKey, scriptpubkey, Transaction.SigHash.ALL, false);
							byte[] signature = sig2.encodeToBitcoin();
							byte[] tempArr2 = ByteBuffer.allocate(4).putInt(signature.length).array();
							byte[] sigLen = Arrays.copyOfRange(tempArr2, 3, 4);
							System.out.println(signature.length);
							try {
								outputStream.write(sigLen);
								outputStream.write(signature);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						byte[] sigArray = outputStream.toByteArray();
						System.out.println(Utils.bytesToHex(sigArray));
						//Select which connection to use
						Message msg = null;
			        	if (PairingProtocol.conn==null){
			        		try {
								msg = new Message(conn);
							} catch (IOException e) {
								e.printStackTrace();
							}
			        	}
			        	else {
			        		try {
								msg = new Message(PairingProtocol.conn);
							} catch (IOException e) {
								e.printStackTrace();
							}
			        	}
			        	//Send the signature
							try {
								msg.sendSig(sigArray, sharedsecret);
							} catch (InvalidKeyException e) {
								e.printStackTrace();
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						//Reload the ConnectionToWallets task to set up to receive another transaction.
						new ConnectToWallets().execute("");
					}
				  })
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						//TODO if the user doesn't approve the transaction, send a message decline message back to the wallet.
						dialog.cancel();
					}
				});
				// create and show the alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();}
	}
	
	/**This method loads the metadata from Shared Preferences needed to display in the listview*/
    private ArrayList getListData() {
    	//Open shared preferences and get the number of wallets
    	SharedPreferences settings = getSharedPreferences("ConfigFile", 0);	
	    int num = settings.getInt("numwallets", 0);
    	ArrayList results = new ArrayList();
    	//Load the data for each wallet and add it to a WalletItem object
    	for (int i=1; i<num+1; i+=1){
    	String wdata = "WalletData" + i;
    	SharedPreferences data = getSharedPreferences(wdata, 0);	
        WalletItem walletData = new WalletItem();
        String wID = ("ID");
        String wFP = ("Fingerprint");
        String wTP = ("Type");
        walletData.setWalletLabel(data.getString(wID, "null"));
        walletData.setFingerprint(data.getString(wFP, "null"));
        //Decide which icon to display
        if (data.getString(wTP, "null").equals("blockchain")){walletData.setIcon(R.drawable.blockchain_info_logo);}
        else if (data.getString(wTP, "null").equals("electrum")){walletData.setIcon(R.drawable.electrum_logo);}
        else if (data.getString(wTP, "null").equals("hive")){walletData.setIcon(R.drawable.hive_logo);}
        else if (data.getString(wTP, "null").equals("multibit")){walletData.setIcon(R.drawable.multibit_logo);}
        else if (data.getString(wTP, "null").equals("bitcoincore")){walletData.setIcon(R.drawable.bitcoin_core_logo);}
        else if (data.getString(wTP, "null").equals("armory")){walletData.setIcon(R.drawable.armory_logo);}
        else if (data.getString(wTP, "null").equals("darkwallet")){walletData.setIcon(R.drawable.darkwallet_logo);}
        else {walletData.setIcon(R.drawable.authenticator_logo);}
        results.add(walletData);}
 
        return results;
    }
    
    /**Creates an object that holds the metadata for each wallet to include in the listview*/
    public class WalletItem {
    	 
        private String walletLabel;
        private String fingerprint;
        private int icon;
     
        public String getWalletLabel() {
            return walletLabel;
        }
     
        public void setWalletLabel(String label) {
            this.walletLabel = label;
        }
     
        public String getFingerprint() {
            return fingerprint;
        }
     
        public void setFingerprint(String fp) {
            this.fingerprint = fp;
        }
     
        public int getIcon() {
            return icon;
        }
     
        public void setIcon(int blockchainInfoLogo) {
            this.icon = blockchainInfoLogo;
        }
     
        @Override
        public String toString() {
            return "[ Wallet Label=" + walletLabel + ", Fingerprint=" + 
                    fingerprint + " , Icon=" + icon + "]";
        }
    }

    /**Creates a custom adapter object for the listview*/
    public class CustomListAdapter extends BaseAdapter {
    	
    	private ArrayList listData;
    	private LayoutInflater layoutInflater;
 
    	public CustomListAdapter(Context context, ArrayList listData) {
    		this.listData = listData;
    		layoutInflater = LayoutInflater.from(context);
    	}
 
    	@Override
    	public int getCount() {
    		return listData.size();
    	}
 
    	@Override
    	public Object getItem(int position) {
    		return listData.get(position);
    	}
    	
    	@Override
    	public long getItemId(int position) {
    		return position;
    	}
 
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;
    		if (convertView == null) {
    			convertView = layoutInflater.inflate(R.layout.list_item, null);
    			holder = new ViewHolder();
    			holder.walletLabelView = (TextView) convertView.findViewById(R.id.wallet_label);
    			holder.walletFingerprintView = (TextView) convertView.findViewById(R.id.wallet_fingerprint);
    			holder.walletIcon = (ImageView) convertView.findViewById(R.id.wallet_icon);
    			convertView.setTag(holder);
    		} else {
    			holder = (ViewHolder) convertView.getTag();
    		}
    		holder.walletLabelView.setText(((WalletItem) listData.get(position)).getWalletLabel());
    		holder.walletFingerprintView.setText(((WalletItem) listData.get(position)).getFingerprint());
    		holder.walletIcon.setImageResource(((WalletItem) listData.get(position)).getIcon());
 
    		return convertView;
    	}
 
    	class ViewHolder {
    		TextView walletLabelView;
    		TextView walletFingerprintView;
    		ImageView walletIcon;
    	}
    }
    
    /**
     * This is a class that runs in the background and connects to the wallet and waits to receive a transaction.
     * When one is received, it loads the dialog box.
     */
    public class ConnectToWallets extends AsyncTask<String,String,Connection> {
    	TxData tx;
        @Override
        protected Connection doInBackground(String... message) {
        	//Load AES Key from file
        	byte [] key = null;
    		String FILENAME = "AESKey1";
    		File file = new File(getFilesDir(), FILENAME);
    		int size = (int)file.length();
    		if (size != 0)
    		{
    			FileInputStream inputStream = null;
    			try {
    				inputStream = openFileInput(FILENAME);
    			} catch (FileNotFoundException e1) {
    				e1.printStackTrace();
    			}
    			key = new byte[size];
    			try {
    				inputStream.read(key, 0, size);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			try {
    				inputStream.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		final byte[] AESKey = key;
    		SecretKey sharedsecret = new SecretKeySpec(AESKey, "AES");
        	//Decide which IP to use for the connection
        	String IP = null;
        	if (IPAddress.equals(PublicIP)){IP = LocalIP;}
        	else {IP = IPAddress;}
        	//If wallet was recently paired we can use the connection from PairingProtocol.
        	//Otherwise we will create a new connection.
        	if (PairingProtocol.conn==null){
        		try {
					conn = new Connection(IP);
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
				}
        	}
        	else {
        		conn = PairingProtocol.conn;
        	}
        	//Create a new message object for receiving the transaction.
        	Message msg = null;
    		try {
				msg = new Message(conn);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		try {
				tx = msg.receiveTX(sharedsecret);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
        }
 	
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        
        /**On finish show the transaction in a dialog box*/
        protected void onPostExecute(Connection result) {
           super.onPostExecute(result);
           if (tx != null)
			try {
				showDialogBox(tx);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
 
    /**
     * Class to get the public IP of the device so the Authenticator can decide if the device is on
     * the same WiFi network. 
     */
    public class getIPtask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... message) {
        	String ip = Utils.getPublicIP();
            return ip;
        }
 
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        
        /**After we receive the IP connect to the wallet*/
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            PublicIP = result;
            new ConnectToWallets().execute("");
        }    
    }

}