package rong.carissima.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
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

import java.util.ArrayList;
import java.util.List;

import rong.carissima.R;
import rong.carissima.activity.MainTabActivity;
import rong.carissima.util.SharedPreferencesHelper;
import zuo.biao.library.model.Entry;
import zuo.biao.library.util.Log;
import zuo.biao.library.util.SettingUtil;

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
    private SharedPreferencesHelper sharedPreferencesHelper;

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

        sharedPreferencesHelper = new SharedPreferencesHelper(
                getBaseContext(), SharedPreferencesHelper.CONTACTS_FILE_NAME);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        flashLightOff();
    }

    public ArrayList<String> mContactList;

    private boolean[] settings;

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        switch(key){
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.i(TAG, "KEYCODE_VOLUME_UP");
                settings = SettingUtil.getAllBooleans(getBaseContext());
                getContactLists();
                if(settings[2]){
                    callEmergencyContact();
                }
                if(settings[3]){
                    sendMessage();
                }
                if(settings[4]){
                    flashLightOn();
                }
                 break;
            case KeyEvent.KEYCODE_ENTER:
                Log.i(TAG, "KEYCODE_ENTER");
                settings = SettingUtil.getAllBooleans(getBaseContext());
                getContactLists();
                if(settings[2]){
                    callEmergencyContact();
                }
                if(settings[3]){
                    sendMessage();
                }
                if(settings[4]){
                    flashLightOn();
                }
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
        for (String contactNumber : mContactList) {
            Log.i(TAG, "sendMessage: " + "Latitude: " + mLocation.getLatitude() + " Longitude: " + mLocation.getLongitude()
                    + " Altitude: " + mLocation.getAltitude() + " Time: " + mLocation.getTime());
            String locationLink = "https://maps.google.com/?q=" + mLocation.getLatitude() + "," + mLocation.getLongitude();
            String smsContent = "I'm in danger!\n" + locationLink;
            Log.i(TAG, "Contact: " + contactNumber + " Message: " + smsContent);
//        SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(contactNumber, null,
//                smsContent, null, null);
        }
    }

    private void callEmergencyContact(){
        if(mContactList.size() > 0) {
            String contactNumber = mContactList.get(0);
            Log.i(TAG, "callContact: " + "Latitude: " + mLocation.getLatitude() + " Longitude: " + mLocation.getLongitude()
                    + " Altitude: " + mLocation.getAltitude());
            String locationLink = "https://maps.google.com/?q=" + mLocation.getLatitude() + "," + mLocation.getLongitude();
            Log.i(TAG, locationLink);
            Intent Intent = new Intent();
            Intent.setAction(Intent.ACTION_CALL);
            Intent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(Intent);
        }
    }

    private boolean flashLightStatus = false;
    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
        } catch (CameraAccessException e) {
        }
    }


    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
        } catch (CameraAccessException e) {
        }
    }

    private void getContactLists() {
        ArrayList<String[]> contactsList = sharedPreferencesHelper.getContacts();
        mContactList = new ArrayList<>();
        int nums = (Integer) sharedPreferencesHelper.getSharedPreference(SharedPreferencesHelper.CONTACT_NUMS, 0);
        for (int i = 0; i < nums; i++) {
            String[] contact = contactsList.get(i);
            Log.i(TAG, "ContactID: " + contact[0] + " ContactName: " + contact[1] + "ContactNumber: " + contact[2]);
            mContactList.add(contact[2]);
        }
    }
}
