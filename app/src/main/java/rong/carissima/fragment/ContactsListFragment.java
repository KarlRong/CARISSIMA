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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import rong.carissima.DEMO.DemoAdapter;
import rong.carissima.R;
import rong.carissima.activity.UserActivity;
import rong.carissima.util.SharedPreferencesHelper;
import zuo.biao.library.base.BaseListFragment;
import zuo.biao.library.interfaces.AdapterCallBack;
import zuo.biao.library.model.Entry;
import zuo.biao.library.util.Log;


/** 使用方法：复制>粘贴>改名>改代码 */

/**列表Fragment示例
 * @author Lemon
 * @use new DemoListFragment(),具体参考.DemoTabActivity(getFragment方法内)
 */
public class ContactsListFragment extends BaseListFragment<Entry<String, String>, ListView, DemoAdapter>
        implements View.OnClickListener {
	private static final String TAG = "ContactsListFragment";

	//与Activity通信<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**创建一个Fragment实例
	 * @return
	 */
	public static ContactsListFragment createInstance() {
		return new ContactsListFragment();
	}

	//与Activity通信>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setContentView(R.layout.demo_list_fragment);

		//功能归类分区方法，必须调用<<<<<<<<<<
		initView();
		initData();
		initEvent();
		//功能归类分区方法，必须调用>>>>>>>>>>

		onRefresh();

		return view;//返回值必须为view
	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    private ImageView mAddView;
	@Override
	public void initView() {//必须在onCreateView方法内调用
		super.initView();

		mAddView = getActivity().findViewById(R.id.btn_add_contact);
		mAddView.setOnClickListener(this);
	}

	@Override
	public void setList(final List<Entry<String, String>> list) {
		//示例代码<<<<<<<<<<<<<<<
		setList(new AdapterCallBack<DemoAdapter>() {

			@Override
			public void refreshAdapter() {
				adapter.refresh(list);
			}

			@Override
			public DemoAdapter createAdapter() {
				return new DemoAdapter(context);
			}
		});
		//示例代码>>>>>>>>>>>>>>>
	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private SharedPreferencesHelper sharedPreferencesHelper;
	@Override
	public void initData() {//必须在onCreateView方法内调用
		super.initData();
        sharedPreferencesHelper = new SharedPreferencesHelper(
                getActivity(), SharedPreferencesHelper.CONTACTS_FILE_NAME);

	}


	@Override
	public void getListAsync(int page) {
		//示例代码<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		showProgressDialog(R.string.loading);

		List<Entry<String, String>> list = new ArrayList<Entry<String, String>>();
//		for (int i = 0; i < 64; i++) {
//			list.add(new Entry<String, String>("联系人" + i , String.valueOf(1311736568 + i*i)));
//		}

		ArrayList<String[]> contactsList = sharedPreferencesHelper.getContacts();
		int nums = (Integer)sharedPreferencesHelper.getSharedPreference(SharedPreferencesHelper.CONTACT_NUMS, 0);
		for(int i = 0; i < nums; i++){
		    String[] contact = contactsList.get(i);
		    Log.i(TAG, "ContactID: " + contact[0] + " ContactName: " + contact[1] +  "ContactNumber: " + contact[2]);
            list.add(new Entry<String, String>(contact[1] , contact[2]));
        }
		onLoadSucceed(page, list);
		//示例代码>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	}


	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//Event事件区(只要存在事件监听代码就是)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	@Override
	public void initEvent() {//必须在onCreateView方法内调用
		super.initEvent();

	}


	//示例代码<<<<<<<<<<<<<<<<<<<
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		toActivity(UserActivity.createIntent(context, position));//一般用id，这里position仅用于测试 id));//
	}
	//示例代码>>>>>>>>>>>>>>>>>>>


	//系统自带监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//类相关监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    public static final int RC_GET_CONTACT = 1;

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_contact:

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                //REQUEST_CODE为自定义的请求码
                startActivityForResult(intent, RC_GET_CONTACT);

                Log.i(TAG,"Add contact button clicked!");
                break;
            default:
                break;
        }
    }



	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//系统自带监听方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//系统自带监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GET_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                //查询联系人姓名的方法
                String contactName = getContactName(cursor);
                String contactId = getContactId(cursor);
                //查询联系人电话的方法
                String contactNumber = getContactNumber(cursor);
                sharedPreferencesHelper.putContact(contactId, contactName, contactNumber );
                Log.i(TAG,"Contact name: " + contactName + " Id " + contactId + " Phone number " + contactNumber);

                onRefresh();
            }
        }
    }
	//Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//内部类,尽量少用>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private String getContactName(Cursor cursor) {
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        return name;
    }

    private String getContactId(Cursor cursor) {
        cursor.moveToFirst();
        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        return contactId;
    }

    private String getContactNumber(Cursor cursor) {
        StringBuilder builder = new StringBuilder();
        //获取所选联系人的电话的个数
        int count = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
        if (count > 0) { // 存在电话
            //获取联系人的id
            int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //根据id查询电话
            Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
            if (phoneCursor.moveToFirst()) {
                String numbler = "";
                do {
                    numbler = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    builder.append(numbler + "\n");
                } while (phoneCursor.moveToNext());
            }


        }

        return builder.toString();
    }
}