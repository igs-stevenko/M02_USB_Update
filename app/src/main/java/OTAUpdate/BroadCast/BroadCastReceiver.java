package OTAUpdate.BroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONException;

import EnvVar.EnvVar;
import OTAUpdate.OTADownloadManager.OTADownloadMethod;
import OTAUpdate.OTAStatusCtrl.OTAStatusCtrl;
import OTAUpdate.OTAVar.OTAVar;
import OTAUpdate.OTAVar.OTAVarDefine;

public class BroadCastReceiver {

    private static Context sContext;

    public static void RegisterBroadcastReciver(Context mContext) {
        sContext = mContext;
        sContext.registerReceiver(ReceiveOTASevice, new IntentFilter("OTA_AB_Update"));
        sContext.registerReceiver(ReceiveGameCommand, new IntentFilter("OTA_RECV_FROM_GAME"));
    }

    private static BroadcastReceiver ReceiveOTASevice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle extras = intent.getExtras();
            Log.d(TAGS, "Wait  OTA_AB_Update BroadCast");
            EnvVar.SYSTEM_UPDATE_COMPLETE = true;
            EnvVar.SYSTEM_UPDATE_SUCCESS = extras.getBoolean("OTA_ab_update_confirm");
            Log.d(TAGS, "OTA_ab_update_confirm = " + EnvVar.SYSTEM_UPDATE_SUCCESS);
        }
    };

    private static BroadcastReceiver ReceiveGameCommand = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int cmd = 0;
            Bundle extras = intent.getExtras();
            Log.d(TAGS, "Wait Game Command");
            cmd = extras.getInt("Command");
            switch (cmd) {
                case OTAVarDefine.CMD_RECV_GAME_UPDATE:
                    Log.d(TAGS, "CMD_RECV_GAME_UPDATE");
                    OTAStatusCtrl.OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_UPDATE);
                    Sleep(2000);
                    rebootDevice();
                    break;

                case OTAVarDefine.CMD_RECV_GAME_DOWNLOAD:
                    Log.d(TAGS, "CMD_RECV_GAME_DOWNLOAD");
                    if(OTAStatusCtrl.OTA_STATUS_GET().equals(OTAVarDefine.OTA_STATUS_DOWNLOAD) == true){
                        Log.d(TAGS, "It's Download Status Now");
                        break;
                    }
                    String DownloadFileInfo = extras.getString("DownloadInfo");
                    Log.d(TAGS, "DownloadFileInfo = " + DownloadFileInfo);
                    OTADownloadMethod.WriteFileInfoToFile(DownloadFileInfo);
                    OTAStatusCtrl.OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_DOWNLOAD);
                    break;

                case OTAVarDefine.CMD_RECV_GAME_RESET:
                    Log.d(TAGS, "CMD_RECV_GAME_RESET");
                    OTAStatusCtrl.OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_NORMAL);
                    Sleep(1000);
                    OTADownloadMethod.RemoveAllFilesInfoVec();
                    OTADownloadMethod.RemoveAllFilesInfoFiles();
                    break;

                case OTAVarDefine.CMD_RECV_GAME_SETTING:
                    Log.d(TAGS, "CMD_RECV_GAME_SETTING");
                    OTAVar.PerSleepMs = intent.getIntExtra("PerSleepMs", 1);
                    OTAVar.PerSleepBytes = intent.getIntExtra("PerSleepBytes", 8912);
                    Log.d(TAGS, "PerSleepMs :" + OTAVar.PerSleepMs);
                    Log.d(TAGS, "PerSleepMs :" + OTAVar.PerSleepBytes);
                    break;

                default:
                    break;
            }
        }
    };

    public static void UnRegisterBroadcastReciver() {
        sContext.unregisterReceiver(ReceiveOTASevice);
        sContext.unregisterReceiver(ReceiveGameCommand);
    }

    private static void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        } catch (Exception e) {

        }
    }

    private static void rebootDevice() {
        Log.d(TAGS, "rebootDevice");
        PowerManager pm = (PowerManager) sContext.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            Log.d(TAGS, "reboot");
            pm.reboot(null);
        }
    }

    static String TAGS = "## [KO] BroadCastReceiver";
}
