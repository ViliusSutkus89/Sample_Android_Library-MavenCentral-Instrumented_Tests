package com.viliussutkus89.samplelib.sampleapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.viliussutkus89.samplelib.VersionGetter;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ((TextView)findViewById(R.id.appVersionName)).setText(BuildConfig.VERSION_NAME);
    ((TextView)findViewById(R.id.appVersionCode)).setText(String.valueOf(BuildConfig.VERSION_CODE));
    ((TextView)findViewById(R.id.appGitCommit)).setText(R.string.GIT_COMMMIT);

    ((TextView)findViewById(R.id.libVersionName)).setText(VersionGetter.getVersionName());
    ((TextView)findViewById(R.id.libVersionCode)).setText(String.valueOf(VersionGetter.getVersionCode()));
    ((TextView)findViewById(R.id.libGitCommit)).setText(VersionGetter.getGitCommit());
  }

}
