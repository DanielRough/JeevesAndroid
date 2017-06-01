package com.example.daniel.jeeves;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class UpdateSettingsActivity extends AppCompatActivity {

    EditText contact;
    Button btnSelectContact;
    Button btnSaveContact;
    SharedPreferences.Editor editor;
    static int PICK_REQUEST = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_update_setting);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        contact = (EditText)findViewById(R.id.txtContact);
        btnSelectContact = (Button)findViewById(R.id.btnSelectContact);
        btnSaveContact = (Button)findViewById(R.id.btnSaveContact);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        editor = preferences.edit();
        btnSaveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("contact",contact.getText().toString());
                editor.commit();
            }
        });

        btnSelectContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, PICK_REQUEST);
            }
        });



    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contacturi =data.getData();
                String number = getContactPhoneNumber(contacturi);
                contact.setText(number);
            }
        }
    }


    public String getContactPhoneNumber(Uri contactUri) {
        // Get the name
        String mime;    // MIME type
        int dataIdx;    // Index of DATA1 column
        int mimeIdx;    // Index of MIMETYPE column
        int nameIdx;
        Cursor cursor = getContentResolver().query(contactUri,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                null, null, null);
        if (cursor.moveToFirst()) {
            nameIdx = cursor.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME);
            String name = cursor.getString(nameIdx);

            // Set up the projection
            String[] projection = {
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.Contacts.Data.DATA1,
                    ContactsContract.Contacts.Data.MIMETYPE};

            // Query ContactsContract.Data
            cursor = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI, projection,
                    ContactsContract.Data.DISPLAY_NAME + " = ?",
                    new String[]{name},
                    null);

            if (cursor.moveToFirst()) {
                // Get the indexes of the MIME type and data
                mimeIdx = cursor.getColumnIndex(
                        ContactsContract.Contacts.Data.MIMETYPE);
                dataIdx = cursor.getColumnIndex(
                        ContactsContract.Contacts.Data.DATA1);

                // Match the data to the MIME type, store in variables
                do {
                    mime = cursor.getString(mimeIdx);
                    if (ContactsContract.CommonDataKinds.Email
                            .CONTENT_ITEM_TYPE.equalsIgnoreCase(mime)) {
                        String email = cursor.getString(dataIdx);
                    }
                    if (ContactsContract.CommonDataKinds.Phone
                            .CONTENT_ITEM_TYPE.equalsIgnoreCase(mime)) {
                        String phone = cursor.getString(dataIdx);
                        phone = PhoneNumberUtils.formatNumber(phone);
                        return phone;
                    }
                } while (cursor.moveToNext());
            }
        }
        return "";
    }
}
