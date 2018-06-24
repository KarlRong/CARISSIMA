/*Copyright ©2015 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package rong.carissima.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jaeger.library.StatusBarUtil;

import rong.carissima.DEMO.DemoListFragment;
import rong.carissima.DEMO.DemoTabFragment;
import rong.carissima.R;
import rong.carissima.fragment.ContactsListFragment;
import rong.carissima.fragment.MapFragment;
import rong.carissima.fragment.ServiceFragment;
import rong.carissima.fragment.SettingFragment;
import rong.carissima.fragment.UserListFragment;
import rong.carissima.fragment.UserRecyclerFragment;
import zuo.biao.library.base.BaseBottomTabActivity;
import zuo.biao.library.interfaces.OnBottomDragListener;
import zuo.biao.library.manager.SystemBarTintManager;
import zuo.biao.library.util.Log;

/**应用主页
 * @author Lemon
 * @use MainTabActivity.createIntent(...)
 */
public class MainTabActivity extends BaseBottomTabActivity implements OnBottomDragListener {
		private static final String TAG = "MainTabActivity";


	//启动方法<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**启动这个Activity的Intent
	 * @param context
	 * @return
	 */
	public static Intent createIntent(Context context) {
		return new Intent(context, MainTabActivity.class);
	}


	//启动方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	@Override
	public Activity getActivity() {
		return this; //必须return this;
	}

    public static final String ANONYMOUS = "anonymous";
	private FirebaseAuth mFirebaseAuth;
	private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final int RC_SIGN_IN = 1;

	private String mUsername;

