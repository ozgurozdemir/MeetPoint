package com.example.ozgurozdemir.meetpoint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class EventsActivity extends AppCompatActivity {

    // variable initialize
    private RelativeLayout noEventLayout;
    private RelativeLayout eventListLayout;
    private Button goHomeBtn;
    private ListView eventList;

    private String personName, personID;

    private DrawerLayout nDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigation;

    private ArrayList<Meeting> events;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        getSupportActionBar().setTitle("Events");

        // Initializing the database variable
        File myDB = getApplication().getFilesDir();
        final String path = myDB +  "/" + "MeetPointDB";
        database = new Database(path);

        // Initializing the intent
        final Intent intent = getIntent();
        personID = intent.getStringExtra("personID");
        personName = intent.getStringExtra("personName");

        // Side Bar Setup
        // Initialize DrawerLayout variable
        nDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, nDrawerLayout, R.string.open, R.string.close);
        nDrawerLayout.addDrawerListener(mToggle);
        navigation = (NavigationView) findViewById(R.id.navigation);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(navigation);

        // Initialize different layouts
        noEventLayout = (RelativeLayout) findViewById(R.id.noEventLayout);
        eventListLayout = (RelativeLayout) findViewById(R.id.eventListLayout);

        eventList = (ListView) findViewById(R.id.eventList);

        events = database.getEvents(intent.getStringExtra("personID"));
        // If user haven't got any meeting then view the layout that tells user to go home activity and make a
        // schedule
        if(events.size()==0){
            noEventLayout.setVisibility(RelativeLayout.VISIBLE);
            eventListLayout.setVisibility(RelativeLayout.GONE);
        } else {
            // Otherwise view the meetings that user have got
            noEventLayout.setVisibility(RelativeLayout.GONE);
            eventListLayout.setVisibility(RelativeLayout.VISIBLE);
        }
        CustomAdapter<Meeting> adapter = new CustomAdapter<Meeting>(this, events, "event");
        eventList.setAdapter(adapter);
        registerForContextMenu(eventList);

        // Button initializer for noEventLayout for going home activity
        goHomeBtn = (Button) findViewById(R.id.goHomeBtn);
        goHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EventsActivity.this, HomeActivity.class);
                i.putExtra("personName", personName);
                i.putExtra("personID", personID);
                startActivity(i);
                finish();
            }
        });

    }

    // Context Menus for ListView
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.eventList) {
            menu.add("Detail");
            menu.add("Send SMS");
            menu.add("Send Mail");
            menu.add("Cancel Meeting");
        }
    }

    // Context Menu Item Selected Methods
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position = menuInfo.position;

        // If detail is selected creates a popup and view the details of meeting
        if(item.getTitle().equals("Detail")){
            final Dialog popUp = new Dialog(this);
            popUp.setContentView(R.layout.popup_meeting);
            // Lookup view for data population
            TextView meetingHourTxt = popUp.findViewById(R.id.meetingHourTxt);
            TextView meetingDateTxt = popUp.findViewById(R.id.meetingDateTxt);
            TextView meetingNameTxt = popUp.findViewById(R.id.meetingNameTxt);
            TextView meetingLocationTxt = popUp.findViewById(R.id.meetingLocationTxt);
            TextView meetingParticipantsTxt = popUp.findViewById(R.id.meetingParticipantsTxt);
            TextView meetingNoteTxt = popUp.findViewById(R.id.meetingNoteTxt);
            // Get selected meeting information
            Meeting current = events.get(position);
            String[] _ = current.getDate().split(" ");
            String hour = _[1];
            String date = _[0].split("-")[2] + "/" + _[0].split("-")[1] + "/" + _[0].split("-")[0];
            // Setting the information of meeting
            meetingHourTxt.setText(hour);
            meetingDateTxt.setText(date);
            meetingNameTxt.setText(current.getName());
            meetingLocationTxt.setText(current.getName());
            meetingParticipantsTxt.setText(database.getParticipants(current.getId()));
            meetingNoteTxt.setText(current.getNote());

            // User can cancel detailed meeting from that view with delete button
            Button deleteMeetingBtn = popUp.findViewById(R.id.deleteMeetingBtn);
            deleteMeetingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(EventsActivity.this);
                    alert.setTitle("Cancel Meeting");
                    final Meeting current = events.get(position);
                    String msg = "Are you sure to cancel meeting that information's given above?";
                    alert.setMessage(msg);

                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // After taking id delete meeting object
                            database.cancelMeeting(current.getId());
                            // Then return a successful message
                            final AlertDialog.Builder alert = new AlertDialog.Builder(EventsActivity.this);
                            alert.setMessage("Your meeting canceled successfully.");
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(EventsActivity.this, EventsActivity.class);
                                    intent.putExtra("personID", personID);
                                    intent.putExtra("personName", personName);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            alert.create();
                            alert.show();
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alert.create();
                    alert.show();
                }
            });

            // Popup dismiss button
            Button okMeetingBtn = popUp.findViewById(R.id.okMeetingBtn);
            okMeetingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUp.dismiss();
                }
            });

            popUp.show();
        }

        // If 'send sms' is selected direct to SMS application with participants phone number
        // and meeting details
        if(item.getTitle().equals("Send SMS")){
            // Getting phoneNumbers of participants
            String phoneNumbers = database.getParticipantsPhoneNumber(personID, events.get(position).getId());
            if(!phoneNumbers.equals("")) {
                String msg = "We have meeting information given below:\n\tLocation: " + events.get(position).getName() +
                        "\n\tAt: " + events.get(position).getDate();
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumbers));
                smsIntent.putExtra("sms_body", msg);
                startActivity(smsIntent);
            }

        }

        // If 'send mail' is selected direct to mail application with participants mail addresses
        // and meeting details
        if(item.getTitle().equals("Send Mail")){
            // Getting mailAddresses of participants
            String mailAddresses = database.getParticipantsMailAddresses(personID, events.get(position).getId());
            if(!mailAddresses.equals("")) {
                String msg = "We have meeting information given below:\n\tLocation: " + events.get(position).getName() +
                        "\n\tAt: " + events.get(position).getDate();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "We have meeting");
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                intent.setData(Uri.parse("mailto:" + mailAddresses));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        }

        // If detail is selected creates a popup and summarize the meeting and ask user to sure or not
        if(item.getTitle().equals("Cancel Meeting")){
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Cancel Meeting");
            final Meeting current = events.get(position);
            String msg = "Current Meeting is:\n\tName: " + current.getName() + "\n\tDate: " + current.getDate() +
                    "\n\tNote: " + current.getNote() + "\nAre you sure to cancel meeting that information's given above?";
            alert.setMessage(msg);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // After taking id delete meeting object
                    database.cancelMeeting(current.getId());
                    // Then return a successful message
                    final AlertDialog.Builder alert = new AlertDialog.Builder(EventsActivity.this);
                    alert.setMessage("Your meeting canceled successfully.");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(EventsActivity.this, EventsActivity.class);
                            intent.putExtra("personID", personID);
                            intent.putExtra("personName", personName);
                            startActivity(intent);
                            finish();
                        }
                    });

                    alert.create();
                    alert.show();
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.create();
            alert.show();
        }
        return true;
    }

    // Toggle the side bar burger
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Side bar menu item selection
    // In order to visit activities from sidebar
    public void selectIterDrawer(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                Intent i = new Intent(EventsActivity.this, HomeActivity.class);
                i.putExtra("personName", personName);
                i.putExtra("personID", personID);
                startActivity(i);
                finish();
                break;
            case R.id.nav_friends:
                Intent i_friends = new Intent(EventsActivity.this, FriendsActivity.class);
                i_friends.putExtra("personName", personName);
                i_friends.putExtra("personID", personID);
                startActivity(i_friends);
                finish();
                break;
            case R.id.nav_logout:
                Intent i_logut = new Intent(EventsActivity.this, LoginActivity.class);
                startActivity(i_logut);
                finish();
                break;
        }
    }
    // Setting the item listener to side bar
    public void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectIterDrawer(item);
                return true;
            }
        });
    }
}
