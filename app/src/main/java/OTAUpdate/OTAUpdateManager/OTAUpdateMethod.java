package OTAUpdate.OTAUpdateManager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

import EnvVar.EnvVar;
import OTAUpdate.OTAVar.OTAVar;
import model.ApkControl.ApkControl;
import model.Crypto.Crypto;
import model.FileControl.FileControl;

public class OTAUpdateMethod {

    private Context sContext;
    private ApkControl mApkControl;

    public OTAUpdateMethod(Context mContext){

        sContext = mContext;
        mApkControl = new ApkControl(sContext);
    }

    public int UpdateApp(String Source){

        String InstallPkgName = mApkControl.GetApkPkgName(Source);
        Log.d(TAGS, "InstallPkgName : " + InstallPkgName);

        int rtn = 0;
        rtn = mApkControl.uninstall_app(InstallPkgName);
        if(rtn != 0){
            return -1;
        }

        Sleep(1000);

        rtn = mApkControl.install_app(InstallPkgName, Source);
        if(rtn != 0){
            return -2;
        }

        while(mApkControl.isAppInstalled(InstallPkgName) != true){
            Sleep(1000);
        }

        return rtn;
    }

    public int UpdateMedia(String Source, String Target)  {

        int rtn = 0;

        FileControl.RemoveFolder(Target);
        try {
            rtn = FileControl.CopyAllDir(Source, Target);
        } catch (IOException e) {
            rtn = -1;
        }

        return rtn;
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

        FileControl.Unzip(Source, Target);

        return rtn;
    }


    public boolean IsNeedUpdateGame(){

        return (FileControl.IsFileExist(EnvVar.DOWNLOAD_PATH + EnvVar.INFO_GAME_FILE) && FileControl.IsFileExist(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_GAME_FILE));
    }

    public boolean IsNeedUpdateSystem(){

        return FileControl.IsFileExist(EnvVar.DOWNLOAD_PATH + EnvVar.ENC_SYSTEM_FILE);
    }

    static String TAGS = "## [KO] OTAUpdateManager";
}
