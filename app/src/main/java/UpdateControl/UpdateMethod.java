package UpdateControl;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

import EnvVar.EnvVar;
import model.FileControl.FileControl;
import model.ApkControl.ApkControl;
import model.Crypto.Crypto;


public class UpdateMethod {

    private Context sContext;
    private ApkControl mApkControl;

    public UpdateMethod(Context mContext){

        sContext = mContext;
        mApkControl = new ApkControl(sContext);
    }

    public boolean IsSameVersion(String UsbSource, String MediaSource){

        String NowVersion = "";
        String NewVersion = "";

        if(FileControl.IsFileExist(MediaSource) == false){
            return false;
        }

        NowVersion = FileControl.ReadStringFromFile(MediaSource);
        NewVersion = FileControl.ReadStringFromFile(UsbSource);

        /*如果從Media/下，拿到的新版本是空字串，則直接判斷為不同版本*/
        if(NowVersion.equals("") == true){
            return false;
        }

        if(NowVersion.equals(NewVersion.toString()) == true){
            return true;
        }

        return false;
    }

    public int CheckFile(String SourceFile, String MD5File) {

        int rtn = 0;

        String Md5String = FileControl.CalculateMD5(SourceFile);
        String Md5InInfo = FileControl.ReadStringFromFile(MD5File);
        if(Md5String.equals(Md5InInfo.toString()) == true){
            Log.d(TAGS, "Same Md5 ");
            rtn = 0;
        }
        else{
            Log.d(TAGS, "Different Md5 ");
            rtn = -1;
        }
        return rtn;
    }

    public int DecryptFile(String Source, String Target) {

        int rtn = 0;

        rtn = Crypto.decryptAESCBCFile(Source, Target, EnvVar.Key, EnvVar.Iv);

        return rtn;

    }

    public int UnzipFile(String Source, String Target) throws IOException {

        int rtn = 0;

        rtn = FileControl.Unzip(Source, Target);

        return rtn;
    }

    public int UnzipFileWithoutFristName(String Source, String Target) throws IOException {

        int rtn = 0;

        rtn = FileControl.UnzipWithoutFirstName(Source, Target);

        return rtn;
    }


    public int UpdateApp(String Source){

        String InstallPkgName = mApkControl.GetApkPkgName(Source);
        if(InstallPkgName == null){
            return -1;
        }
        Log.d(TAGS, "InstallPkgName : " + InstallPkgName);

        int rtn = 0;

        Sleep(1000);

        rtn = mApkControl.install_app(InstallPkgName, Source);
        if(rtn != 0){
            return -2;
        }

        while(mApkControl.isAppInstalled(InstallPkgName) != true){
            Sleep(1000);
        }

        FileControl.RemoveFile(Source);
        FileControl.RemoveFile(EnvVar.TMP_README_PATH);


        return rtn;
    }

    public int UpdateMedia(String Source, String Target) throws IOException {

        int rtn = 0;

        rtn = FileControl.RemoveFolder(Target);
        if(rtn < 0) {
            Log.d(TAGS, "RemoveFolder ... ");
            return rtn;
        }

        rtn = FileControl.CopyAllDir(Source, Target);
        if(rtn < 0) {
            Log.d(TAGS, "CopyAllDir ... ");
            return rtn;
        }

        return 0;
    }

    public void SendBroadCastUpdateSystem(){
        Intent mIntent = new Intent();
        mIntent.setAction("OTA_update");
        mIntent.putExtra("OTA_UPDATE_AB_SYSYTEM_PATH", EnvVar.TMP_PATH + EnvVar.DEC_SYSTEM_FILE);
        sContext.sendBroadcast(mIntent);
    }

    public void WaitForCompleted(){
        while (!EnvVar.SYSTEM_UPDATE_COMPLETE){
            Log.d(TAGS, "Wait ABUPDATE Recive ... ");
            Sleep(1000);
        }
    }

    public boolean IsSystemUpdateSuccess(){
        if(EnvVar.SYSTEM_UPDATE_SUCCESS == true){
            return true;
        }
        else{
            return false;
        }
    }

    private void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        }catch(Exception e) {

        }
    }

    public boolean IsNeedUpdateGame(){

        return (FileControl.IsFileExist(EnvVar.DOWNLOAD_PATH + EnvVar.INFO_GAME_FILE) && FileControl.IsFileExist(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_GAME_FILE));
    }

    public boolean IsNeedUpdateSystem(){

        return FileControl.IsFileExist(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_SYSTEM_FILE);
    }
    static String TAGS = "## [KO] UpdateMethod";
}
