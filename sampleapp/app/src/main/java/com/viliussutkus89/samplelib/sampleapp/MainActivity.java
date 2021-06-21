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

    TextView appVersion = findViewById(R.id.appVersion);
    appVersion.setText("Code=" + BuildConfig.VERSION_CODE + ", name=" + BuildConfig.VERSION_NAME);

    TextView appGitCommit = findViewById(R.id.appGitCommit);
    appGitCommit.setText(R.string.APP_GIT_COMMMIT);

    TextView libVersion = findViewById(R.id.libVersion);
    libVersion.setText(VersionGetter.getVersion());

    TextView libGitCommit = findViewById(R.id.libGitCommit);
    libGitCommit.setText(VersionGetter.getGitCommit());
  }

}
