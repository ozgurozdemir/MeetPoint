package com.example.ozgurozdemir.meetpoint;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

// Customized list view adapter class with generic type in order to use many types
// such that <Person>, <Meeting>, <String>
public class CustomAdapter<T> extends ArrayAdapter<T>{

    // Deciding the which customized view will used
    String custom;
    ArrayList<String> participants = new ArrayList<String>();

    public CustomAdapter(Context context, ArrayList<T> items, String custom) {
        super(context, 0, items);
        this.custom = custom;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T item = getItem(position);
        // Using the adapter for events
        if (custom.equals("event")){
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_events, parent, false);
            }
            // Lookup view for data population
            TextView hourText = (TextView) convertView.findViewById(R.id.eventListHourTxt);
            TextView dateText = (TextView) convertView.findViewById(R.id.eventListDateTxt);
            TextView nameText = (TextView) convertView.findViewById(R.id.eventListNameTxt);
            // Populate the data into the template view using the data object
            String[] _ = ((Meeting)item).getDate().split(" ");
            String[] date = _[0].split("-");
            String hour = _[1];
            hourText.setText(hour);
            dateText.setText(date[2] + "/" + date[1] + "/" + date[0]);
            nameText.setText(((Meeting)item).getName());
        }

        // Using the adapter for friends
        else if (custom.equals("people")){
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_events, parent, false);
            }
            // Lookup view for data population
            TextView hourText = (TextView) convertView.findViewById(R.id.eventListHourTxt);
            // Populate the data into the template view using the data object
            String name = item.toString().split("-")[0];
            hourText.setText(name);
        }

        // Using the adapter for participants from HomeActivity
        else if (custom.equals("friends")){
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_events, parent, false);
            }
            // Lookup view for data population
            TextView hourText = (TextView) convertView.findViewById(R.id.eventListHourTxt);
            // Populate the data into the template view using the data object
            if(participants.contains(item)){
                convertView.setBackgroundResource(R.color.colorPrimary);
                hourText.setTextColor(Color.WHITE);
            } else {
                convertView.setBackgroundResource(R.color.background);
                hourText.setTextColor(Color.GRAY);
            }
            String name = item.toString().split("-")[0];
            hourText.setText(name);
    }

        return convertView;

    }

}
