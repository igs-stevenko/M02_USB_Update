package OTAUpdate.OTAStatusCtrl;

import android.os.SystemProperties;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import EnvVar.EnvVar;
import OTAUpdate.OTAVar.OTAVarDefine;
import model.FileControl.FileControl;


public class OTAStatusCtrl {

    public static boolean NeedUpdate()  {


        if (FileControl.IsFileExist(EnvVar.OTA_STATUS_FILE) == false) {
            OTA_STATUS_SET(OTAVarDefine.OTA_STATUS_NORMAL);
            return false;
        }

        String StrStatus = OTA_STATUS_GET();

        Log.d(TAGS, "StrStatus == " + StrStatus);

        SystemProperties.set("rw.igs.otastatus", StrStatus);

        if (StrStatus.equals(OTAVarDefine.OTA_STATUS_UPDATE) == true) {
            return true;
        } else {
            return false;
        }
    }

    public static String OTA_STATUS_GET()  {

        String Status = "";
        if (FileControl.IsFileExist(EnvVar.OTA_STATUS_FILE) == false) {
            return "";
        }

        Status = ReadFromJsonFile(EnvVar.OTA_STATUS_FILE, OTAVarDefine.OTA_KEY_STATUS);
        if(Status.equals("") == true) {

            String FileBuffer = FileControl.ReadStringFromFile(EnvVar.OTA_STATUS_FILE);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(OTAVarDefine.OTA_KEY_STATUS, OTAVarDefine.OTA_STATUS_NORMAL);
            } catch (JSONException e) {
                Log.d(TAGS, "OTA_STATUS_GET JSONException");
                return "";
            }
            FileControl.WriteStringToFile(jsonObject.toString(), EnvVar.OTA_STATUS_FILE);

            SystemProperties.set("rw.igs.otastatus", OTAVarDefine.OTA_STATUS_NORMAL);
        }

        return Status;
    }

    public static int OTA_STATUS_SET(String Value)  {
        int rtn = 0;
        rtn = WriteToJsonFile(EnvVar.OTA_STATUS_FILE, OTAVarDefine.OTA_KEY_STATUS, Value);
        if(rtn == 0){
            SystemProperties.set("rw.igs.otastatus", Value);
        }

        return rtn;
    }


    public static String ReadFromJsonFile(String FileName, String Key)  {

        if (FileControl.IsFileExist(FileName) == false) {
            return "";
        }
        String StrStatus = "";

        try {
            String FileBuffer = FileControl.ReadStringFromFile(FileName);
            JSONObject jsonObject = new JSONObject(FileBuffer);
            try {
                StrStatus = jsonObject.getString(Key);
            } catch (Exception e) {
                Log.d(TAGS, "ReadFromJsonFile Exception");
                StrStatus = "";
            }
        } catch (JSONException e) {
            Log.d(TAGS, "ReadFromJsonFile JSONException");
            StrStatus = "";
        }
        return StrStatus.toString();
    }

    public static int WriteToJsonFile(String FileName, String Key, String Value) {

        Log.d(TAGS, "WriteToJsonFile");
        int rtn = 0;
        try {
            if (FileControl.IsFileExist(FileName) == false) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Key, Value);
                FileControl.WriteStringToFile(jsonObject.toString(), FileName);
            } else {

                String FileBuffer = FileControl.ReadStringFromFile(FileName);
                JSONObject jsonObject = null;
                if(FileBuffer.equals("") == true){
                    jsonObject = new JSONObject();
                }
                else{
                    jsonObject = new JSONObject(FileBuffer);
                }

                jsonObject.put(Key, Value);
                rtn = FileControl.WriteStringToFile(jsonObject.toString(), FileName);
                if(rtn != 0){
                    rtn = -2;
                }
            }
            rtn = 0;

        } catch (JSONException e) {
            rtn = -1;
            Log.d(TAGS, "WriteToJsonFile JSONException");
        }

        return rtn;
    }

    static String TAGS = "## [KO] OTAStatusCtrl";
}
