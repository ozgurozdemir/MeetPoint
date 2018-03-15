package com.example.ozgurozdemir.meetpoint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    // variable initialize
    private ListView myFriendsList, peopleList;
    private ArrayList<String> people, friends;
    private EditText searchPeople, searchMyFriend;

    private String personName, personID;

    private DrawerLayout nDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigation;

    private Database database;

    private String context = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        getSupportActionBar().setTitle("Friends");

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

        // Friends list setup
        myFriendsList = (ListView) findViewById(R.id.myFriendsList);
        friends = database.getFriends(personID);
        final CustomAdapter<String> friendAdapter = new CustomAdapter<String>(this, friends, "people");
        myFriendsList.setAdapter(friendAdapter);
        registerForContextMenu(myFriendsList);

        // Friends list search setup
        searchMyFriend = (EditText) findViewById(R.id.searchMyFriend);
        searchMyFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtering the result with respect to given text
                friendAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // People list that possible friends for adding setup
        peopleList = (ListView) findViewById(R.id.peopleList);
        people = database.getPeople(personID);
        final CustomAdapter<String> peopleAdapter = new CustomAdapter<String>(this, people, "people");
        peopleList.setAdapter(peopleAdapter);
        registerForContextMenu(peopleList);

        // People list search setup
        searchPeople = (EditText) findViewById(R.id.searchPeople);
        searchPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtering the result with respect to given text
                peopleAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Making more visible when using people search and keyboard
        // (it kinds of overlapping, so should be avoided for decent view)
        searchPeople.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    myFriendsList.setVisibility(ListView.GONE);
                } else {
                    myFriendsList.setVisibility(ListView.VISIBLE);
                }
            }
        });

    }

    // Context Menus for ListViews
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // Specialized menu for friends list
        if (v.getId() == R.id.myFriendsList) {
            menu.add("Detail");
            menu.add("Delete");
            // Setting a context variable to avoiding confliction of people list context menu detail item
            context = "myFriend";
        }
        // Specialized menu for people list
        if (v.getId() == R.id.peopleList) {
            menu.add("Detail");
            menu.add("Add to Friend");
            // Setting a context variable to avoiding confliction of friends list context menu detail item
            context = "people";
        }
    }

    // Context Menu Item Selected Methods
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position = menuInfo.position;

        // If detail is selected creates a popup and view the details of either friend or person
        if(item.getTitle().equals("Detail")){
            final Dialog popUp = new Dialog(this);
            popUp.setContentView(R.layout.popup_friend);
            // Lookup view for data population
            TextView friendNameTxt = popUp.findViewById(R.id.friendNameTxt);
            TextView friendMailTxt = popUp.findViewById(R.id.friendMailTxt);
            TextView friendPhoneTxt = popUp.findViewById(R.id.friendPhoneTxt);
            TextView friendOfficeTxt = popUp.findViewById(R.id.friendOffice);
            Person friend;

            // If context menu item created by friends list person calling from database with getFriend method
            if (context.equals("myFriend")) {
                String[] current = friends.get(position).split("-");
                friend = database.getFriend(personID, current[1]);
            } else {
                // Otherwise person calling from database with getPerson method
                String[] current = people.get(position).split("-");
                friend = database.getPerson(current[1]);
            }
            friendNameTxt.setText(friend.getName());
            friendMailTxt.setText(friend.getMail());
            friendPhoneTxt.setText(friend.getPhone());
            friendOfficeTxt.setText(friend.getOffice());

            // Detail view button initializer
            if(context.equals("myFriend")){
                // If detailed person chosen from friends list you can delete it from that view with
                // delete button
                Button deleteFriendBtn = popUp.findViewById(R.id.deleteFriendBtn);
                deleteFriendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                        alert.setTitle("Delete Friend");
                        String msg = "Are you sure to delete friend that information's given above?";
                        alert.setMessage(msg);

                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // It uses personID and personName in one string, so it should split
                                // the string and take id
                                String[] current = friends.get(position).split("-");
                                // After taking id delete friendship relation with user and selected friend
                                database.deleteFriend(personID, current[1]);
                                // Then return a successful message
                                final AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                                alert.setMessage("Your friend deleted successfully.");
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(FriendsActivity.this, FriendsActivity.class);
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
            } else {
                // If detailed person chosen from people list you can add it from that view with
                // add button
                Button addFriendBtn = popUp.findViewById(R.id.deleteFriendBtn);
                addFriendBtn.setText("ADD");
                addFriendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                        alert.setTitle("Add Friend");
                        String msg = "Are you sure to add friend that information's given above?";
                        alert.setMessage(msg);

                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // It uses personID and personName in one string, so it should split
                                // the string and take id
                                String[] current = people.get(position).split("-");
                                // After taking id add friendship relation with user and selected friend
                                database.addFriend(personID, current[1]);
                                // Then return a successful message
                                final AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                                alert.setMessage("Your friend added successfully.");
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(FriendsActivity.this, FriendsActivity.class);
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
            }

            // Popup dismiss button
            Button okFriendBtn = popUp.findViewById(R.id.okFriendBtn);
            okFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUp.dismiss();
                }
            });

            popUp.show();

        }

        // If delete is selected creates a popup and summarize the friend and ask user to sure or not
        else if(item.getTitle().equals("Delete")){
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Delete Friend");
            // It uses personID and personName in one string, so it should split
            // the string and take id
            final String[] current = friends.get(position).split("-");
            // After taking id getting friend all information to summarize
            Person friend = database.getFriend(personID, current[1]);
            String msg = "Your Friend is:\n\tName: " + friend.getName() + "\n\tMail: " + friend.getMail() +
                    "\n\tPhone Number: " + friend.getPhone() + "\nAre you sure to delete friend that information's given above?";
            alert.setMessage(msg);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // If user sure to delete that friend information given,then it deletes friendship relation with user and selected friend
                    database.deleteFriend(personID, current[1]);
                    // Then return a successful message
                    final AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                    alert.setMessage("Your friend deleted successfully.");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(FriendsActivity.this, FriendsActivity.class);
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

        // If add is selected creates a popup and summarize the person and ask user to sure or not
        else if(item.getTitle().equals("Add to Friend")){
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Add Friend");
            // It uses personID and personName in one string, so it should split
            // the string and take id
            final String[] current = people.get(position).split("-");
            // After taking id getting friend all information to summarize
            Person friend = database.getPerson(current[1]);
            String msg = "Your Friend is:\n\tName: " + friend.getName() + "\n\tMail: " + friend.getMail() +
                    "\n\tPhone Number: " + friend.getPhone() + "\nAre you sure to add friend that information's given above?";
            alert.setMessage(msg);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // If user sure to add that person information given,then it adds friendship relation with user and selected person
                    database.addFriend(personID, current[1]);
                    // Then return a successful message
                    final AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                    alert.setMessage("Your friend added successfully.");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(FriendsActivity.this, FriendsActivity.class);
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
                Intent i = new Intent(FriendsActivity.this, HomeActivity.class);
                i.putExtra("personName", personName);
                i.putExtra("personID", personID);
                startActivity(i);
                finish();
                break;
            case R.id.nav_events:
                Intent i_events = new Intent(FriendsActivity.this, EventsActivity.class);
                i_events.putExtra("personID", personID);
                i_events.putExtra("personName", personName);
                startActivity(i_events);
                finish();
                break;
            case R.id.nav_logout:
                Intent i_logut = new Intent(FriendsActivity.this, LoginActivity.class);
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
