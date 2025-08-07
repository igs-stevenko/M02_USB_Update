package Threads;

import EnvVar.EnvVar;
import android.content.Context;


import OTAUpdate.OTAThreadMethod.OTAUpdateThreadMethod;
import UsbUpdate.UsbThreadMethod.UsbThreadMethod;

public class TextUpdateThread {
    private Context sContext;
    private UsbThreadMethod mUsbThreadMethod;
    private OTAUpdateThreadMethod mOTAThreadMethod;


    public TextUpdateThread(Context mContext){

        sContext = mContext;
        mUsbThreadMethod = new UsbThreadMethod(mContext);
        mOTAThreadMethod = new OTAUpdateThreadMethod(mContext);
    }



    public void Start(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                if(EnvVar.UpdateConfig == EnvVar.UPDATE_FROM_USB) {
                    mUsbThreadMethod.USB_UpdateText();
                } else if(EnvVar.UpdateConfig == EnvVar.UPDATE_FROM_OTA){
                    mOTAThreadMethod.OTA_UpdateText();
                }
            }
        }).start();
    }
}
