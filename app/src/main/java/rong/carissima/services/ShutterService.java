package rong.carissima.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;

import rong.carissima.activity.MainTabActivity;
import zuo.biao.library.util.Log;

public class ShutterService extends AccessibilityService
        implements LocationEngineListener {

    private static final String TAG = "ShutterService";
    private AccessibilityServiceInfo mAccessibilityServiceInfo;

    @Override
    public void onInterrupt() {

    }


//    private AuthMethod mAuth;
//    private NexmoClient mClient;

    private LocationEngine locationEngine;
    private Location mLocation;

    @Override
    public void onCreate() {
        super.onCreate();
//
//        mAuth = new TokenAuthMethod(this.getString(R.string.NEXMO_API_KEY), this.getString(R.string.NEXMO_API_SECRET));
//        mClient = new NexmoClient(mAuth);

        Log.i(TAG, "ShutterService::onCreate");

        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(getApplicationContext());
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location mLocation = locationEngine.getLastLocation();
        if (mLocation != null) {
            Log.i(TAG, "LastLocation: " + "Latitude: " + mLocation.getLatitude() + " Longitude: " + mLocation.getLongitude()
                    + " Altitude: " + mLocation.getAltitude());
        }
        locationEngine.addLocationEngineListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        switch(key){
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.i(TAG, "KEYCODE_VOLUME_UP");

//                sendMessage();
                callEmergencyContact();
                break;
            case KeyEvent.KEYCODE_ENTER:
                Log.i(TAG, "KEYCODE_ENTER");

                callEmergencyContact();
                break;
        }
        return super.onKeyEvent(event);
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLocation = location;
            Log.i(TAG, "onLocationChanged " + "Latitude: " + mLocation.getLatitude() + " Longitude: " + mLocation.getLongitude()
                        + " Altitude: " + mLocation.getAltitude());
//            locationEngine.requestLocationUpdates();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    private void sendMessage() {

        Log.i(TAG, "sendMessage: " + "Latitude: " + mLocation.getLatitude() + " Longitude: " + mLocation.getLongitude()
                + " Altitud e: " + mLocation.getAltitude() + " Time: " + mLocation.getTime());
        String locationLink = "https://maps.google.com/?q=" + mLocation.getLatitude() + "," + mLocation.getLongitude();
        String smsContent = "I'm in danger\n" + locationLink;
        Log.i(TAG, "Message: " + smsContent);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("00393336147389", null,
                smsContent, null, null);
    }

    private void callEmergencyContact(){
        Log.i(TAG, "callContact: " + "Latitude: " + mLocation.getLatitude() + " Longitude: " + mLocation.getLongitude()
                + " Altitude: " + mLocation.getAltitude());
        String locationLink = "https://maps.google.com/?q=" + mLocation.getLatitude() + "," + mLocation.getLongitude();
        Log.i(TAG, locationLink);
        Intent Intent = new Intent();
        Intent.setAction(Intent.ACTION_CALL);
        Intent.setData(Uri.parse("tel:00393336147389"));
        startActivity(Intent);
    }

}
