package com.passthewifi.ptw;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class QRActivity extends Activity {

	static final String TAG = "PassTheWiFi";
	String ssid;
	String pass;
	String security;
	String qr_sec;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qr_activity);
		
		Intent intent = getIntent();
		ssid = intent.getStringExtra("ssid");
		pass = intent.getStringExtra("pass");
		security = intent.getStringExtra("security");

	    if (security.equals("WEP"))
	    	qr_sec = "WEP";
	    else if (security.equals("PSK") || security.equals("EAP"))
	    	qr_sec = "WPA";
	    else
	    	qr_sec = "nopass";
		
		/*
		 * https://code.google.com/p/zxing/wiki/BarcodeContents
		 * Order of fields does not matter. Special characters "\", ";", "," and ":" 
		 * should be escaped with a backslash ("\") as in MECARD encoding. 
		 * For example, if an SSID was literally "foo;bar\baz" 
		 * (with double quotes part of the SSID name itself) 
		 * then it would be encoded like: WIFI:S:\"foo\;bar\\baz\";;
		 */
	    
	    /*
	     * TO-DO: Fix the above issue later.
	     */
		String qr_text = "WIFI:T:"+qr_sec+";S:"+ssid+";P:"+pass+";;";

		// Find screen size
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		int width = point.x;
		int height = point.y;
		int smallerDimension = width < height ? width : height;
		smallerDimension = smallerDimension * 3 / 4;

		// Encode with a QR Code image
		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qr_text, null,
				Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
				smallerDimension);
		try {
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
			ImageView myImage = (ImageView) findViewById(R.id.imageView);
			myImage.setImageBitmap(bitmap);

		} catch (WriterException e) {
			e.printStackTrace();
		}

	}
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  // ignore orientation/keyboard change
	  super.onConfigurationChanged(newConfig);
	}
	*/
}