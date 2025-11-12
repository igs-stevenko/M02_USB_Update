package UsbUpdate.UsbUpdateManager;

import android.content.Context;
import android.util.Log;

import EnvVar.EnvVar;
import UpdateControl.UpdateMethod;
import UsbUpdate.UsbUpdateStatusCtrl.UsbUpdateStatusCtrl;
import UsbUpdate.UsbVarDefine.UsbVar;
import UsbUpdate.UsbVarDefine.UsbVarDefine;
import model.FileControl.FileControl;

public class UsbUpdateManager {

    private Context sContext;
    private UpdateMethod mUsbUpdateMethod;

    public UsbUpdateManager(Context mContext){
        sContext = mContext;
        mUsbUpdateMethod = new UpdateMethod(sContext);
    }
    public void USB_Update_Init() {

        UsbVar.UpdateStatus = UsbVarDefine.START;
        FileControl.RemoveFolder(EnvVar.TMP_PATH);
        /* 清除OTA的狀態，只要有進入FOTA，都將OTA的檔案刪除 */
        FileControl.RemoveFile(EnvVar.OTA_STATUS_FILE);
        FileControl.RemoveFolder(EnvVar.DOWNLOAD_PATH);

    }

    public void USB_Update_Finish() {

        UsbVar.UpdateStatus = UsbVarDefine.UPDATEFINISH;
        //todo 加入try catch
        FileControl.CopyFile(EnvVar.TMP_PATH + EnvVar.INFO_GAME_FILE, EnvVar.MEDIA_PATH + EnvVar.INFO_GAME_FILE);
        FileControl.CopyFile(EnvVar.TMP_PATH + EnvVar.INFO_SYSTEM_FILE, EnvVar.MEDIA_PATH + EnvVar.INFO_SYSTEM_FILE);
        FileControl.RemoveFolder(EnvVar.TMP_PATH);
        /*【防呆檢查】: 更新完畢，刪除旗標檔案，使下次可以進入一般流程*/
        UsbUpdateStatusCtrl.UsbUpdate_FileRemove();
    }

    public void USB_Update_Failed(int ErrorCode) {

        UsbVar.UpdateStatus = ErrorCode;

    }

    public void USB_Update_SAMEVERSION() {

        UsbVar.UpdateStatus = UsbVarDefine.SAMEVERSION;
        /*【防呆檢查】: 相同版本不更新，仍然嘗試刪除旗標檔案，使下次可以進入一般流程*/
        UsbUpdateStatusCtrl.UsbUpdate_FileRemove();
    }

    int IsSameProject(){
        int rtn = 0;

        String ProjectName = FileControl.ReadStringFromFile(EnvVar.USB_INFO_PROJNAME_FILE_PATH);
        Log.d(TAGS, "Target ProjectName = " + ProjectName);
        if(ProjectName.equals(EnvVar.PROJECT_NAME)){
            Log.d(TAGS, "Same ProjectName");
            return 0;
        }
        else{
            Log.d(TAGS, "Defernet ProjectName");
            return -1;
        }
    }


