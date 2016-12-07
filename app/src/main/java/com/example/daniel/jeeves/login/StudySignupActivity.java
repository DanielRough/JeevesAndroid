package com.example.daniel.jeeves.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.SenseActivity;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class StudySignupActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();
    String selectedStudy;
    public Activity getInstance(){
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("JeevesData").child("patients").child(mFirebaseUser.getUid());
        setContentView(R.layout.activity_study_signup);
        EditText txtStudyId = (EditText) findViewById(R.id.textStudyId);
        final ListView lstStudies = (ListView)findViewById(R.id.lstStudies);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Start study");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getInstance(),SenseActivity.class);
                intent.putExtra("studyname",selectedStudy);
                dialog.dismiss();
                myRef.child("currentStudy").setValue(selectedStudy);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
// 3. Get the AlertDialog from create()
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        lstStudies.setAdapter(adapter);
        lstStudies.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                              public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                                                  Log.i("DOES","THIS HAPPEN");
                                                  selectedStudy= lstStudies.getItemAtPosition(position).toString();
                                                  builder.setMessage("Are you sure you want to start study " + selectedStudy + "?");
                                                  Log.i("Selected study is ",selectedStudy);
                                                  AlertDialog dialog = builder.create();
                                                  dialog.show();
                                                  // String value= selItem.getTheValue(); //getter method

                                              }
                                          });
        DatabaseReference projectsRef = database.getReference("JeevesData").child("projects");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Iterable<DataSnapshot> post = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iter = post.iterator();
                while(iter.hasNext()){
                    FirebaseProject proj = iter.next().getValue(FirebaseProject.class);
                    String name = proj.getname();
                    listItems.add(name);
                    adapter.notifyDataSetChanged();

                }
            //    txtWelcome.setText("Welcome, " + post.getname() + "!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        projectsRef.addValueEventListener(postListener);
    }
}
