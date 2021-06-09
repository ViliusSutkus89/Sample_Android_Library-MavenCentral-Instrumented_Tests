package com.viliussutkus89.samplelib.sampleapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.viliussutkus89.samplelib.Hello;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Hello hello = new Hello();
    hello.getSomething();
  }

}
