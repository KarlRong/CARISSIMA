package rong.carissima.util;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 保存信息配置类
 *
 * @author admin
 */
public class SharedPreferencesHelper {
    public static final String CONTACTS_FILE_NAME = "EmergencyContacts";
    private SharedPreferences sharedPreferences;
    /*
     * 保存手机里面的名字
     */private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context,String FILE_NAME) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * 存储
     */
    public void put(String key, Object object) {
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.commit();
    }

    public final static String CONTACT_NUMS  = "ContactNums";
    public final static String ITEM = "Item";
    public final static String CONTACT_ID = "ContactId";
    public final static String CONTACT_NAME = "ContactName";
    public final static String CONTACT_NUMBER = "ContactNumber";

    public void putContact(String contactId, String contactName, String contactNumber){
        Integer nums = 0;
        if(contain(CONTACT_NUMS)){
            nums = (Integer)getSharedPreference(CONTACT_NUMS, 0);
        }
        int index = nums + 1;
        put(ITEM + index + CONTACT_ID, contactId);
        put(ITEM + index + CONTACT_NAME, contactName);
        put(ITEM + index + CONTACT_NUMBER, contactNumber);
        nums = index;
        put(CONTACT_NUMS, nums);
    }
    /**
     * 获取保存的数据
     */
    public Object getSharedPreference(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return sharedPreferences.getString(key, null);
        }
    }

    public ArrayList<String[]> getContacts(){
        ArrayList<String[]> contactsList = new ArrayList();
        if(!contain(CONTACT_NUMS)){
            return contactsList;
        }
        int nums = (Integer)getSharedPreference(CONTACT_NUMS, 0);
        for(int i = 1; i <= nums; i++){
            int index = i;
            String contactId = getSharedPreference(ITEM + index + CONTACT_ID, "").toString().trim();
            String contactName = getSharedPreference(ITEM + index + CONTACT_NAME, "").toString().trim();
            String contactNumber = getSharedPreference(ITEM + index + CONTACT_NUMBER, "").toString().trim();
            String[] contact = {contactId, contactName, contactNumber};
            contactsList.add(contact);
        }
        return contactsList;
    }

    /**
     * 移除某个key值已经对应的值
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    /**
     * 查询某个key是否存在
     */
    public Boolean contain(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * 返回所有的键值对
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}