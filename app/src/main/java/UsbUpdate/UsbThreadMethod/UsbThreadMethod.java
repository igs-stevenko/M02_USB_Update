package UsbUpdate.UsbThreadMethod;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;


import UsbUpdate.UsbUpdateManager.UsbUpdateManager;
import UsbUpdate.UsbVarDefine.UsbVar;
import UsbUpdate.UsbVarDefine.UsbVarDefine;
import ViewCtrl.ViewCtrl;

public class UsbThreadMethod {

    private Context sContext;
    private UsbUpdateManager mUsbUpdateManager;

    public UsbThreadMethod(Context mContext){
        sContext = mContext;
        mUsbUpdateManager = new UsbUpdateManager(sContext);
    }

    private void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        }catch(Exception e) {

        }
    }

    public void USB_UpdateText(){
        try {

            int toggle = 0;

            int rtn = 0;
            while (true) {

                if (UsbVar.UpdateStatus == UsbVarDefine.UPDATEFINISH) {
                    Sleep(2000);
                    ViewCtrl.SetupTitleTextView("Update Finish");
                } else if (UsbVar.UpdateStatus == UsbVarDefine.SAMEVERSION) {
                    ViewCtrl.SetupTitleTextView("Same version.");
                    ViewCtrl.SetupPromptTextView("Please make sure USB drive is unplugged, then reboot the machine again.");
                } else if (UsbVar.UpdateStatus >= 0) {
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
                    ViewCtrl.SetupPromptTextView("Do not turn off this machine or unplug the USB drive.");
                } else {
                    ViewCtrl.SetupTitleTextView("Update failed. Reboot this machine to try again. &"+ UsbVar.UpdateStatus);
                    ViewCtrl.SetupPromptTextView("Please contact your provider if it’s still not working.");
                }

                Sleep(1000);
            }
        } catch(Exception e){

        }
    }

    private void CheckAndSetupProgressBar(int i){
        if(UsbVar.UpdateStatus < 0) {
            return;
        }
        ViewCtrl.SetupProgressBar(i);
    }

    public void USB_UpdateProgress(){

        try {

            double TotalSpentTime = 0;
            double PerPercentStopTime = 0;
            int SleepTime = 0;

            while (UsbVar.UpdateStatus == 0) {
                Sleep(16);
            }

            Log.d(TAGS, "UpdateFileSize = " + UsbVar.UpdateFileSize);
            TotalSpentTime = (((UsbVar.UpdateFileSize) / (1024 * 1024) / 2.65)) + 400;
            Log.d(TAGS, "TotalSpentTime = " + TotalSpentTime);
            PerPercentStopTime = TotalSpentTime / 100;
            Log.d(TAGS, "PerPercentStopTime = " + PerPercentStopTime);
            SleepTime = (int) (PerPercentStopTime * 1000);
            Log.d(TAGS, "SleepTime = " + SleepTime);


            int i = 0;

            while (true) {
                if (UsbVar.UpdateStatus < 0) {
                    break;
                } else if (UsbVar.UpdateStatus == UsbVarDefine.UPDATEFINISH) {
                    Log.d(TAGS, "SleepTime = " + SleepTime);
                    ViewCtrl.SetupProgressBar(100);
                    Sleep(1000);
                    break;

                } else if(UsbVar.UpdateStatus == UsbVarDefine.SAMEVERSION){
                    ViewCtrl.SetupProgressBar(100);
                    Sleep(1000);
                    break;
                }  else if (UsbVar.UpdateStatus > 0) {

                    ViewCtrl.SetupProgressBar(i);
                    Sleep(SleepTime);

                }

                i++;
                if(i >= 99) i=99;
            }
        } catch(Exception e){

        }
    }


    public void USB_Update(){

        int rtn = 0;

        mUsbUpdateManager.USB_Update_Init();

        rtn = mUsbUpdateManager.USB_Update();
        if(rtn < 0){
            Log.d(TAGS, "USB_Update failed, rtn = " + rtn);
            mUsbUpdateManager.USB_Update_Failed(rtn);
        } else if(rtn == UsbVarDefine.SAMEVERSION) {
            mUsbUpdateManager.USB_Update_SAMEVERSION();
        } else {
            mUsbUpdateManager.USB_Update_Finish();
            rebootDevice();
        }
    }

    public void rebootDevice() {
        Log.d(TAGS, "rebootDevice");
        PowerManager pm = (PowerManager) sContext.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            Log.d(TAGS, "reboot");
            pm.reboot(null);
        }
    }

    static String TAGS = "## [KO] UsbThreadMethod";

}
