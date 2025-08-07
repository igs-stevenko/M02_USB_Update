package OTAUpdate.OTADownloadManager;

import android.os.SystemProperties;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import EnvVar.EnvVar;
import OTAUpdate.OTAStatusCtrl.OTAStatusCtrl;
import OTAUpdate.OTAVar.OTAVar;
import OTAUpdate.OTAVar.OTAVarDefine;
import model.FileControl.FileControl;

public class OTADownloadMethod {

    public static int DownloadAllFiles() {

        int rtn = 0;

        for (OTADownloadFileStruct element : OTAVar.DownloadFileInfoVec) {

            if(OTAStatusCtrl.OTA_STATUS_GET().equals(OTAVarDefine.OTA_STATUS_DOWNLOAD) == false){
                rtn = 1;
                break;
            }

            rtn = DownloadFile(element.Url, EnvVar.DOWNLOAD_PATH + element.FileName);
            if(rtn != 0){
                Log.d(TAGS, "Download failed");
                rtn = -1;
                break;
            }

            if(OTAStatusCtrl.OTA_STATUS_GET().equals(OTAVarDefine.OTA_STATUS_DOWNLOAD) == false){
                rtn = 1;
                break;
            }

            /*
            rtn = IsFileCorrect(EnvVar.DOWNLOAD_PATH + element.FileName, element.md5);
            if (rtn != 0) {
                Log.d(TAGS, "The Download File is damage");
                rtn = -2;
                break;
            }
             */
        }

        return rtn;
    }

    public static int IsFileCorrect(String DownloadPath, String Md5Info) {

        Log.d(TAGS, "IsFileCorrect()");

        int rtn = 0;

        String Md5String = FileControl.CalculateMD5(DownloadPath);

        if (Md5String.equals(Md5Info.toString()) == true) {
            rtn = 0;
        } else {
            rtn = -1;
        }

        return rtn;
    }