    private ImageView mAddView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_activity, this);

       	//功能归类分区方法，必须调用<<<<<<<<<<
		initView();
		initData();
		initEvent();
		//功能归类分区方法，必须调用>>>>>>>>>>
	}

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }


    // UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	private DemoTabFragment demoTabFragment;
	@Override
	public void initView() {// 必须调用
		super.initView();
		exitAnim = R.anim.bottom_push_out;

        mAddView = findViewById(R.id.btn_add_contact);

        StatusBarUtil.setTranslucent(this, 50);

        demoTabFragment = DemoTabFragment.createInstance("杭州");
	}


	@Override
	protected int[] getTabClickIds() {
		return new int[]{R.id.llBottomTabTab0, R.id.llBottomTabTab1, R.id.llBottomTabTab2, R.id.llBottomTabTab3};
	}

	@Override
	protected int[][] getTabSelectIds() {
		return new int[][]{
				new int[]{R.id.ivBottomTabTab0, R.id.ivBottomTabTab1, R.id.ivBottomTabTab2, R.id.ivBottomTabTab3},//顶部图标
				new int[]{R.id.tvBottomTabTab0, R.id.tvBottomTabTab1, R.id.tvBottomTabTab2, R.id.tvBottomTabTab3}//底部文字
		};
	}

	@Override
	public int getFragmentContainerResId() {
		return R.id.flMainTabFragmentContainer;
	}

	@Override
    public void selectFragment(int position) {
	    super.selectFragment(position);
	    if(position == 2){
            mAddView.setVisibility(View.VISIBLE);
        }else {
            mAddView.setVisibility(View.GONE);
        }
    }
	@Override
	protected Fragment getFragment(int position) {
		switch (position) {
		case 1:
			return MapFragment.createInstance();
		case 2:
//		    return UserRecyclerFragment.createInstance(UserListFragment.RANGE_RECOMMEND);
//            return DemoTabFragment.createInstance("杭州");
//            return UserListFragment.createInstance(UserListFragment.RANGE_ALL);
            return ContactsListFragment.createInstance();
		case 3:
			return SettingFragment.createInstance();
		default:
//			return UserListFragment.createInstance(UserListFragment.RANGE_ALL);
            return ServiceFragment.createInstance(false);
		}
	};

	private static final String[] TAB_NAMES = {"Carissima", "Map", "Contact", "Settings"};
	@Override
	protected void selectTab(int position) {
		//导致切换时闪屏，建议去掉BottomTabActivity中的topbar，在fragment中显示topbar
		//		rlBottomTabTopbar.setVisibility(position == 2 ? View.GONE : View.VISIBLE);

		tvBaseTitle.setText(TAB_NAMES[position]);
//
//		//点击底部tab切换顶部tab，非必要
//		if (position == 2 && position == currentPosition && demoTabFragment != null) {
//			demoTabFragment.selectNext();
//		}
	}


	// UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	// Data数据区(存在数据获取或处理代码，但不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




	@Override
	public void initData() {// 必须调用
		super.initData();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUsername = ANONYMOUS;

        checkPlayServices();

		// Mapbox Access token
//		Mapbox.getInstance(getApplicationContext(), this.getString(R.string.mapbox_token));

	}



	// Data数据区(存在数据获取或处理代码，但不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	// Event事件区(只要存在事件监听代码就是)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	@Override
	public void initEvent() {// 必须调用
		super.initEvent();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
	}

	@Override
	public void onDragBottom(boolean rightToLeft) {
		//将Activity的onDragBottom事件传递到Fragment，非必要<<<<<<<<<<<<<<<<<<<<<<<<<<<
		switch (currentPosition) {
		case 2:
			if (demoTabFragment != null) {
				if (rightToLeft) {
//					demoTabFragment.selectMan();
				} else {
//					demoTabFragment.selectPlace();
				}
			}
			break;
		default:
			break;
		}
		//将Activity的onDragBottom事件传递到Fragment，非必要>>>>>>>>>>>>>>>>>>>>>>>>>>>
	}


	// 系统自带监听方法<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "activityresult called!", Toast.LENGTH_SHORT).show();
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	//双击手机返回键退出<<<<<<<<<<<<<<<<<<<<<
	private long firstTime = 0;//第一次返回按钮计时
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			long secondTime = System.currentTimeMillis();
			if(secondTime - firstTime > 2000){
				showShortToast("再按一次退出");
				firstTime = secondTime;
			} else {//完全退出
				moveTaskToBack(false);//应用退到后台
				System.exit(0);
			}
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}
	//双击手机返回键退出>>>>>>>>>>>>>>>>>>>>>

	// 类相关监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	// 类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 系统自带监听方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        printToast(parseKeyCode(keyCode));
        return true;
    }

    public String parseKeyCode(int keyCode) {
        String ret = "";
        Log.i(TAG, "key code :" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_POWER:
                // 监控/拦截/屏蔽电源键 这里拦截不了
                ret = "get Key KEYCODE_POWER(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_RIGHT_BRACKET:
                // 监控/拦截/屏蔽返回键
                ret = "get Key KEYCODE_RIGHT_BRACKET";
                break;
            case KeyEvent.KEYCODE_MENU:
                // 监控/拦截菜单键
                ret = "get Key KEYCODE_MENU";
                break;
            case KeyEvent.KEYCODE_HOME:
                // 由于Home键为系统键，此处不能捕获
                ret = "get Key KEYCODE_HOME";
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                // 监控/拦截/屏蔽上方向键
                ret = "get Key KEYCODE_DPAD_UP";
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                // 监控/拦截/屏蔽左方向键
                ret = "get Key KEYCODE_DPAD_LEFT";
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // 监控/拦截/屏蔽右方向键
                ret = "get Key KEYCODE_DPAD_RIGHT";
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // 监控/拦截/屏蔽下方向键
                ret = "get Key KEYCODE_DPAD_DOWN";
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // 监控/拦截/屏蔽中方向键
                ret = "get Key KEYCODE_DPAD_CENTER";
                break;
            case KeyEvent.FLAG_KEEP_TOUCH_MODE:
                // 监控/拦截/屏蔽长按
                ret = "get Key FLAG_KEEP_TOUCH_MODE";
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                // 监控/拦截/屏蔽下方向键
                ret = "get Key KEYCODE_VOLUME_DOWN(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_VOLUME_UP://Shutter 提供
                // 监控/拦截/屏蔽中方向键
                ret = "get Key KEYCODE_VOLUME_UP(KeyCode:" + keyCode + ")";
                break;
            case 220:
                // case KeyEvent.KEYCODE_BRIGHTNESS_DOWN:
                // 监控/拦截/屏蔽亮度减键
                ret = "get Key KEYCODE_BRIGHTNESS_DOWN(KeyCode:" + keyCode + ")";
                break;
            case 221:
                // case KeyEvent.KEYCODE_BRIGHTNESS_UP:
                // 监控/拦截/屏蔽亮度加键
                ret = "get Key KEYCODE_BRIGHTNESS_UP(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                ret = "get Key KEYCODE_MEDIA_PLAY(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                ret = "get Key KEYCODE_MEDIA_PAUSE(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                ret = "get Key KEYCODE_MEDIA_PREVIOUS(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                ret = "get Key KEYCODE_MEDIA_PLAY_PAUSE(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                ret = "get Key KEYCODE_MEDIA_NEXT(KeyCode:" + keyCode + ")";
                break;
            default:
                ret = "keyCode: "
                        + keyCode
                        + " (http://developer.android.com/reference/android/view/KeyEvent.html)";
                break;
        }
        return ret;
    }

    public void printToast(String str) {
        Log.i(TAG, str);
    }
	// Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



	// 内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	// 内部类,尽量少用>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

     // 自定义方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     private void onSignedInInitialize(String username) {
         mUsername = username;
         Log.i(TAG, "User name:" + mUsername);
     }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
    }

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private boolean checkPlayServices() {
		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);
		Log.i(TAG,"GooglePlayServiceAvailable: " + result);
		if(result != ConnectionResult.SUCCESS) {
			if(googleAPI.isUserResolvableError(result)) {
				googleAPI.getErrorDialog(this, result,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}

			return false;
		}

		return true;
	}
}