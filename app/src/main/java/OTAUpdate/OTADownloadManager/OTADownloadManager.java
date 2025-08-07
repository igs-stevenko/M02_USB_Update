package OTAUpdate.OTADownloadManager;

import android.content.Context;

import OTAUpdate.OTAStatusCtrl.OTAStatusCtrl;
import OTAUpdate.OTAVar.OTAVarDefine;

public class OTADownloadManager {

    private Context sContext;

    public OTADownloadManager(Context mContext) {

        sContext = mContext;
    }


    public void DownloadFinish()  {

        OTADownloadMethod.RemoveAllFilesInfoVec();
        OTADownloadMethod.RemoveAllFilesInfoFiles();
        OTADownloadMethod.SetDownloadSpeedToProp(0);
        OTAStatusCtrl.OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_DOWNLOAD_COMPLETED);
    }

    public int DownloadInit()  {

        int rtn = 0;

        String DownloadFileInfo = OTADownloadMethod.ReadFileInfoFromFile();
        if(DownloadFileInfo.equals("")){
            return -1;
        }
        OTADownloadMethod.RemoveAllFilesInfoVec();
        rtn = OTADownloadMethod.CollectFilesInfoToVec(DownloadFileInfo);
        if(rtn != 0){
            return -2;
        }
        OTADownloadMethod.RemoveDownloadDirFiles();
        OTADownloadMethod.SetTotalDownloadCount(0);
        OTADownloadMethod.SetTotalDownloadCountToProp(0);
        OTADownloadMethod.SetDownloadSpeedToProp(0);
        //OTADownloadMethod.ShowFilesInfo();

        return rtn;
    }

    public int DownloadStart() {

        int rtn = 0;

        rtn = OTADownloadMethod.DownloadAllFiles();

        return rtn;
    }

    public void DownloadFailed() {

        OTADownloadMethod.RemoveAllFilesInfoVec();
        OTADownloadMethod.RemoveAllFilesInfoFiles();
        OTADownloadMethod.SetDownloadSpeedToProp(0);
        OTAStatusCtrl.OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_DOWNLOAD_FAILED);

    }


}
