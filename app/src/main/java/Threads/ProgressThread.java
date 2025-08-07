package Threads;

import android.content.Context;
import EnvVar.EnvVar;
import OTAUpdate.OTAThreadMethod.OTAUpdateThreadMethod;
import UsbUpdate.UsbThreadMethod.UsbThreadMethod;

public class ProgressThread {

    private Context sContext;
    private UsbThreadMethod mUsbThreadMethod;
    private OTAUpdateThreadMethod mOTAThreadMethod;
    public ProgressThread(Context mContext){
        sContext = mContext;
        mUsbThreadMethod = new UsbThreadMethod(sContext);
        mOTAThreadMethod = new OTAUpdateThreadMethod(sContext);
    }

    public void Start(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                if(EnvVar.UpdateConfig == EnvVar.UPDATE_FROM_USB) {
                    mUsbThreadMethod.USB_UpdateProgress();
                }
                else if(EnvVar.UpdateConfig == EnvVar.UPDATE_FROM_OTA) {
                    mOTAThreadMethod.OTA_UpdateProgress();
                }
            }
        }).start();
    }

    static String TAGS = "## [KO] ProgressThread";
}
