package com.example.ozgurozdemir.meetpoint;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ImageView logo,logoWhite, background;
    Animation animation,animationAlpha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -- Database Part --

        // Creating database file at beginning
        File myDB = getApplication().getFilesDir();
        final String path = myDB +  "/" + "MeetPointDB";
        Database db = new Database(path);
        db.createDB();

        /* Delete database in order to reset
         *   To do so, comment out following code run application
         *   then comment again that code and run application that database's reset
         */
        //File data = new File(getFilesDir(), "MeetPointDB");
        //data.delete();

        // -- Animation Part --

        // Initializing animations variables
        animation = AnimationUtils.loadAnimation(this, R.anim.anim);
        animationAlpha = AnimationUtils.loadAnimation(this, R.anim.white);

        // Initializing logo variables in order to use at animation
        logo = (ImageView) findViewById(R.id.logo);
        logoWhite = (ImageView) findViewById(R.id.logoWhite);
        background = (ImageView) findViewById(R.id.background);
        logoWhite.setAnimation(animationAlpha);
        logo.setAnimation(animation);

        // Starting new activity after animation end
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                logo.getBackground().setAlpha(0);
                logoWhite.getBackground().setAlpha(0);
                background.setBackgroundColor(Color.WHITE);
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Hiding the title bar
        getSupportActionBar().hide();
    }

}
