package Threads;

import android.content.Context;


import OTAUpdate.OTAThreadMethod.OTAUpdateThreadMethod;
import UsbUpdate.UsbThreadMethod.UsbThreadMethod;
import EnvVar.EnvVar;
public class UpdateThread {
    private Context sContext;
    private UsbThreadMethod mUsbThreadMethod;
    private OTAUpdateThreadMethod mOTAThreadMethod;

    public UpdateThread(Context mContext) {
        sContext = mContext;
        mUsbThreadMethod = new UsbThreadMethod(sContext);
        mOTAThreadMethod = new OTAUpdateThreadMethod(sContext);
    }


    public void Start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (EnvVar.UpdateConfig == EnvVar.UPDATE_FROM_USB) {
                    mUsbThreadMethod.USB_Update();
                } else if (EnvVar.UpdateConfig == EnvVar.UPDATE_FROM_OTA) {
                    mOTAThreadMethod.OTA_Update();
                }
            }
        }).start();
    }

    static String TAGS = "## [KO] UpdateThread";
}
