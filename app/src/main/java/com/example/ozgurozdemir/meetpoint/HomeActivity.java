package com.example.ozgurozdemir.meetpoint;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    // variable initialize
    private DrawerLayout nDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigation;

    private Button eventLocationBtn, eventHourBtn, eventDateBtn, eventFriendBtn, eventScheduleBtn,
            eventNoteBtn, logoutBtn;

    private ArrayList<String> participants = new ArrayList<String>();

    private String personName, personID, hour, date, note, location, locationLatLng;

    private TextView homeGreetingText, upcomingEventInfoTxt;

    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("Home");

        // Initializing the database variable
        File myDB = getApplication().getFilesDir();
        final String path = myDB + "/" + "MeetPointDB";
        database = new Database(path);

        // Initializing the intent
        final Intent intent = getIntent();
        personName = intent.getStringExtra("personName");
        personID = intent.getStringExtra("personID");

        // If location is selected and returned to Home
        // getting the extras in order to setting up buttons
        if(intent.getStringExtra("locationSelected") != null) {
            if (intent.getStringExtra("locationSelected").equals("true")) {
                hour = intent.getStringExtra("hour");
                date = intent.getStringExtra("date");
                note = intent.getStringExtra("note");
                location = intent.getStringExtra("location");
                participants = intent.getStringArrayListExtra("participants");
            }
        }
        locationLatLng = intent.getStringExtra("locationLatLng");

        // Greeting Text
        homeGreetingText = (TextView) findViewById(R.id.homeGreetingTxt);
        homeGreetingText.setText("Welcome " + personName);

        // Screening the upcoming event
        // To do so, it uses getUpcomingEvent method explained at Database class
        String upcoming = database.getUpcomingEvent(personID);
        upcomingEventInfoTxt = (TextView) findViewById(R.id.upcomingEventInfoTxt);
        if (!upcoming.equals(""))
            upcomingEventInfoTxt.setText(upcoming);

        // Side Bar Setup
        // Initialize DrawerLayout variable
        nDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, nDrawerLayout, R.string.open, R.string.close);
        nDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigation = (NavigationView) findViewById(R.id.navigation);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(navigation);

        // Logout Button
        logoutBtn = (Button) findViewById(R.id.logoutBtn);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        // Input buttons in order to schedule a meeting
        // Location btn
        // It opens a map activity to get meeting location
        // It keeps the current button information, because after user selected location, it returns
        // to Home Activity with same inputs
        eventLocationBtn = (Button) findViewById(R.id.eventLocationBtn);
        eventLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, MapActivity.class);
                i.putExtra("personID", personID);
                i.putExtra("personName", personName);
                i.putExtra("hour",eventHourBtn.getText().toString());
                i.putExtra("date", eventDateBtn.getText().toString());
                i.putExtra("note",note);
                i.putExtra("location", location);
                i.putExtra("locationLatLng",locationLatLng);
                i.putExtra("participants", participants);
                startActivity(i);
                finish();

            }
        });

        // Hour btn that use TimePickerDialog to get hour of meeting
        eventHourBtn = (Button) findViewById(R.id.eventHourBtn);

        // Create a calendar object and get current time in order to give an hour estimation and
        // restrict the date (past days of month and year)
        final Calendar c = Calendar.getInstance();
        eventHourBtn.setText((c.get(Calendar.HOUR_OF_DAY) + 1) + ":00");
        eventHourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(HomeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if (selectedMinute < 10)
                            eventHourBtn.setText(selectedHour + ":0" + selectedMinute);
                        else
                            eventHourBtn.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });


        // Date btn that use DatePickerDialog to get date of the meeting
        eventDateBtn = (Button) findViewById(R.id.eventDateBtn);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        eventDateBtn.setText(day + "/" + month + "/" + year);
        eventDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        HomeActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        eventDateBtn.setText(i2 + "/" + (i1 + 1) + "/" + i);
                    }
                }, year, month, day);

                // Restrict the past days
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        // Friend btn that use custom popup to get participants of the meeting
        // It creates a popup with list view with custom adapter
        eventFriendBtn = (Button) findViewById(R.id.eventFriendBtn);
        eventFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog popUp = new Dialog(HomeActivity.this);
                popUp.setContentView(R.layout.popup_participants);

                ListView participantsList = (ListView) popUp.findViewById(R.id.participantsList);

                // Getting all friends from database for possible participants
                final ArrayList<String> friends = database.getFriends(personID);
                final CustomAdapter<String> friendAdapter = new CustomAdapter<String>(HomeActivity.this, friends, "friends");
                friendAdapter.participants = participants;
                participantsList.setAdapter(friendAdapter);

                participantsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        // If friends is not selected, select friend and change background color to tell user
                        // that friend is selected
                        if (!participants.contains(friends.get(position))) {
                            participants.add(friends.get(position));
                            view.setBackgroundResource(R.color.colorPrimary);
                            ((TextView) view.findViewById(R.id.eventListHourTxt)).setTextColor(Color.WHITE);
                        } else {

                            // Otherwise, unselect friend and change background color to tell user that
                            // friend is not selected
                            participants.remove(friends.get(position));
                            view.setBackgroundResource(R.color.background);
                            ((TextView) view.findViewById(R.id.eventListHourTxt)).setTextColor(Color.GRAY);
                        }
                    }
                });

                // After participants tagged return Home activity and change button name
                Button okParticipantsBtn = popUp.findViewById(R.id.okParticipantsBtn);
                okParticipantsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (participants.size() > 0) {
                            eventFriendBtn.setText(participants.size() + " friends tagged.");
                        } else {
                            eventFriendBtn.setText("No friends tagged.");
                        }
                        popUp.dismiss();
                    }
                });

                popUp.show();
            }
        });

        // Note btn that use custom popup to get note of the meeting if there is one
        eventNoteBtn = (Button) findViewById(R.id.eventNoteBtn);
        eventNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog popUp = new Dialog(HomeActivity.this);
                popUp.setContentView(R.layout.popup_note);
                final EditText noteTxt = popUp.findViewById(R.id.noteTxt);

                // If note is already written but user want to see or change it, then
                // puts the note given before into txt
                if(note != null) {
                    if (!note.isEmpty()) {
                        noteTxt.setText(note);
                    }
                }

                // After the process return Home activity and change button name
                Button okNoteBtn = popUp.findViewById(R.id.okNoteBtn);
                okNoteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        note = noteTxt.getText().toString();
                        if (!note.isEmpty()) {
                            eventNoteBtn.setText("Given Note");
                        } else {
                            eventNoteBtn.setText("Without Note");
                        }
                        popUp.dismiss();
                    }
                });

                popUp.show();
            }
        });

        // Schedule btn for scheduling a meeting
        eventScheduleBtn = (Button) findViewById(R.id.eventScheduleBtn);
        eventScheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // A location must be selected otherwise it returns an error
                if (!eventLocationBtn.getText().toString().equals("Not Selected")) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);

                    // Create a popup that summarize the meeting wants to schedule and ask to user
                    // If the information are correct or not
                    alert.setTitle("Schedule a Meeting");
                    String msg = "Your Meeting is:\n\tLocation: " + eventLocationBtn.getText().toString() + "\n\tHour: " + eventHourBtn.getText().toString() +
                            "\n\tDate: " + eventDateBtn.getText().toString() + "\n\tParticipants: " + eventFriendBtn.getText().toString() +
                            "\n\tNote: " + eventNoteBtn.getText().toString() + "\nAre you sure to schedule meeting that information's given above?";
                    alert.setMessage(msg);
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // If information are correct create a Meeting object to database and give
                            // message that meeting scheduled successfully
                            database.scheduleMeeting(personID, eventLocationBtn.getText().toString(), "Levent", eventHourBtn.getText().toString(),
                                    eventDateBtn.getText().toString(), participants, note);

                            final AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                            alert.setMessage("Your meeting scheduled successfully.");
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
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
                } else {

                    // Error message for not location selected
                    final AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                    alert.setMessage("Please select a location to meet.");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    alert.create();
                    alert.show();
                }
            }
        });

        // Setting up buttons if user returns from map to home and have got some inputs
        if(location != null){
            eventLocationBtn.setText(location);
        }
        if(hour != null){
            eventHourBtn.setText(hour);
        }
        if(date != null){
            eventDateBtn.setText(date);
        }
        if(participants != null){
            eventFriendBtn.setText(participants.size() + " friends tagged.");
        }
        if(note != null){
            eventNoteBtn.setText("Given Note");
        }

    }

    // Toggle the side bar burger
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Side bar menu item selection
    // In order to visit activities from sidebar
    public void selectIterDrawer(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_events:
                Intent i = new Intent(HomeActivity.this, EventsActivity.class);
                i.putExtra("personID", personID);
                i.putExtra("personName", personName);
                startActivity(i);
                finish();
                break;
            case R.id.nav_friends:
                Intent i_friends = new Intent(HomeActivity.this, FriendsActivity.class);
                i_friends.putExtra("personName", personName);
                i_friends.putExtra("personID", personID);
                startActivity(i_friends);
                finish();
                break;
            case R.id.nav_logout:
                Intent i_logut = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(i_logut);
                finish();
                break;
        }

    }
    // Setting the item listener to side bar
    public void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectIterDrawer(item);
                return true;
            }
        });
    }

}
