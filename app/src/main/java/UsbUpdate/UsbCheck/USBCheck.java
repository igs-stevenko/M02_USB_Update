package UsbUpdate.UsbCheck;

import android.util.Log;

import java.io.File;

import EnvVar.EnvVar;
import model.FileControl.FileControl;

public class USBCheck {

    public static final String USB_PATH = "/mnt/media_rw/";

    public static boolean NeedUpdate() {

        /*檢查專案名稱*/
        EnvVar.USB_INFO_PROJNAME_FILE_PATH = FindFileInUSB(EnvVar.PROJNAME_FILE);
        Log.d(TAGS, "USB_INFO_PROJNAME_FILE_PATH = " + EnvVar.USB_INFO_PROJNAME_FILE_PATH);



        EnvVar.USB_ENC_GAME_FILE_PATH = FindFileInUSB(EnvVar.ENC_GAME_FILE);
        EnvVar.USB_INFO_GAME_FILE_PATH = FindFileInUSB(EnvVar.INFO_GAME_FILE);
        if(EnvVar.USB_ENC_GAME_FILE_PATH.equals("") || EnvVar.USB_INFO_GAME_FILE_PATH.equals("")) {
            Log.d(TAGS, "NeedUpdate : Game file not found");
            return false;
        }


        EnvVar.USB_ENC_SYSTEM_FILE_PATH = FindFileInUSB(EnvVar.ENC_SYSTEM_FILE);
        EnvVar.USB_INFO_SYSTEM_FILE_PATH = FindFileInUSB(EnvVar.INFO_SYSTEM_FILE);
        if(EnvVar.USB_ENC_SYSTEM_FILE_PATH.equals("") || EnvVar.USB_INFO_SYSTEM_FILE_PATH.equals("")) {
            Log.d(TAGS, "NeedUpdate : System file not found");
            return false;
        }

        return true;
    }

    public static String FindFileInUSB(String FileName){
        File targetdir = new File(USB_PATH);
        File targetfile = FindFile(targetdir, FileName);
        if(targetfile.toString() != "")
            return targetfile.toPath().toString();
        else
            return "";
    }

    public static File FindFile(File Dir, String targetname){

        File targetfile = new File("");
        File[] files = Dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    //Log.d(TAGS, file.toString());
                    targetfile = FindFile(file, targetname);
                }
                else {
                    //Log.d(TAGS, "File : " + file.getName());
                    if (file.getName().equals(targetname)) {
                        targetfile = file;
                        break;
                    }
                }
            }
        }

        return targetfile;
    }
    static String TAGS = "## [KO] USBCheck ";
}
