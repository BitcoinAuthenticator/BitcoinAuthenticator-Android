package org.bitcoin.authenticator.backup;

import org.bitcoin.authenticator.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;
import com.google.bitcoin.crypto.MnemonicCode;
import com.google.bitcoin.crypto.MnemonicException.MnemonicChecksumException;
import com.google.bitcoin.crypto.MnemonicException.MnemonicLengthException;
import com.google.bitcoin.crypto.MnemonicException.MnemonicWordException;
import com.google.bitcoin.wallet.DeterministicSeed;
import com.google.common.base.Joiner;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import static com.google.bitcoin.core.Utils.HEX;

public class PaperWalletQR{
	private Context context;
	
	public PaperWalletQR(Context c){
		context = c;
	}
	
	public Bitmap generatePaperWallet(String mnemonic, DeterministicSeed seed, long creationTime) throws IOException{
		Bitmap qrSeed = createQRSeedImage(seed, creationTime);
		Bitmap qrMPubKey = createMnemonicStringImage(mnemonic, seed);
		return completePaperWallet(mnemonic, qrSeed, qrMPubKey);
	}
	
	@SuppressWarnings("restriction")
	public Bitmap createQRSeedImage(DeterministicSeed seed, long creationTime){
		byte[] imageBytes = null;
//		imageBytes = QRCode
//				        .from(generateQRSeedDataString(seed, creationTime))
//				        .withSize(170, 170)
//				        .to(ImageType.PNG)
//				        .stream()
//				        .toByteArray();
//        return BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
		return this.generateQR(generateQRSeedDataString(seed, creationTime), 170, 170);
	}
	
	private String generateQRSeedDataString(DeterministicSeed seed, long creationTime)
	{
		String qrCodeData = null;
		MnemonicCode ms = null;
		try {
 			ms = new MnemonicCode();
 			List<String> mnemonic = seed.getMnemonicCode();
 			byte[] entropy = ms.toEntropy(mnemonic);
 			String entropyHex = HEX.encode(entropy);
 			qrCodeData = "Seed=" + entropyHex + 
		  			"&Time=" + creationTime;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}

		
		try {
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return qrCodeData;
	}
	
	public SeedQRData parseSeedQR(String data){
		String seedStr = data.substring(data.indexOf("Seed=") + 5, data.indexOf("&Time="));
		String creationTimeStr = data.substring(data.indexOf("&Time=") + 6, data.length());
		return new SeedQRData(seedStr, creationTimeStr);
	}
	
	public class SeedQRData{
		public DeterministicSeed seed;
		public long creationTime;
		
		public SeedQRData(String entropyHex, String creationTimeStr){
			creationTime =  (long)Double.parseDouble(creationTimeStr);
			
			// get mnemonic seed
			MnemonicCode ms = null;
			try {
	 			ms = new MnemonicCode();
	 			byte[] entropy = HEX.decode(entropyHex);
	 			List<String> mnemonic = ms.toMnemonic(entropy);
	 			seed = new DeterministicSeed(mnemonic, "", creationTime);
	 			String mnemonicStr = Joiner.on(" ").join(seed.getMnemonicCode());
	 		} catch (Exception e) {
	 			e.printStackTrace();
	 		}
			
		}
	}
	
	private Bitmap createMnemonicStringImage(String mnemonic, DeterministicSeed seed){
		byte[] imageBytes = null;
		DeterministicKey mprivkey = HDKeyDerivation.createMasterPrivateKey(seed.getSecretBytes());
        DeterministicKey mpubkey = mprivkey.getPubOnly();
//        imageBytes = QRCode
//			        .from(mpubkey.toString())
//			        .withSize(160, 160)
//			        .to(ImageType.PNG)
//			        .stream()
//			        .toByteArray();
//        return BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
        return this.generateQR(mpubkey.toString(), 160, 160);
	}
	
	private Bitmap completePaperWallet(String mnemonic, Bitmap qrSeed, Bitmap qrMPubKey) throws IOException{
		Bitmap paperwallet = BitmapFactory.decodeResource(context.getResources(), R.drawable.paperwallet);
		Bitmap mutableBitmap = paperwallet.copy(Bitmap.Config.ARGB_8888, true);
		
		
		Canvas canvas = new Canvas(mutableBitmap);
		canvas.drawBitmap(qrSeed, 625, 185, null);
		canvas.drawBitmap(qrMPubKey, 37, 86, null);
		
		// Draw the string
		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setColor(context.getResources().getColor(android.R.color.black));
		paint.setTextSize(16);
		canvas.drawText(mnemonic,92, 420, paint);

		return mutableBitmap;
	}
	
	private Bitmap generateQR(String content, int w, int h){
		QRCodeWriter writer = new QRCodeWriter();
	    try {
	        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, w, h);
	        int width = bitMatrix.getWidth();
	        int height = bitMatrix.getHeight();
	        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	        for (int x = 0; x < width; x++) {
	            for (int y = 0; y < height; y++) {
	                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
	            }
	        }
	        
	        return bmp;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return null;
	}
}
