package com.example.ozgurozdemir.meetpoint;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

public class LoginActivity extends AppCompatActivity {

    // variable initialize
    private Button login;
    private TextView register;
    private EditText loginUsername, loginPassword;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initializing the database variable
        File myDB = getApplication().getFilesDir();
        final String path = myDB +  "/" + "MeetPointDB";
        database = new Database(path);

        // Input initialize
        loginUsername = (EditText) findViewById(R.id.loginUsername);
        loginPassword = (EditText) findViewById(R.id.loginPassword);

        // Register Session
        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Creating a register pop up with popup_register layout
                final Dialog popUp = new Dialog(LoginActivity.this);
                popUp.setContentView(R.layout.popup_register);

                // Input initialize at popup
                final TextView registerUsername = (EditText) popUp.findViewById(R.id.registerUsername);
                final TextView registerPassword = (EditText) popUp.findViewById(R.id.registerPassword);
                final TextView registerName = (EditText) popUp.findViewById(R.id.registerName);
                final TextView registerMail = (EditText) popUp.findViewById(R.id.registerMail);
                final TextView registerPhone = (EditText) popUp.findViewById(R.id.registerTelephone);
                final TextView officePhone = (EditText) popUp.findViewById(R.id.registerOffice);

                Button registerRegister = (Button) popUp.findViewById(R.id.registerRegister);
                Button registerCancel = (Button) popUp.findViewById(R.id.registerCancel);

                // Popup dismiss button
                registerCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp.dismiss();
                    }
                });

                // Registration button
                registerRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // User should fill all inputs in order to create an account
                        // If user don't fill an input give an error message
                        if (registerUsername.getText().toString().isEmpty() || registerPassword.getText().toString().isEmpty() ||
                                registerName.getText().toString().isEmpty() || registerMail.getText().toString().isEmpty() ||
                                registerPhone.getText().toString().isEmpty()) {

                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                            alertDialogBuilder.setMessage("Please check your inputs..");
                            alertDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        }

                        else if(!registerMail.getText().toString().contains("@")){
                            System.out.println(officePhone.getText().toString());
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                            alertDialogBuilder.setMessage("Please check your email address..");
                            alertDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        }

                        else {

                            // Otherwise it goes to database class and call register method
                            // That method return either 1 or -1 (explained at Database class)
                            int register = database.registerDB(registerUsername.getText().toString(), registerPassword.getText().toString(),
                               registerName.getText().toString(), registerMail.getText().toString(), Double.valueOf(registerPhone.getText().toString()),
                                    Double.valueOf(officePhone.getText().toString()));

                            // If method return 1 that means you created account and give successful message
                            if (register == 1) {
                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                                alertDialogBuilder.setMessage("You registered successfully.");

                                alertDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        popUp.dismiss();
                                    }
                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            } else {

                                // If method return -1 that means username want to taken and give an error message
                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                                alertDialogBuilder.setMessage("Username is already taken.");

                                alertDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }
                    }
                });
                popUp.show();
            }
        });

        // Login button
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // In order to login, it goes database again and calls loginDB method
                // This method return an array that contains user id and name
                String[] loginInformation = database.loginDB(loginUsername.getText().toString(), loginPassword.getText().toString());

                // If array contains two empty string that means there is not an account such that name
                // and gives an error message
                if(loginUsername.getText().toString().isEmpty() || loginPassword.getText().toString().isEmpty() ||
                        loginInformation[0].equals("") ){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                    alertDialogBuilder.setMessage("Please check your inputs..");

                    alertDialogBuilder.setNegativeButton("OK",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loginUsername.setText("");
                            loginPassword.setText("");
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {

                    // Otherwise start Home activity with intent contains id and name related to loginInformation
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    i.putExtra("personID", loginInformation[0]);
                    i.putExtra("personName", loginInformation[1]);
                    startActivity(i);
                    finish();
                }
            }
        });

        // Hiding the title bar
        getSupportActionBar().hide();

    }
}
