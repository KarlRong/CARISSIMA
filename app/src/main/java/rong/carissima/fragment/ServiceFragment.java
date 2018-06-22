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

package rong.carissima.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rong.carissima.DEMO.DemoAdapter;
import rong.carissima.R;
import rong.carissima.activity.UserActivity;
import zuo.biao.library.base.BaseFragment;
import zuo.biao.library.model.Entry;
import zuo.biao.library.util.Log;


/** 使用方法：复制>粘贴>改名>改代码 */

/**fragment示例
 * @author Lemon
 * @use new DemoFragment(),具体参考.DemoFragmentActivity(initData方法内)
 */
public class ServiceFragment extends BaseFragment implements
        View.OnClickListener {
	private static final String TAG = "ServiceFragment";

	//与Activity通信<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	public static final String ARGUMENT_SERVICE_STATE = "ARGUMENT_USER_ID";

	/**创建一个Fragment实例
	 * @return
	 */
	public static ServiceFragment createInstance() {
		return createInstance(false);
	}
	/**创建一个Fragment实例
	 * @param serviceState
	 * @return
	 */
	public static ServiceFragment createInstance(boolean serviceState) {
		ServiceFragment fragment = new ServiceFragment();

		Bundle bundle = new Bundle();
		bundle.putBoolean(ARGUMENT_SERVICE_STATE, serviceState);

		fragment.setArguments(bundle);
		return fragment;
	}

	//与Activity通信>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



	private boolean mServiceState = false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		//TODO demo_fragment改为你所需要的layout文件
		setContentView(R.layout.service_fragment);

		argument = getArguments();
		if (argument != null) {
			mServiceState = argument.getBoolean(ARGUMENT_SERVICE_STATE, mServiceState);
		}

		//功能归类分区方法，必须调用<<<<<<<<<<
		initView();
		initData();
		initEvent();
		//功能归类分区方法，必须调用>>>>>>>>>>

		return view;//返回值必须为view
	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	//示例代码<<<<<<<<
	private Button btActiveService;
	//示例代码>>>>>>>>
	@Override
	public void initView() {//必须在onCreateView方法内调用

		//示例代码<<<<<<<<<<<<<<
		btActiveService = findView(R.id.bt_activeService);
		//示例代码>>>>>>>>>>>>>>
	}

	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	//示例代码<<<<<<<<
    public BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final String SHUTTER_NAME = "AB Shutter3";
    public String mShutterAddress;
	//示例代码>>>>>>>>>
	@Override
	public void initData() {//必须在onCreateView方法内调用

		//示例代码<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBoundedShutter();

		showShortToast(TAG + ": serviceState = " + mServiceState);

		showProgressDialog(R.string.loading);

		runThread(TAG + "initData", new Runnable() {
			@Override
			public void run() {
				runUiThread(new Runnable() {
					@Override
					public void run() {
						dismissProgressDialog();
					}
				});
			}
		});

		//示例代码>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	}


	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//Event事件区(只要存在事件监听代码就是)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	@Override
	public void initEvent() {//必须在onCreateView方法内调用
		//示例代码<<<<<<<<<<<<<<<<<<<
		findView(R.id.bt_activeService).setOnClickListener(this);
		//示例代码>>>>>>>>>>>>>>>>>>>
	}

	//系统自带监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//类相关监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<





	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//系统自带监听方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_activeService:
                Log.i(TAG,"Button clicked!");
                break;
            default:
                break;
        }
    }
	//系统自带监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//内部类,尽量少用>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    protected boolean checkBoundedShutter() {
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.w(TAG, "This device does not support Bluetooth");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                Log.i(TAG, device.getName() + "\n" + device.getAddress() + "\n");
                if (device.getName() == SHUTTER_NAME) {
                    mShutterAddress = device.getAddress();
                    Log.i(TAG, SHUTTER_NAME + "Found in bounded pairs ");
                    return true;
                }
                else{Log.i(TAG, SHUTTER_NAME + "Found in bounded pairs ");}
            }
        } else {
            Log.i(TAG, "Bluetooth device not found");
            return false;
        }
        return false;
    }

}