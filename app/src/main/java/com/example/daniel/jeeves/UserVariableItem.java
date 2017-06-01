package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.example.daniel.jeeves.firebase.UserVariable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by Daniel on 24/06/16.
 */
public class UserVariableItem extends BaseAdapter {
    ArrayList<Map<String,String>>  result;

    Activity context;
    private static LayoutInflater inflater=null;
    public UserVariableItem(Activity mainActivity, ArrayList<Map<String,String>> varsList) {
        // TODO Auto-generated constructor stub
        result=varsList;
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
        View rowView = inflater.inflate(R.layout.user_variable_list, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = (TextView) rowView.findViewById(R.id.labelView);
        TextView valueView = (TextView) rowView.findViewById(R.id.valueView);
        TextView startedView = (TextView) rowView.findViewById(R.id.startedView);
        LinearLayout layout = (LinearLayout)rowView.findViewById(R.id.listlayout);
        // 4. Set the text for textView
        labelView.setText(result.get(position).get("name"));
        final String vartype = result.get(position).get("vartype");
        switch(vartype){
            case "Location":layout.setBackgroundColor(Color.RED);break;
            case "Time":layout.setBackgroundColor(Color.BLUE);break;
            case "Date":layout.setBackgroundColor(Color.MAGENTA);break;
            case "Numeric":layout.setBackgroundColor(Color.GREEN);break;
            case "Boolean":layout.setBackgroundColor(Color.CYAN);break;

        }
        final String value = result.get(position).get("value");
        valueView.setText(value);

        return rowView;
    }

}