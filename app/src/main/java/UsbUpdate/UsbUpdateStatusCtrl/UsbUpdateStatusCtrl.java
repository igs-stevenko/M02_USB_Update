package UsbUpdate.UsbUpdateStatusCtrl;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import model.FileControl.FileControl;
import EnvVar.EnvVar;

public class UsbUpdateStatusCtrl {


    public static boolean UsbUpdate_FileExist(){

        boolean rtn;

        if(FileControl.IsFileExist(EnvVar.USB_StatusFilePath) == true) {
            rtn = true;
        }
        else{
            rtn = false;
        }

        return rtn;
    }

    public static boolean UsbUpdate_FileCreate(){

        File sourceFile = new File(EnvVar.USB_StatusFilePath);
        try {
            if(sourceFile.exists() == true){
                /*檔案存在，無須再創建一次，直接回傳*/
                return true;
            }

            FileOutputStream fos = new FileOutputStream(sourceFile);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAGS, "[UsbUpdate_FileCreate] : FileNotFoundException");
            return false;
        } catch (IOException e) {
            Log.d(TAGS, "[UsbUpdate_FileCreate] : IOException");
            return false;
        }

        return true;
    }

    public static boolean UsbUpdate_FileRemove(){

        File sourceFile = new File(EnvVar.USB_StatusFilePath);
        if(sourceFile.exists() == true){
            sourceFile.delete();
        }

        return true;
    }

    static String TAGS = "## [KO] UsbUpdateStatusCtrl";
}
