package EnvVar;

public class EnvVar {

    public static final String INFO_GAME_FILE = "info.txt";
    public static final String INFO_SYSTEM_FILE = "system_info.txt";
    public static final String ENC_GAME_FILE = "update.zip";
    public static final String DEC_GAME_FILE = "dec_update.zip";
    public final static String ENC_SYSTEM_FILE = "system_update.zip";
    public final static String DEC_SYSTEM_FILE = "merged-qssi_sdm845-ota.zip";
    public static final String DATA_PATH = "/data/";
    public static final String MEDIA_PATH = "/data/Media/";
    //public static final String TMP_MEDIA_PATH = "/data/Media/";
    //public static final String TMP_MEDIA_PATH = "/data/tmp/Resource/Media/";
    public static final String TMP_README_PATH = "/data/Readme.txt";
    public static final String TMP_APK_PATH = "/data/game.apk";
    //public static final String TMP_APK_PATH = "/data/tmp/Resource/game.apk";
    public static final String TMP_PATH = "/data/tmp/";
    public static final String FOR_LAUNCHER_PATH = "/data/media/forLauncher.txt";
    public static final String DOWNLOAD_PATH = "/sdcard/Download/";
    public static final String OTA_STATUS_FILE = "/data/media/ota_status.json";
    public static final String OTA_DOWNLOAD_FILE_INFO = "/data/media/fileinfo.json";
    public static final String OTA_DOWNLOAD_FILE_INFO_MD5 = "/data/media/fileinfo_md5.txt";
    public static final String USB_StatusFilePath = "/data/media/FOTA_Update.bin";

    public static final String Key = "f2a8b0e7c9d34105";
    public static final String Iv = "7f3e9d0a1b5c8e2f";

    public static String PRODUCT_TYPE = "";
    public static String GAME_PACKAGE_NAME;
    public static String USB_ENC_GAME_FILE_PATH;
    public static String USB_INFO_GAME_FILE_PATH;

    public static String USB_ENC_SYSTEM_FILE_PATH;
    public static String USB_INFO_SYSTEM_FILE_PATH;

    public static boolean SYSTEM_UPDATE_COMPLETE = false;
    public static boolean SYSTEM_UPDATE_SUCCESS = false;

    public static int UpdateConfig = 0;
    public static final int UPDATE_FROM_USB = 1;
    public static final int UPDATE_FROM_OTA = 2;



}

