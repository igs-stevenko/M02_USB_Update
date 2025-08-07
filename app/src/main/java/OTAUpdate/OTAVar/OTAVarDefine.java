package OTAUpdate.OTAVar;

public class OTAVarDefine {

    public final static int START = 0;
    public final static int CHECKFILE = 1;
    public final static int DECRYPTFILE = 2;
    public final static int UNZIPFILE = 3;
    public final static int UPDATEAPP = 4;
    public final static int UPDATEMEDIA = 5;
    public final static int CHECKSYSTEMFILE = 6;
    public final static int DECRYPTSYSTEMFILE = 7;
    public final static int UPDATESYSTEM = 8;
    public final static int UPDATEFINISH = 9;
    public final static int CHECKFILE_FAILED = -1;
    public final static int DECRYPTFILE_FAILED = -2;
    public final static int UNZIPFILE_FAILED = -3;
    public final static int UPDATEAPP_FAILED = -4;
    public final static int UPDATEMEDIA_FAILED = -5;
    public final static int CHECKSYSTEMFILE_FAILED = -6;
    public final static int DECRYPTSYSTEMFILE_FAILED = -7;
    public final static int UPDATESYSTEM_FAILED = -8;
    public final static int NONE_UPDATE_FILES = -9;

    public final static int UNKNOW_FAILED = -10;

    public final static String OTA_KEY_STATUS = "Status";
    public final static String OTA_STATUS_NORMAL = "NORMAL";
    public final static String OTA_STATUS_DOWNLOAD = "DOWNLOAD";
    public final static String OTA_STATUS_DOWNLOAD_COMPLETED = "DOWNLOAD_COMPLETED";
    public final static String OTA_STATUS_DOWNLOAD_FAILED = "DOWNLOAD_FAILED";
    public final static String OTA_STATUS_UPDATE = "UPDATE";
    public final static String OTA_STATUS_UPDATE_COMPLETED = "UPDATE_COMPLETED";
    public final static int CMD_RECV_GAME_UPDATE = 0;
    public final static int CMD_RECV_GAME_DOWNLOAD = 1;
    public final static int CMD_RECV_GAME_RESET = 2;
    public final static int CMD_RECV_GAME_SETTING = 3;

}
