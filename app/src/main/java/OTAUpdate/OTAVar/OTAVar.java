package OTAUpdate.OTAVar;

import java.util.Iterator;
import java.util.Vector;

import OTAUpdate.OTADownloadManager.OTADownloadFileStruct;

public class OTAVar {

    public static long GameUpdateFileSize = 0;
    public static long SystemUpdateFileSize = 0;
    public static int UpdateStatus = 0;
    public static int PerSleepBytes = 8912;
    public static int PerSleepMs = 1;
    public static long DownloadCount = 0;
    public static boolean IsGameUpdated = false;
    public static boolean IsSystemUpdated = false;
    public static Vector<OTADownloadFileStruct> DownloadFileInfoVec;

}
