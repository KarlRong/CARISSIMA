/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rong.carissima.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

import rong.carissima.R;
import rong.carissima.util.SharedPreferencesHelper;
import zuo.biao.library.base.BaseActivity;
import zuo.biao.library.model.Entry;
import zuo.biao.library.util.Log;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends BaseActivity implements View.OnClickListener {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    private ImageView mDeleteButton;
    private ImageView mSaveView;

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mPetHasChanged = false;

    public static final String TAG = "EditorActivity";

    /**获取启动UserActivity的intent
     * @param context
     * @param userId
     * @return
     */
    public static Intent createIntent(Context context, long userId) {
        return new Intent(context, EditorActivity.class).putExtra(INTENT_ID, userId);
    }


    @Override
    public Activity getActivity() {
        return this; //必须return this;
    }    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    private long userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        userId = intent.getLongExtra(INTENT_ID, userId);
        if (userId < 0) {
            finishWithError("User doesn't exist");
            return;
        }

        //功能归类分区方法，必须调用<<<<<<<<<<
        initView();
        initData();
        initEvent();
        //功能归类分区方法，必须调用>>>>>>>>>>
    }


    public void initView(){
        StatusBarUtil.setTranslucent(this, 50);
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);

        mDeleteButton = (ImageView) findView(R.id.btn_delete);
        mSaveView = (ImageView) findView(R.id.btn_save);
    }

    private SharedPreferencesHelper sharedPreferencesHelper;
    public String mContactId;
    public String mContactName;
    public String mContactNumber;

    public void initData(){
        runUiThread(new Runnable() {
            @Override
            public void run() {

                dismissProgressDialog();
                sharedPreferencesHelper = new SharedPreferencesHelper(
                        getActivity(), SharedPreferencesHelper.CONTACTS_FILE_NAME);

                ArrayList<String[]> contactsList = sharedPreferencesHelper.getContacts();
                String[] contact = contactsList.get((int) userId);
                Log.i(TAG, "ContactID: " + contact[0] + " ContactName: " + contact[1] + "ContactNumber: " + contact[2]);
                mContactId = contact[0];
                mContactName = contact[1];
                mContactNumber = contact[2];
                mNameEditText.setText(mContactName);
                mBreedEditText.setText(mContactNumber);
                }
            });
        }

    public void initEvent(){
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);

        mDeleteButton.setOnClickListener(this);
        mSaveView.setOnClickListener(this);
    }
    /**
     * Get user input from editor and save pet into database.
     */
    private void savePet() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        mContactName = mNameEditText.getText().toString().trim();
        mContactNumber = mBreedEditText.getText().toString().trim();

        sharedPreferencesHelper.putContact(mContactId, mContactName, mContactNumber);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public void onClick(View v) {//直接调用不会显示v被点击效果
        switch (v.getId()) {
            case R.id.btn_save:
                // Save pet to database
                savePet();
                // Exit activity
                finish();
                break;
            // Respond to a click on the "Delete" menu option
            case R.id.btn_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                break;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                break;
        }
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Close the activity
        sharedPreferencesHelper.removeContact(mContactId);
        finish();

    }
}