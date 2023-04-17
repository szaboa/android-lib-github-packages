package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.examplelib.ExampleLib;
import com.example.examplelib.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExampleLib exampleLib = new ExampleLib();
        exampleLib.setup();
    }
}