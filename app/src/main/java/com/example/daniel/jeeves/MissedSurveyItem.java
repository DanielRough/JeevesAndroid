package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebaseSurvey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Daniel on 24/06/16.
 */
public class MissedSurveyItem extends BaseAdapter {
    ArrayList<FirebaseSurvey> result;

    Activity context;
    private static LayoutInflater inflater=null;
    public MissedSurveyItem(Activity mainActivity, ArrayList<FirebaseSurvey> prgmNameList) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        context=mainActivity;
        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.survey_list, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = (TextView) rowView.findViewById(R.id.labelView);
        TextView valueView = (TextView) rowView.findViewById(R.id.valueView);
        TextView startedView = (TextView) rowView.findViewById(R.id.startedView);

        // 4. Set the text for textView
        labelView.setText(result.get(position).gettitle());
        final String surveyName = result.get(position).gettitle();
        final String surveyKey = result.get(position).getkey();
        final long timeSent = result.get(position).gettimeSent();
        long expiryTime = result.get(position).getexpiryTime();
        long expiryMillis = expiryTime*60*1000;
        long deadline = result.get(position).gettimeSent() + expiryMillis;
        long timeToGo = deadline - System.currentTimeMillis();
       // final long expiryTime = result.get(position).getexpiryTime();
      //  final long timeAlive = result.get(position).gettimeAlive();
        boolean begun = result.get(position).getbegun();
        if(begun)
            startedView.setText("Partially completed");
    //    long timeToGo = expiryTime - System.currentTimeMillis();
        int minutes = (int)(timeToGo /60000);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateString = formatter.format(new Date(timeSent));
        if(timeToGo > 0)
            valueView.setText("Sent at " + dateString + "\nExpiring in " + (minutes+1) + " minutes");
        else
            valueView.setText("Sent at " + dateString);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent(context, SurveyActivity.class);
                resultIntent.putExtra("surveyid",surveyKey);
                resultIntent.putExtra("name",surveyName);
                resultIntent.putExtra("timeSent",timeSent);
                result.get(position).setbegun(); //Confirm that this survey has been started
                context.startActivity(resultIntent); //So we know which one to delete
            }
        });
        return rowView;
    }

}