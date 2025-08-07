package OTAUpdate.OTAThreadMethod;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import OTAUpdate.OTAUpdateManager.OTAUpdateManager;
import OTAUpdate.OTAVar.OTAVar;
import OTAUpdate.OTAVar.OTAVarDefine;
import ViewCtrl.ViewCtrl;

public class OTAUpdateThreadMethod {

    private Context sContext;
    private OTAUpdateManager mOTAUpdateManager;

    private void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        } catch (Exception e) {
        }
    }

    public OTAUpdateThreadMethod(Context mContext) {

        sContext = mContext;
        mOTAUpdateManager = new OTAUpdateManager(mContext);
    }

    public void OTA_UpdateProgress() {

        double TotalSpentTime = 0;
        double PerPercentStopTime = 0;
        int SleepTime = 0;

        while (OTAVar.UpdateStatus == 0) {
            Sleep(16);
        }

        Log.d(TAGS, "UpdateFileSize = " + OTAVar.GameUpdateFileSize);
        TotalSpentTime = ((OTAVar.GameUpdateFileSize) / (1024 * 1024) / 2.65);
        Log.d(TAGS, "TotalSpentTime = " + TotalSpentTime);
        PerPercentStopTime = TotalSpentTime / 50;
        Log.d(TAGS, "PerPercentStopTime = " + PerPercentStopTime);
        SleepTime = (int) (PerPercentStopTime * 1000);
        Log.d(TAGS, "SleepTime = " + SleepTime);


        int i = 0;

        while (true) {
            if (OTAVar.UpdateStatus < 0) {
                break;
            } else if (OTAVar.UpdateStatus < OTAVarDefine.DECRYPTSYSTEMFILE) {
                if (i >= 50) {
                    i = 50;
                }
                ViewCtrl.SetupProgressBar(i);
                Sleep(SleepTime);
            } else if (OTAVar.UpdateStatus < OTAVarDefine.UPDATEFINISH) {
                if (i < 50) {
                    for (; i < 50; i++) {
                        ViewCtrl.SetupProgressBar(i);
                        Sleep(16);
                    }
                } else if (i >= 99) {
                    i = 99;
                }
                ViewCtrl.SetupProgressBar(i);
                Sleep(8000);
            } else if (OTAVar.UpdateStatus == OTAVarDefine.UPDATEFINISH) {
                ViewCtrl.SetupProgressBar(100);
                break;
            }

            i++;
        }
    }

    public void OTA_UpdateText() {

        int toggle = 0;

        while (true) {

            if (OTAVar.UpdateStatus == OTAVarDefine.UPDATEFINISH) {
                Sleep(2000);
                ViewCtrl.SetupTitleTextView("Update Finish");
            } else if (OTAVar.UpdateStatus >= 0) {
                if (toggle == 0) {
                    toggle = 1;
                    ViewCtrl.SetupTitleTextView("Updating.");
                } else if (toggle == 1) {
                    toggle = 2;
                    ViewCtrl.SetupTitleTextView("Updating..");
                } else if (toggle == 2) {
                    toggle = 0;
                    ViewCtrl.SetupTitleTextView("Updating...");
                }
                ViewCtrl.SetupPromptTextView("Do not turn off this machine.");
            } else if (OTAVar.UpdateStatus == OTAVarDefine.NONE_UPDATE_FILES) {
                ViewCtrl.SetupTitleTextView("No update program has been found.");
                ViewCtrl.SetupPromptTextView("Please reboot the machine again.");
            } else {
                ViewCtrl.SetupTitleTextView("Update failed. Reboot this machine to try again. &" + OTAVar.UpdateStatus);
                ViewCtrl.SetupPromptTextView("Please contact your provider if it’s still not working.");
            }
            Sleep(1000);
        }
    }

    public void OTA_Update() {

        int rtn = 0;

        mOTAUpdateManager.OTA_Update_Init();

        rtn = mOTAUpdateManager.OTA_Update_Game();
        if (rtn < 0) {
            Log.d(TAGS, "OTA_Update_Game failed, rtn = " + rtn);
            mOTAUpdateManager.OTA_Update_Failed(rtn);
            return;
        }

        Sleep(1000);

        rtn = mOTAUpdateManager.OTA_Update_System();
        if (rtn < 0) {
            Log.d(TAGS, "OTA_Update_System failed, rtn = " + rtn);
            mOTAUpdateManager.OTA_Update_Failed(rtn);
            return;
        }

        if (OTAVar.IsSystemUpdated == true || OTAVar.IsGameUpdated == true) {
            mOTAUpdateManager.OTA_Update_Finish();
            Sleep(3000);
            if(OTAVar.IsSystemUpdated == false){
                rebootDevice();
            }
        } else {

            mOTAUpdateManager.OTA_Update_Failed(OTAVarDefine.NONE_UPDATE_FILES);
        }
    }

    private void rebootDevice() {
        Log.d(TAGS, "rebootDevice");
        PowerManager pm = (PowerManager) sContext.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            Log.d(TAGS, "reboot");
            pm.reboot(null);
        }
    }

    static String TAGS = "## [KO] OTAThreadMethod";
}
