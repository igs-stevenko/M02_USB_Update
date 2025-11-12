package com.igs.usb_update;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import EnvVar.EnvVar;
import OTAUpdate.BroadCast.BroadCastReceiver;
import Threads.ProgressThread;
import Threads.TextUpdateThread;
import ViewCtrl.ViewCtrl;
import Threads.UpdateThread;

public class MainActivity extends AppCompatActivity {
    public  TextView VersionText;
    private void init(){

        /* 動態表現 */
        ViewCtrl.progressBar = findViewById(R.id.progressBar);
        ViewCtrl.ProgressText = (TextView) findViewById(R.id.IGSText);
        ViewCtrl.TitleText = (TextView) findViewById(R.id.Title);
        ViewCtrl.PromptText = (TextView) findViewById(R.id.PromptText);

        /* 靜態表現 */
        VersionText = (TextView) findViewById(R.id.VersionView);

        BroadCastReceiver.RegisterBroadcastReciver(this);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();;
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        init();

        VersionText.setText("V2.0");

        ViewCtrl.ProgressShow();
        new UpdateThread(this).Start();
        new ProgressThread(this).Start();
        new TextUpdateThread(this).Start();

    }

    static String TAGS = "## [KO] MainActivity";
}