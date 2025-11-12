package com.igs.usb_update;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import OTAUpdate.BroadCast.BroadCastReceiver;
import Threads.DownloadThread;
import UsbUpdate.UsbUpdateStatusCtrl.UsbUpdateStatusCtrl;
import UsbUpdate.UsbVarDefine.UsbVar;
import UsbUpdate.UsbVarDefine.UsbVarDefine;
import model.ApkControl.ApkControl;
import model.AppDetail.AppDetail;
import model.FileControl.FileControl;
import OTAUpdate.OTAStatusCtrl.OTAStatusCtrl;
import UsbUpdate.UsbCheck.USBCheck;
import EnvVar.EnvVar;


public class FirstStartActivity extends AppCompatActivity {

    private boolean FirstStarted = true;
    private ApkControl mApkControl;
    private List<AppDetail> apps = null;
    private ListView list;
    private PackageManager manager;
    public native String GeySysInfo();

    static {
        System.loadLibrary("usb_update");
    }

    private void init(){

        mApkControl = new ApkControl(this);
        BroadCastReceiver.RegisterBroadcastReciver(this);
        Log.d(TAGS, "init : ");
        EnvVar.PROJECT_NAME = GeySysInfo();
        Log.d(TAGS, "[init] : SYSTEM PROJECT_NAME = " + EnvVar.PROJECT_NAME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        View decorView = getWindow().getDecorView();;
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        /* 初始化 */
        init();
        android.os.Process.setThreadPriority(android.os.Process.myPid(), -19);
        Log.d(TAGS, "USB_Update : V7");
    }

    @Override
    protected void onResume() {
        super.onResume();
        BlockWaiting();

        if(IsPowerError() == true) {
            Log.d(TAGS, "Power Error");
            StartPowerErrorView();
            return;
        }


        if(FirstStarted == true){
            Log.d(TAGS, "FirstStarted : ");
            FirstStarted = false;

            if (USBCheck.NeedUpdate() == true) {
                Log.d(TAGS, "USB NeedUpdate : ");
                EnvVar.UpdateConfig = EnvVar.UPDATE_FROM_USB;
                StartUpdateView();
            } else {

                if(UsbUpdateStatusCtrl.UsbUpdate_FileExist() == true){
                    /*【防呆檢查】: FOTA檔案存在，代表上次未更新完成，要強制在進行更新，否則不開啟遊戲*/
                    StartUpdateUndoneView();
                }
                else {

                    if (OTAStatusCtrl.NeedUpdate() == true) {
                        Log.d(TAGS, "OTACheck NeedUpdate : ");
                        EnvVar.UpdateConfig = EnvVar.UPDATE_FROM_OTA;
                        StartUpdateView();
                    } else {
                        NormalStart();
                    }
                }
            }
        }
        else{
            StartGame();
        }
    }

    private void NormalStart() {

        StartDownloadThread();

        EnvVar.PRODUCT_TYPE = SystemProperties.get("ro.project.env");
        if (EnvVar.PRODUCT_TYPE.equals("REL")) {
            EnvVar.GAME_PACKAGE_NAME = SystemProperties.get("ro.igs.game");
            StartGame();
        } else {
            if (FileControl.IsFileExist(EnvVar.FOR_LAUNCHER_PATH) == true) {
                EnvVar.GAME_PACKAGE_NAME = FileControl.ReadStringFromFile(EnvVar.FOR_LAUNCHER_PATH);
                StartGame();
            } else {
                Log.d(TAGS, "loadApps : ");
                loadApps();
                loadListView();
                addClickListener();
            }
        }
    }

    private void StartDownloadThread(){
        new DownloadThread(this).Start();
    }

    private void StartPowerErrorView(){
        Log.d(TAGS, "Start PowerError Activity");
        Intent intent = new Intent(this, PowerErrorActivity.class);
        this.startActivity(intent);
    }

    private void StartUpdateUndoneView(){
        Log.d(TAGS, "Start UpdateUndone Activity");
        Intent intent = new Intent(this, UpdateUndone.class);
        this.startActivity(intent);
    }

    private void StartGame(){
        Log.d(TAGS, "Start Game");
        PackageManager manager = this.getPackageManager();
        Intent it = manager.getLaunchIntentForPackage(EnvVar.GAME_PACKAGE_NAME);
        while(mApkControl.isAppInstalled(EnvVar.GAME_PACKAGE_NAME) == false){
            Sleep(16);
        }
        this.startActivity(it);
    }

    private void StartUpdateView(){
        Log.d(TAGS, "");
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }

    private void loadApps() {
        manager = getPackageManager();
        apps = new ArrayList<AppDetail>();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            apps.add(app);
        }
    }


    private void loadListView() {

        list = (ListView) findViewById(R.id.apps_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this, R.layout.list_item, apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_app_icon);
                appIcon.setImageBitmap(drawableToBitmap(apps.get(position).icon));

                TextView appLabel = (TextView) convertView.findViewById(R.id.item_app_label);
                appLabel.setText(apps.get(position).label);

                TextView appName = (TextView) convertView.findViewById(R.id.item_app_name);
                appName.setText(apps.get(position).name);

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;

    }

    private void addClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                Intent i = manager.getLaunchIntentForPackage(apps.get(pos).name.toString());
                EnvVar.GAME_PACKAGE_NAME = apps.get(pos).name.toString();
                Log.d(TAGS, "EnvVar.GAME_PACKAGE_NAME : " + EnvVar.GAME_PACKAGE_NAME);
                WriteFile(EnvVar.GAME_PACKAGE_NAME);
                FirstStartActivity.this.startActivity(i);
            }
        });
    }
    private void WriteFile(String text) {
        File myFile = new File(EnvVar.FOR_LAUNCHER_PATH);
        try {
            FileWriter over = new FileWriter(myFile, false);
            over.write(text);
            over.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        }catch(Exception e) {

        }
    }

    private boolean IsPowerError(){
        //Log.d(TAGS, "IsPowerError()");
        boolean PowerErr = true;
        InputStream inputStream = null;
        int bytesRead = 0;
        byte[] buffer = new byte[2];
        try {
            // 建立 InputStream 物件來讀取檔案
            inputStream = new FileInputStream("/dev/pwrd");

            // 讀取兩個位元組
            bytesRead = inputStream.read(buffer);

        } catch (IOException e) {
            Log.d(TAGS, "Read IOException");
            e.printStackTrace();
            return false;

        } finally {

            if (bytesRead == 2) {
                // 處理讀取的位元組
                if(buffer[0] == '0') {
                    //Log.d(TAGS, "buff[0] => 0");
                    PowerErr = false;
                }
                else{
                    //Log.d(TAGS, "buff[0] => 1");
                    PowerErr = true;
                }
            } else {
                Log.d(TAGS, "Read /dev/pwrd failed");
                PowerErr = true;
            }

            try {
                // 關閉 InputStream
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                PowerErr = true;
            }
        }

        return PowerErr;
    }

    public boolean GetBootComplete() {
        return SystemProperties.getInt("sys.boot_completed", 0) == 1 ? true : false;
    }

    public void BlockWaiting() {
        while(!GetBootComplete()) {
            Sleep(16);
        }
    }



    protected void onDestroy() {
        super.onDestroy();
        BroadCastReceiver.UnRegisterBroadcastReciver();
    }

    static String TAGS = "## [KO] FirstStartActivity";
}
