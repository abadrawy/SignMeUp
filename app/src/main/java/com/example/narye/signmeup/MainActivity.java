package com.example.narye.signmeup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mBtn1 = (Button) findViewById(R.id.mainbtn);
        mBtn1.setOnClickListener(this);
    }


    public void onClick(View v) {
        Log.i("clicks","You Clicked!");
        Intent i=new Intent(MainActivity.this, Activity2.class);
        startActivity(i);
    }
}
