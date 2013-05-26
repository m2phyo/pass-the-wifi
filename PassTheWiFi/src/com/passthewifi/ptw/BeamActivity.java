package com.passthewifi.ptw;

import java.nio.charset.Charset;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

public class BeamActivity extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	NfcAdapter mNfcAdapter;
	TextView mInfoText;
	String ssid;
	String pass;
	private static final int MESSAGE_SENT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beam_activity);

		Intent intent = getIntent();
		ssid = intent.getStringExtra("ssid");
		pass = intent.getStringExtra("pass");

		mInfoText = (TextView) findViewById(R.id.textView);
		mInfoText.setText("Hold the phone against another\n NFC-enabled smartphone!");
		mInfoText.setTextSize(18);
	
		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			mInfoText = (TextView) findViewById(R.id.textView);
			mInfoText.setText("NFC is not available on this device.");
		} else {
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
	}

	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {

		// record to launch Play Store if app is not installed
		NdefRecord appRecord = NdefRecord.createApplicationRecord("com.passthewifi.ptw");

		// record that contains our custom data, using custom MIME_TYPE
		byte[] payload_ssid = ssid.getBytes();
		byte[] payload_pass = pass.getBytes();

		String mime = getResources().getString(R.string.PTW_MIME);
		byte[] mimeBytes = mime.getBytes(Charset.forName("US-ASCII"));
		NdefRecord ssidRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload_ssid);
		NdefRecord passRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload_pass);

		NdefMessage message = new NdefMessage(new NdefRecord[] { ssidRecord, passRecord, appRecord });
		return message;
	}

	/**
	 * Implementation for the OnNdefPushCompleteCallback interface
	 */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	/** This handler receives a message from onNdefPushComplete */
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "WiFi details sent!",
						Toast.LENGTH_LONG).show();
				finish();
				break;
			}
			
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0,1 contain the MIME type, record 2 is the AAR, if present
		String ssid = new String(msg.getRecords()[0].getPayload());
		String pass = new String(msg.getRecords()[1].getPayload());
		mInfoText.setText("SSID: " + ssid + " Pass: " + pass);

	}
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  // ignore orientation/keyboard change
	  super.onConfigurationChanged(newConfig);
	}
	*/

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // If NFC is
	 * not available, we won't be needing this menu if (mNfcAdapter == null) {
	 * return super.onCreateOptionsMenu(menu); } MenuInflater inflater =
	 * getMenuInflater(); inflater.inflate(R.menu.options, menu); return true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case R.id.menu_settings: Intent intent = new
	 * Intent(Settings.ACTION_NFCSHARING_SETTINGS); startActivity(intent);
	 * return true; default: return super.onOptionsItemSelected(item); } }
	 */
}
