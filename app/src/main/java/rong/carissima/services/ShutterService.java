package rong.carissima.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import zuo.biao.library.util.Log;

public class ShutterService extends AccessibilityService {

    private static final String TAG = "ShutterService";
    private AccessibilityServiceInfo mAccessibilityServiceInfo;

    @Override
    public void onInterrupt() {

    }


//    private AuthMethod mAuth;
//    private NexmoClient mClient;

    @Override
    public void onCreate() {
        super.onCreate();
//
//        mAuth = new TokenAuthMethod(this.getString(R.string.NEXMO_API_KEY), this.getString(R.string.NEXMO_API_SECRET));
//        mClient = new NexmoClient(mAuth);

        Log.i(TAG, "ShutterService::onCreate");

//        try {
//            SmsSubmissionResult[] responses = mClient.getSmsClient().submitMessage(new TextMessage(
//                    "Acme Inc",
//                    "00393479942262",
//                    "A text message sent using the Nexmo SMS API"));
//
//            for (SmsSubmissionResult response : responses) {
//                System.out.println(response);
//                Log.i(TAG,response.toString());
//            }
//        }
//        catch (IOException ie){
//            Log.e(TAG,"Message IOException");
//        }catch(NexmoClientException ie){
//            Log.e(TAG,"Message NexmoClientException");
//        }


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
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    private void sendMessage() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("00393336147389", null,
                "I'm in danger", null, null);
    }

    private void callEmergencyContact(){
        Intent Intent = new Intent();
        Intent.setAction(Intent.ACTION_CALL);
        Intent.setData(Uri.parse("tel:00393336147389"));
        startActivity(Intent);
    }

}
