package com.jeevesandroid.mainscreens.questions;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;
import com.jeevesandroid.mainscreens.TriggerViewActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TimeListQuestion extends Question {
    private ListView lstTimeList;

    public TimeListQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }

    ArrayList<String> foodlist;
    @Override
    public void handle(int position) {
        lstTimeList = qView.findViewById(R.id.lstItemsandQuants);
        Button btnNewFood = qView.findViewById(R.id.btnNewFood);
         foodlist = new ArrayList<>();

        btnNewFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foodlist = new ArrayList<>();
                String answer = answers.get(currentIndex);

                String[] allanswers = answer.split(",,");
                for (String ans : allanswers) {
                    foodlist.add(ans);
                }
                foodlist.add("");
                TimeListItem adapter = new TimeListItem(context, foodlist);
                lstTimeList.setAdapter(adapter);
            }
        });

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String[] allanswers = answer.split(",,");
            for (String ans : allanswers) {
                foodlist.add(ans);
            }
        } else {
            answers.set(currentIndex, "");
            foodlist.add("");
        }
        TimeListItem adapter = new TimeListItem(context, foodlist);
        lstTimeList.setAdapter(adapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_timequantlist;
    }

    /**
     * Custom list item
     */
    class TimeListItem extends BaseAdapter {
        final ArrayList<String> result;
        final Activity context;


        TimeListItem(Activity mainActivity, ArrayList<String> prgmNameList) {
            result = prgmNameList;
            context = mainActivity;
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
                .getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.timeqlistitem, parent, false);
            final TextView timeText = rowView.findViewById(R.id.txtFoodTime);
            final EditText quantText = rowView.findViewById(R.id.txtFoodQuant);
            final EditText itemText = rowView.findViewById(R.id.txtFoodType);

            String[] res = result.get(position).split("\\|");
            if(res.length > 0) {
                timeText.setText(res[0]);
            }
            if(res.length > 1){
                quantText.setText(res[1]);
            }
            if(res.length > 2){
                itemText.setText(res[2]);
            }
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void afterTextChanged(Editable editable) {
                    String listItem = timeText.getText() + "|" + quantText.getText() + "|" + itemText.getText();
                    result.set(position,listItem);
                    StringBuilder sb = new StringBuilder();
                    for (String s : result)
                    {
                        sb.append(s);
                        sb.append(",,");
                    }
                    answers.set(currentIndex,sb.toString());
                    Log.d("ANSER",sb.toString());

                }
            };
           // timeText.addTextChangedListener(watcher);
            quantText.addTextChangedListener(watcher);
            itemText.addTextChangedListener(watcher);

            timeText.setClickable(true);
            timeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTimePicker(position,timeText,quantText,itemText);
                }
            });

            return rowView;
        }
        public void showTimePicker(int position,TextView timeText, EditText quantText, EditText itemText){
            final TextView t = timeText;
            final EditText q = quantText;
            final EditText i = itemText;
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            final int pos = position;
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    t.setText( selectedHour + ":" + selectedMinute);
                    String listItem = t.getText() + "|" + q.getText() + "|" + i.getText();
                    result.set(pos,listItem);
                    StringBuilder sb = new StringBuilder();
                    for (String s : result)
                    {
                        sb.append(s);
                        sb.append(",,");
                    }
                    answers.set(currentIndex,sb.toString());
                    Log.d("ANSER",sb.toString());
                }
            }, hour, minute, true);
            mTimePicker.setTitle("Select Time");
           // mTimePicker.set
            mTimePicker.show();

        }
    }

}
