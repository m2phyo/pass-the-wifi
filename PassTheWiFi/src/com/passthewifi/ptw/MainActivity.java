package com.passthewifi.ptw;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.passthewifi.ptw.MainActivity;
import com.passthewifi.ptw.R;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	static final String TAG = "PassTheWiFi";
	ListView apList;
	ArrayList<APDetails> details;
	HashSet<String> ssidNames = new HashSet<String>();
	AdapterView.AdapterContextMenuInfo info;
	WifiManager wifi;
	BroadcastReceiver wifiReceiver;
	boolean scanFlag = true;
	List<ScanResult> results;
	int scanSize = 0;
	String chosenSSID;
	String chosenPass;
	String chosenSec;
	
	private NfcAdapter mAdapter;
	private boolean mInWriteMode;
	private AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			
		// grab our NFC Adapter
        mAdapter = NfcAdapter.getDefaultAdapter(this);
		
		// Enable WiFi if disabled
		// Start a scan
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled() == false) {
			Toast.makeText(getApplicationContext(),
					"WiFi is disabled. Enabling it..", Toast.LENGTH_LONG)
					.show();
			wifi.setWifiEnabled(true);
		}
		wifi.startScan();

		
		/*// Register a receiver for when scan result is ready
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				if(scanFlag) {
					manualScanResults();
					 
					 * If another scan occurs, ignore.
					 * Unregistering receiver might be a better option.
					 * Decide later.
					 
					scanFlag = false;
				}
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));*/
	
		wifiReceiver = new BroadcastReceiver() {
        	@Override
			public void onReceive(Context c, Intent intent) {
					//
        			Log.d(TAG, "Inside onReceive!");
        			manualScanResults();
					c.unregisterReceiver(wifiReceiver);
        	}
        };
        
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

	}

	// TO-DO Manual scan option will call this
	private void manualScanResults() {
		findViewById(R.id.marker_progress).setVisibility(View.GONE);
		results = wifi.getScanResults();
		scanSize = results.size();
		
		String prot; // Protection type
		apList = (ListView) findViewById(R.id.APList);
		details = new ArrayList<APDetails>();
		APDetails Detail;

		for (int i=0; i<scanSize; i++) {
			Detail = new APDetails();
			prot = getScanResultSecurity(results.get(i));
			if(!prot.equalsIgnoreCase("OPEN")){
				Detail.setIcon(R.drawable.lock);
				Detail.setProtected();
			}
			else {
				// don't have an icon for public yet
				// set public WiFi icon
			}	
			Detail.setName(results.get(i).SSID);
			Detail.setProtType(getScanResultSecurity(results.get(i)));
			if(!ssidNames.contains(Detail.getName()))
			{
				details.add(Detail);
				ssidNames.add(Detail.getName());
			}
				
		}
		
		apList.setAdapter(new CustomAdapter(details, this));

		apList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, final int position,
					long id) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			    builder.setTitle(details.get(position).getName());
			    chosenSSID = details.get(position).getName();
				chosenPass = details.get(position).getPasskey();
				chosenSec = details.get(position).getProtType();
			  
			    builder.setItems(R.array.share_array, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			               // The 'which' argument contains the index position
			               // of the selected item
			            	   if(which == 0){
			            		   // NFC Tag
			            		   enableWriteMode();
			            		   alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			            		   alertDialog.setTitle("Tap the tag!");
			            		   alertDialog.setMessage("Hold tag against phone to write.");
			            		   alertDialog.setOnDismissListener(new OnDismissListener() {

									@Override
									public void onDismiss(DialogInterface dialog) {
										disableWriteMode();		
									}
			            		   });
			            		   alertDialog.show(); 
			            	   }
			            	   else if(which == 1){
			            		   // Android Beam
			            		   Intent myIntent = new Intent(MainActivity.this, BeamActivity.class);
			            		   myIntent.putExtra("ssid",chosenSSID); 
			            		   myIntent.putExtra("pass",chosenPass);
			            		   MainActivity.this.startActivity(myIntent);
			            	   }
			            	   else if(which == 2){
			            		   // QR
			            		   Intent myIntent = new Intent(MainActivity.this, QRActivity.class);
			            		   myIntent.putExtra("ssid",chosenSSID); 
			            		   myIntent.putExtra("pass",chosenPass);
			            		   myIntent.putExtra("security",chosenSec);
			            		   MainActivity.this.startActivity(myIntent);
			            	   }
			           }
			    });
			    builder.create().show();
			}
		});
	}
	
	private String getScanResultSecurity(ScanResult scanResult) {
	    Log.i(TAG, "* getScanResultSecurity");

	    final String cap = scanResult.capabilities;
	    final String[] securityModes = { "WEP", "PSK", "EAP" };

	    for (int i = securityModes.length - 1; i >= 0; i--) {
	        if (cap.contains(securityModes[i])) {
	            return securityModes[i];
	        }
	    }
	    return "OPEN";
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		disableWriteMode();
	}
	
	/**
	 * Called when our blank tag is scanned executing the PendingIntent
	 */
	@Override
    public void onNewIntent(Intent intent) {
		if(mInWriteMode) {
			mInWriteMode = false;
			
			alertDialog.dismiss();
			// write to newly scanned tag
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			writeTag(tag,chosenSSID,chosenPass);
		}
    }
	
	/**
	 * Force this Activity to get NFC events first
	 */
	private void enableWriteMode() {
		mInWriteMode = true;
		
		// set up a PendingIntent to open the app when a tag is scanned
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[] { tagDetected };
        
		mAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
	}
	
	private void disableWriteMode() {
		mAdapter.disableForegroundDispatch(this);
	}
	
	/**
	 * Format a tag and write our NDEF message
	 */
	private boolean writeTag(Tag tag, String SSID, String pass) {
		// record to launch Play Store if app is not installed
		NdefRecord appRecord = NdefRecord.createApplicationRecord("com.passthewifi.ptw");
		
		// record that contains our custom data, using custom MIME_TYPE
		byte[] payload_ssid = SSID.getBytes();
		byte[] payload_pass = pass.getBytes();
		
		String mime = getResources().getString(R.string.PTW_MIME);
		byte[] mimeBytes = mime.getBytes(Charset.forName("US-ASCII"));
        NdefRecord ssidRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, 
        										new byte[0], payload_ssid);
        NdefRecord passRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, 
        										new byte[0], payload_pass);
        
		NdefMessage message = new NdefMessage(new NdefRecord[] { ssidRecord, passRecord, appRecord});
        
		try {
			// check if tag is already NDEF formatted
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					Toast.makeText(getApplicationContext(),"Read-only tag.", Toast.LENGTH_SHORT).show();
					return false;
				}
				
				// calculate how much space needed for the data
				int size = message.toByteArray().length;
				if (ndef.getMaxSize() < size) {
					Toast.makeText(getApplicationContext(),"Tag doesn't have enough free space.", Toast.LENGTH_SHORT).show();
					return false;
				}

				ndef.writeNdefMessage(message);
				Toast.makeText(getApplicationContext(),"Tag written successfully.", Toast.LENGTH_SHORT).show();
				return true;
			} else {
				// attempt to format tag
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						Toast.makeText(getApplicationContext(),"Tag written successfully.", Toast.LENGTH_SHORT).show();
						return true;
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(),"Unable to format tag to NDEF.", Toast.LENGTH_SHORT).show();
						return false;
					}
				} else {
					Toast.makeText(getApplicationContext(),"Tag doesn't appear to support NDEF format.", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),"Failed to write tag", Toast.LENGTH_SHORT).show();
		}

        return false;
    }
	
	public class CustomAdapter extends BaseAdapter {

		private ArrayList<APDetails> _data;
		Context _c;

		CustomAdapter(ArrayList<APDetails> data, Context c) {
			_data = data;
			_c = c;
		}

		public int getCount() {
			return _data.size();
		}

		public Object getItem(int position) {
			return _data.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) _c
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item_ap, null);
			}

			// Show icon and SSID
			ImageView image = (ImageView) v.findViewById(R.id.icon);
			TextView nameView = (TextView) v.findViewById(R.id.name);

			APDetails ap = _data.get(position);
			image.setImageResource(ap.icon);
			nameView.setText(ap.name);

			image.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					
					// Check if password necessary
					if(details.get(position).isProtected()){
						AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
	
						alert.setTitle("Change Password");
						alert.setMessage("Enter password for "+details.get(position).getName());
	
						// Set an EditText view to get password 
						// May make this a password field later. (I prefer regular text view :))
						
						final EditText input = new EditText(MainActivity.this);
						alert.setView(input);
	
						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						  String value = input.getText().toString();
						  details.get(position).setPasskey(value);
						  }
						});
	
						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						  public void onClick(DialogInterface dialog, int whichButton) {
						    // Canceled.
						  }
						});
	
						alert.show();
					}
					else {
						Toast.makeText(getApplicationContext(),"No password needed", Toast.LENGTH_SHORT).show();
					}
				}    						
			});

			return v;
		}
	}

	public class APDetails {
		int icon;
		String name;
		// implement a secure way to store passwords in a database/file
		String pass = "";
		String protType;
		boolean isProtected = false;

		public boolean isProtected() {
			return isProtected;
		}

		public void setProtected() {
			isProtected = true;
		}
		public void setProtType(String p) {
			protType = p;
		}
		
		public String getProtType() {
			return protType;
		}

		public String getName() {
			return name;
		}

		public void setName(String from) {
			this.name = from;
		}
		
		public void setPasskey(String p) {
			pass = p;
			setIcon(R.drawable.unlock);
		}
		
		public String getPasskey() {
			return pass;
		}

		public int getIcon() {
			return icon;
		}

		public void setIcon(int icon) {
			this.icon = icon;
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