    public int USB_Update(){
        try {
            int rtn = 0;
            boolean IsGameSameVer = false;
            boolean IsSystemSameVer = false;

            Log.d(TAGS, "UpdateThread Start");



            if(UsbUpdateStatusCtrl.UsbUpdate_FileExist() != true){
                /* 先檢查是否Game與System版本相同，若都相同則不更新 */
                IsGameSameVer = mUsbUpdateMethod.IsSameVersion(EnvVar.USB_INFO_GAME_FILE_PATH,EnvVar.MEDIA_PATH + EnvVar.INFO_GAME_FILE);
                IsSystemSameVer = mUsbUpdateMethod.IsSameVersion(EnvVar.USB_INFO_SYSTEM_FILE_PATH,EnvVar.MEDIA_PATH + EnvVar.INFO_SYSTEM_FILE);

                if(IsGameSameVer == true && IsSystemSameVer == true){
                    return UsbVarDefine.SAMEVERSION;
                }
            }

            /*【防呆檢查】: 更新時建立更新檔案旗標，代表狀態為"更新中"*/
            if(UsbUpdateStatusCtrl.UsbUpdate_FileCreate() != true){
                /*創建FOTA_Update.bin失敗，不要進行更新*/
                return UsbVarDefine.UPDATE_STATUS_FILECREATE_FAILED;
            }

            /* 判斷更新包與當前系統是否為相同專案 */
            rtn = IsSameProject();
            if(rtn < 0) {
                return UsbVarDefine.DEFERENT_PROJECT_NAME;
            }

            /* 取得Game更新包的Size */
            UsbVar.UpdateFileSize = FileControl.GetFileSize( EnvVar.USB_ENC_GAME_FILE_PATH);

            /* 複製遊戲&系統更新包到tmp下 */
            UsbVar.UpdateStatus = UsbVarDefine.COPYFILE;

            Log.d(TAGS, "CopyUpdateFile Start");
            rtn = CopyUpdateFile();
            if(rtn < 0) {
                return UsbVarDefine.COPYFILE_FAILED;
            }

            /* 檢查遊戲&系統更新包的完整性 */
            UsbVar.UpdateStatus = UsbVarDefine.CHECKFILE;

            Log.d(TAGS, "CheckFile Start");
            rtn = mUsbUpdateMethod.CheckFile(EnvVar.TMP_PATH + EnvVar.ENC_GAME_FILE, EnvVar.TMP_PATH + EnvVar.INFO_GAME_FILE);
            if(rtn < 0) {
                return UsbVarDefine.CHECKFILE_FAILED;
            }

            rtn = mUsbUpdateMethod.CheckFile(EnvVar.TMP_PATH + EnvVar.ENC_SYSTEM_FILE, EnvVar.TMP_PATH + EnvVar.INFO_SYSTEM_FILE);
            if(rtn < 0) {
                return UsbVarDefine.CHECKFILE_FAILED;
            }

            /* 刪除該刪的檔案*/
            Log.d(TAGS, "Remove Start");
            FileControl.RemoveFolder(EnvVar.MEDIA_PATH);
            FileControl.RemoveFile(EnvVar.TMP_APK_PATH);
            FileControl.RemoveFile(EnvVar.TMP_README_PATH);

            /* 解密遊戲&系統更新包 */
            UsbVar.UpdateStatus = UsbVarDefine.DECRYPTFILE;

            Log.d(TAGS, "DecryptFile Start");
            rtn = mUsbUpdateMethod.DecryptFile(EnvVar.TMP_PATH + EnvVar.ENC_GAME_FILE, EnvVar.TMP_PATH + EnvVar.DEC_GAME_FILE);
            if(rtn < 0) {
                return UsbVarDefine.DECRYPTFILE_FAILED;
            }

            rtn = mUsbUpdateMethod.DecryptFile(EnvVar.TMP_PATH + EnvVar.ENC_SYSTEM_FILE, EnvVar.TMP_PATH + EnvVar.DEC_SYSTEM_FILE);
            if(rtn < 0) {
                return UsbVarDefine.DECRYPTFILE_FAILED;
            }

            /* 解壓縮遊戲&系統更新包 */

            UsbVar.UpdateStatus = UsbVarDefine.UNZIPFILE;

            Log.d(TAGS, "UnzipFileWithoutFristName Start");
            rtn = mUsbUpdateMethod.UnzipFileWithoutFristName(EnvVar.TMP_PATH + EnvVar.DEC_GAME_FILE, EnvVar.DATA_PATH);
            if(rtn < 0) {
                return UsbVarDefine.UNZIPFILE_FAILED;
            }

            rtn = mUsbUpdateMethod.UnzipFile(EnvVar.TMP_PATH + EnvVar.DEC_SYSTEM_FILE, EnvVar.TMP_PATH);
            if(rtn < 0) {
                return UsbVarDefine.UNZIPFILE_FAILED;
            }

            /* 更新遊戲App */
            UsbVar.UpdateStatus = UsbVarDefine.UPDATEAPP;

            Log.d(TAGS, "UpdateApp Start");
            rtn = mUsbUpdateMethod.UpdateApp(EnvVar.TMP_APK_PATH);
            if(rtn < 0) {
                return UsbVarDefine.UPDATEAPP_FAILED;
            }

           /*
            UsbVar.UpdateStatus = UsbVarDefine.UPDATEMEDIA;

            rtn = mUsbUpdateMethod.UpdateMedia(EnvVar.TMP_MEDIA_PATH, EnvVar.MEDIA_PATH);
            if(rtn < 0) {
                return UsbVarDefine.UPDATEMEDIA_FAILED;
            }
            */

            /* 更新系統 */
            UsbVar.UpdateStatus = UsbVarDefine.UPDATESYSTEM;

            mUsbUpdateMethod.SendBroadCastUpdateSystem();
            mUsbUpdateMethod.WaitForCompleted();
            if( mUsbUpdateMethod.IsSystemUpdateSuccess() == false) {
                return UsbVarDefine.UPDATESYSTEM_FAILED;
            }

            return 0;

        } catch(Exception e){
            return UsbVarDefine.UNKNOW_FAILED;
        }
    }

    private int CopyUpdateFile(){

        int rtn = 0;

        String sourceFile = EnvVar.USB_ENC_GAME_FILE_PATH;
        String targetFile = EnvVar.TMP_PATH + EnvVar.ENC_GAME_FILE;

        rtn = FileControl.CopyFile(sourceFile, targetFile);
        if(rtn < 0){
            return rtn;
        }

        sourceFile = EnvVar.USB_INFO_GAME_FILE_PATH;
        targetFile = EnvVar.TMP_PATH + EnvVar.INFO_GAME_FILE;

        rtn = FileControl.CopyFile(sourceFile, targetFile);
        if(rtn < 0){
            return rtn;
        }

        sourceFile = EnvVar.USB_ENC_SYSTEM_FILE_PATH;
        targetFile = EnvVar.TMP_PATH + EnvVar.ENC_SYSTEM_FILE;

        rtn = FileControl.CopyFile(sourceFile, targetFile);
        if(rtn < 0){
            return rtn;
        }

        sourceFile = EnvVar.USB_INFO_SYSTEM_FILE_PATH;
        targetFile = EnvVar.TMP_PATH + EnvVar.INFO_SYSTEM_FILE;

        rtn = FileControl.CopyFile(sourceFile, targetFile);

        return rtn;
    }

    static String TAGS = "## [KO] UsbUpdateManager";
}
