package com.example.daniel.jeeves.login;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.WelcomeActivity;
import com.example.daniel.jeeves.firebase.FirebasePatient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener{
    Context context;
    private static final int MY_PERMISSIONS = 12345;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    public static final String ANONYMOUS = "anonymous";

    //    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;

    public Activity getInstance(){
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();

        Log.d("MAIN", "Main activity created");
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_PHONE_STATE},
                MY_PERMISSIONS);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
        carryOn();

    }
    protected void carryOn(){
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API)
//                .build();

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        // mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        //  mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        //    mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        //    Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //If we're not signed in, launch the sign-in activity
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {

            //Bit of code to get the user's data from the Firebase database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference("JeevesData").child("patients").child(mFirebaseUser.getUid());
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    myRef.removeEventListener(this);
                    FirebasePatient user = dataSnapshot.getValue(FirebasePatient.class);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userphone",user.getphoneNo());
                    editor.commit();
                    Log.d("PHONEIO","Put userphone in prefs " + user.getphoneNo());
                    if(user.getcurrentStudy() != null){
                        Intent intent = new Intent(getInstance(),WelcomeActivity.class);
                        intent.putExtra("studyname",user.getcurrentStudy());
                        intent.putExtra("username",user.getname());
                        startActivity(intent);
                        finish();
                    }
                    else
                        goToSecondActivity();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        }
    }

    private void goToSecondActivity(){
        Intent intent = new Intent(this,StudySignupActivity.class);
        startActivity(intent);
        finish();
    }
    //
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return true;
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.sign_out_menu:
//                mFirebaseAuth.signOut();
//                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
//                mUsername = ANONYMOUS;
//                startActivity(new Intent(this, SignInActivity.class));
//                return true;
//            default:
        return super.onOptionsItemSelected(item);
    }
    //}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