    public static int DownloadFile(String Url, String DownloadPath) {

        int rtn = 0;
        Log.d(TAGS, "DownloadFile Url : " + Url);
        Log.d(TAGS, "DownloadPath  : " + DownloadPath);


        try {
            URL url = new URL(Url);
            File file = new File(DownloadPath);

            if (url.getProtocol().toUpperCase().equals("HTTPS")) {
                Log.d(TAGS, "Is HTTPS");
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };


                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                connection.setConnectTimeout(60000);
                connection.setReadTimeout(60000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    //InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    InputStream inputStream = connection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(file);

                    byte[] buffer = new byte[1024 * 8];
                    int bytesRead;

                    int DownloadTmpCount_1 = 0;
                    int DownloadTmpCount_2 = 0;
                    long startTime = System.currentTimeMillis();
                    long endTime;
                    float Speed;
                    long TotalDownloadCount;
                    float TotalCost = 0;


                    while ((bytesRead = inputStream.read(buffer)) > 0) {

                        DownloadTmpCount_1 += bytesRead;
                        DownloadTmpCount_2 += bytesRead;
                        AddTotalDownloadCount(bytesRead);

                        if (DownloadTmpCount_1 >= OTAVar.PerSleepBytes) {
                            DownloadTmpCount_1 = 0;
                            Sleep(OTAVar.PerSleepMs);
                        }

                        outputStream.write(buffer, 0, bytesRead);

                        /* 計算下載速度 */
                        endTime = System.currentTimeMillis();
                        TotalCost = (endTime - startTime);
                        if(TotalCost > 1000){

                            if(OTAStatusCtrl.OTA_STATUS_GET().equals(OTAVarDefine.OTA_STATUS_DOWNLOAD) == false){
                                break;
                            }

                            Speed = (DownloadTmpCount_2 / (TotalCost/1000)) / (1024*1024);
                            TotalDownloadCount = GetTotalDownloadCount();
                            SetTotalDownloadCountToProp(TotalDownloadCount);
                            SetDownloadSpeedToProp(Speed);

                            DownloadTmpCount_2 = 0;
                            startTime = System.currentTimeMillis();
                        }
                    }

                    outputStream.flush();
                    outputStream.getFD().sync();
                    outputStream.close();
                    inputStream.close();

                } else {
                    rtn = -1;
                    Log.e(TAGS, "Server returned HTTPS response code: " + responseCode);
                }
            } else {
                rtn = -2;
                Log.e(TAGS, "Url Protocol errro, Only Supprot HTTPS");
            }
        } catch (MalformedURLException e) {
            Log.e(TAGS, "DownloadFile MalformedURLException");
            rtn = -3;
        } catch (IOException e) {
            Log.e(TAGS, "DownloadFile IOException");
            rtn = -4;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAGS, "DownloadFile NoSuchAlgorithmException");
            rtn = -5;
        } catch (KeyManagementException e) {
            Log.e(TAGS, "DownloadFile KeyManagementException");
            rtn = -6;
        }

        return rtn;
    }


    public static int CollectFilesInfoToVec(String DonwloadFileInfo)  {

        Log.d(TAGS, "CollectFilesInfo");

        int rtn = 0;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(DonwloadFileInfo);

            Iterator<String> Targets = jsonObject.keys();
            ;

            while (Targets.hasNext()) {

                OTADownloadFileStruct DownloadFileInfo = new OTADownloadFileStruct();
                String outerKey = Targets.next();
                JSONObject innerObject = null;

                innerObject = jsonObject.getJSONObject(outerKey);

                Iterator<String> innerKeys = innerObject.keys();
                DownloadFileInfo.FileName = outerKey;
                while (innerKeys.hasNext()) {
                    String innerKey = innerKeys.next();
                    String innerValue = innerObject.getString(innerKey); // 获取内层键对应的值

                    if (innerKey.equals("url")) {
                        DownloadFileInfo.Url = innerValue;
                    }

                    if (innerKey.equals("md5")) {
                        DownloadFileInfo.md5 = innerValue;
                    }
                }
                OTAVar.DownloadFileInfoVec.add(DownloadFileInfo);
            }
        }catch (JSONException e) {
            Log.d(TAGS, "CollectFilesInfoToVec JSONException" );
            rtn = -1;
        }
        return rtn;
    }

    public static void ShowFilesInfo() {

        Log.d(TAGS, "ShowFilesInfo");
        for (OTADownloadFileStruct element : OTAVar.DownloadFileInfoVec) {
            Log.d(TAGS, "File Name = " + element.FileName);
            Log.d(TAGS, "Url = " + element.Url);
            Log.d(TAGS, "MD5 = " + element.md5);
        }
    }

    public static void RemoveAllFilesInfoVec() {
        OTAVar.DownloadFileInfoVec.clear();
    }

    public static void RemoveAllFilesInfoFiles() {
        FileControl.RemoveFile(EnvVar.OTA_DOWNLOAD_FILE_INFO);
        FileControl.RemoveFile(EnvVar.OTA_DOWNLOAD_FILE_INFO_MD5);
    }

    public static void WriteFileInfoToFile(String Download_Info) {

        FileControl.RemoveFile(EnvVar.OTA_DOWNLOAD_FILE_INFO);
        FileControl.RemoveFile(EnvVar.OTA_DOWNLOAD_FILE_INFO_MD5);

        FileControl.WriteStringToFile(Download_Info, EnvVar.OTA_DOWNLOAD_FILE_INFO);
        String md5 = FileControl.CalculateMD5(EnvVar.OTA_DOWNLOAD_FILE_INFO);
        FileControl.WriteStringToFile(md5, EnvVar.OTA_DOWNLOAD_FILE_INFO_MD5);

    }

    public static void RemoveDownloadDirFiles() {

        FileControl.RemoveFolder(EnvVar.DOWNLOAD_PATH);
    }

    public static String ReadFileInfoFromFile() {

        int rtn = 0;
        String Md5String = FileControl.CalculateMD5(EnvVar.OTA_DOWNLOAD_FILE_INFO);
        String Md5Info = FileControl.ReadStringFromFile(EnvVar.OTA_DOWNLOAD_FILE_INFO_MD5);

        if (Md5String.equals(Md5Info) != true) {
            return "";
        }

        String Download_Info = FileControl.ReadStringFromFile(EnvVar.OTA_DOWNLOAD_FILE_INFO);

        return Download_Info;
    }

    public static void AddTotalDownloadCount(long DownloadCount){
        OTAVar.DownloadCount += DownloadCount;
    }

    public static void SetTotalDownloadCount(long DownloadCount){
        OTAVar.DownloadCount = DownloadCount;
    }

    private static long GetTotalDownloadCount(){
        return OTAVar.DownloadCount;
    }

    public static void SetTotalDownloadCountToProp(long DownloadCount){
        SystemProperties.set("rw.igs.downloadcount", Long.toString(DownloadCount));
    }

    public static void SetDownloadSpeedToProp(float Speed){
        SystemProperties.set("rw.igs.downloadspeed", String.format("%.3f", Speed));
    }

    private static void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        } catch (Exception e) {

        }
    }

    static String TAGS = "## [KO] OTADownloadMethod";
}
