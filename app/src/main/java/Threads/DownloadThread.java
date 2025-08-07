package Threads;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.util.Vector;

import OTAUpdate.OTADownloadManager.OTADownloadManager;
import OTAUpdate.OTADownloadManager.OTADownloadMethod;
import OTAUpdate.OTAStatusCtrl.OTAStatusCtrl;
import OTAUpdate.OTAVar.OTAVar;
import OTAUpdate.OTAVar.OTAVarDefine;

public class DownloadThread {

    private Context sContext;
    private OTADownloadManager mOTADownloadManager;

    public DownloadThread(Context mContext) {
        sContext = mContext;
        mOTADownloadManager = new OTADownloadManager(sContext);
        OTAVar.DownloadFileInfoVec = new Vector<>();
    }

    public void Start() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d(TAGS, "DownloadThread Thread Start");
                while (true) {

                    int rtn = 0;

                    if (OTAStatusCtrl.OTA_STATUS_GET().equals(OTAVarDefine.OTA_STATUS_DOWNLOAD) == true) {
                        do {
                            Log.d(TAGS, "Start to Download");
                            rtn = mOTADownloadManager.DownloadInit();
                            if (rtn != 0) {
                                Log.d(TAGS, "DownloadInit Failed");
                                mOTADownloadManager.DownloadFailed();
                                break;
                            }

                            rtn = mOTADownloadManager.DownloadStart();
                            if(rtn == 1){
                                Log.d(TAGS, "Download Cancel");
                            }
                            else if (rtn < 0) {
                                Log.d(TAGS, "DownloadStart Failed");
                                mOTADownloadManager.DownloadFailed();
                                break;
                            } else {
                                Log.d(TAGS, "Download Finish");
                                mOTADownloadManager.DownloadFinish();
                            }
                        } while (false);
                    }
                    Sleep(1000);
                }
            }
        });

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        } catch (Exception e) {

        }
    }

    static String TAGS = "## [KO] DownloadThread";
}
