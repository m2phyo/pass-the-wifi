package com.passthewifi.ptw;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class WifiActivity extends Activity {
	static final String TAG = "PassTheWiFi";
	WifiManager wifi;
	boolean scanFlag = true;
	int notFound = 1;
	BroadcastReceiver wifiReceiver;
	
	String ssid;
	String pass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.wifi_activity);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "Start of onResume!");
		Intent intent = getIntent();
        if(intent.getType() != null && intent.getType().equals(getResources().getString(R.string.PTW_MIME))) {
        	wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    		if (wifi.isWifiEnabled() == false) {
    			Toast.makeText(getApplicationContext(),
    					"WiFi is disabled. Enabling it..", Toast.LENGTH_LONG)
    					.show();
    			wifi.setWifiEnabled(true);
    		}
    		wifi.startScan();
    		
        	Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            NdefRecord ssidRecord = msg.getRecords()[0];
            NdefRecord passRecord = msg.getRecords()[1];
            
            ssid = new String(ssidRecord.getPayload());
            pass = new String(passRecord.getPayload());
            
            wifiReceiver = new BroadcastReceiver() {
            	@Override
				public void onReceive(Context c, Intent intent) {
						
            			Log.d(TAG, "Inside onReceive!");
						connectToWifi(ssid, pass);
						c.unregisterReceiver(wifiReceiver);
            	}
            };
            
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
	}
	
	private void connectToWifi(String ss, String passkey) {
		Log.i(TAG, "* connectToAP");

	    WifiConfiguration wifiConfiguration = new WifiConfiguration();

	    String networkSSID = ss;
	    String networkPass = passkey;
	    int res;

		if (wifi.isWifiEnabled() == false) {
			Toast.makeText(getApplicationContext(),
					"WiFi is disabled. Enabling it..", Toast.LENGTH_LONG)
					.show();
			wifi.setWifiEnabled(true);
		}
		
	    List<ScanResult> scanResultList = wifi.getScanResults();
	    //Log.d(TAG, "# password " + networkPass);

	    for (ScanResult result : scanResultList) {
	        if (result.SSID.equals(networkSSID)) {
	        	notFound = 0;
	            String securityMode = getScanResultSecurity(result);
	            
				if (securityMode.equalsIgnoreCase("OPEN")) {
					
	                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
	                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	                res = wifi.addNetwork(wifiConfiguration);

	                wifi.enableNetwork(res, true);

	                wifi.setWifiEnabled(true);
	                
	            } 
				
				else if (securityMode.equalsIgnoreCase("WEP")) {
	                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
	                wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
	                wifiConfiguration.wepTxKeyIndex = 0;
	                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	                res = wifi.addNetwork(wifiConfiguration);

	                wifi.enableNetwork(res, true);

	                wifi.setWifiEnabled(true);
	            }
				
				else {
		            wifiConfiguration.SSID = "\"" + networkSSID + "\"";
		            wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
		            wifiConfiguration.hiddenSSID = true;
		            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
		            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	
		            res = wifi.addNetwork(wifiConfiguration);
	
		            wifi.enableNetwork(res, true);
		            wifi.setWifiEnabled(true);
				}

	            boolean success = wifi.saveConfiguration();

	            if(res != -1 && success){
	            	
	                String connectedSsidName = networkSSID;
	                Log.d(TAG, " Connection Successful: "+connectedSsidName);
	                Toast.makeText(getApplicationContext(),
	    					"Connection Successful!", Toast.LENGTH_LONG)
	    					.show();
	                
	                break;

	            }else{
	                Log.d(TAG, " Connection NOT Successful");
	                Toast.makeText(getApplicationContext(),
	    					"Error?", Toast.LENGTH_LONG)
	    					.show();
	                break;
	            }
	        }
	    }
	    
	    if(notFound == 1) {
	    	Toast.makeText(getApplicationContext(),
					"Could not find the given Access Point!", Toast.LENGTH_LONG)
					.show();
	    }
	    Log.i(TAG, "* End of connecToWifi");
	    finish();
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
	
}
