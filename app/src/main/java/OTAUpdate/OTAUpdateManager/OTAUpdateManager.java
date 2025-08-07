package OTAUpdate.OTAUpdateManager;

import android.content.Context;
import android.util.Log;

import EnvVar.EnvVar;
import OTAUpdate.OTAStatusCtrl.OTAStatusCtrl;
import OTAUpdate.OTAVar.OTAVar;
import OTAUpdate.OTAVar.OTAVarDefine;
import UpdateControl.UpdateMethod;
import model.FileControl.FileControl;

public class OTAUpdateManager {

    private Context sContext;
    private UpdateMethod mUpdateMethod;
    public OTAUpdateManager(Context mContext) {

        sContext = mContext;
        mUpdateMethod = new UpdateMethod(mContext);
    }

    public int OTA_Update_System() {

        try {
            int rtn = 0;

            if (mUpdateMethod.IsNeedUpdateSystem() == false) {
                return 0;
            }

            OTAVar.UpdateStatus = OTAVarDefine.CHECKSYSTEMFILE;

            rtn = mUpdateMethod.CheckFile(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_SYSTEM_FILE, EnvVar.DOWNLOAD_PATH + EnvVar.INFO_SYSTEM_FILE);
            if (rtn < 0) {
                return OTAVarDefine.CHECKSYSTEMFILE_FAILED;
            }

            OTAVar.UpdateStatus = OTAVarDefine.DECRYPTSYSTEMFILE;

            rtn = mUpdateMethod.DecryptFile(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_SYSTEM_FILE, EnvVar.TMP_PATH + EnvVar.DEC_SYSTEM_FILE);
            if (rtn < 0) {
                return OTAVarDefine.DECRYPTSYSTEMFILE_FAILED;
            }

            OTAVar.UpdateStatus = OTAVarDefine.UPDATESYSTEM;
            mUpdateMethod.SendBroadCastUpdateSystem();
            mUpdateMethod.WaitForCompleted();
            if (mUpdateMethod.IsSystemUpdateSuccess() == false) {
                OTAVar.UpdateStatus = OTAVarDefine.UPDATESYSTEM_FAILED;
                return OTAVarDefine.UPDATESYSTEM_FAILED;
            }

            OTAVar.IsSystemUpdated = true;

            return 0;

        } catch (Exception e) {

            return OTAVarDefine.UNKNOW_FAILED;
        }
    }

    public int OTA_Update_Game() {
        try {
            int rtn = 0;

            if (mUpdateMethod.IsNeedUpdateGame() == false) {
                return 0;
            }

            OTAVar.GameUpdateFileSize = FileControl.GetFileSize(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_GAME_FILE);

            OTAVar.UpdateStatus = OTAVarDefine.CHECKFILE;

            rtn = mUpdateMethod.CheckFile(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_GAME_FILE, EnvVar.DOWNLOAD_PATH + EnvVar.INFO_GAME_FILE);
            if (rtn < 0) {
                return OTAVarDefine.CHECKFILE_FAILED;
            }

            /* 解壓縮前先刪除該刪的檔案 */
            Log.d(TAGS, "Remove Start");
            FileControl.RemoveFolder(EnvVar.MEDIA_PATH);
            FileControl.RemoveFile(EnvVar.TMP_APK_PATH);
            FileControl.RemoveFile(EnvVar.TMP_README_PATH);

            OTAVar.UpdateStatus = OTAVarDefine.DECRYPTFILE;

            rtn = mUpdateMethod.DecryptFile(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_GAME_FILE, EnvVar.TMP_PATH + EnvVar.DEC_GAME_FILE);
            if (rtn < 0) {
                return OTAVarDefine.DECRYPTFILE_FAILED;
            }

            OTAVar.UpdateStatus = OTAVarDefine.UNZIPFILE;

            rtn = mUpdateMethod.UnzipFileWithoutFristName(EnvVar.TMP_PATH + EnvVar.DEC_GAME_FILE, EnvVar.DATA_PATH);
            if (rtn < 0) {
                return OTAVarDefine.UNZIPFILE_FAILED;
            }

            OTAVar.UpdateStatus = OTAVarDefine.UPDATEAPP;

            rtn = mUpdateMethod.UpdateApp(EnvVar.TMP_APK_PATH);
            if (rtn < 0) {
                return OTAVarDefine.UPDATEAPP_FAILED;
            }

            /*
            OTAVar.UpdateStatus = OTAVarDefine.UPDATEMEDIA;

            rtn = mUpdateMethod.UpdateMedia(EnvVar.TMP_MEDIA_PATH, EnvVar.MEDIA_PATH);
            if (rtn < 0) {
                return OTAVarDefine.UPDATEMEDIA_FAILED;
            }
            */

            OTAVar.IsGameUpdated = true;

            return 0;

        } catch (Exception e) {

            return OTAVarDefine.UNKNOW_FAILED;
        }
    }

    public void OTA_Update_Failed(int Error) {

        if (Error == OTAVarDefine.NONE_UPDATE_FILES) {
            OTAStatusCtrl.OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_NORMAL);
        }

        OTAVar.UpdateStatus = Error;
    }

    public void OTA_Update_Init() {

        OTAVar.UpdateStatus = OTAVarDefine.START;
        FileControl.RemoveFolder(EnvVar.TMP_PATH);
    }

    public void OTA_Update_Finish() {

        OTAVar.UpdateStatus = OTAVarDefine.UPDATEFINISH;
        FileControl.RemoveFolder(EnvVar.TMP_PATH);
        OTAStatusCtrl.OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_UPDATE_COMPLETED);
    }

    static String TAGS = "## [KO] OTAUpdateManager";
}
