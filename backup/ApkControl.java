package model.ApkControl;

import android.content.BroadcastReceiver;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


public class ApkControl {

    private Context sContext;

    private static final String EXTRA_LEGACY_STATUS_COMPAT = "android.content.pm.extra.LEGACY_STATUS";
    private static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE_COMPAT = -25;

    public ApkControl(Context mContext){

        sContext = mContext;
    }

    public int uninstall_app(String pkgname){

        int rtn = 0;
        Log.d(TAGS, "01");
        PackageManager packageManager = sContext.getPackageManager();
        Intent intent = new Intent();
        intent.putExtra("key", "value");
        PendingIntent pendingIntent = PendingIntent.getActivity(sContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        IntentSender intentSender = pendingIntent.getIntentSender();
        packageManager.getPackageInstaller().uninstall(pkgname ,  intentSender) ;
        Log.d(TAGS, "02");
        boolean isInstalled = true;
        do {
            isInstalled = isAppInstalled(pkgname);
            if (isInstalled) {
                // 应用已安装
                Log.d(TAGS, "App已安裝");
            } else {
                // 应用未安装
                Log.d(TAGS, "App未安裝");
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                //Todo 加return
                return -1;
            }
        }while(isInstalled != false);

        return rtn;
    }

    public int install_app(String pkgname, String AppPath){

        int rtn = 0;

        PackageInstaller packageInstaller = sContext.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(pkgname);
        try {
            Method allowDowngrade = PackageInstaller.SessionParams.class.getMethod("setAllowDowngrade", boolean.class);
            allowDowngrade.setAccessible(true);
            allowDowngrade.invoke(params, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        OutputStream os = null;
        InputStream is = null;
        PackageInstaller.Session session = null;

        final String resultAction = "model.ApkControl.ApkControl.INSTALL_RESULT_" + System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger installStatus = new AtomicInteger(PackageInstaller.STATUS_FAILURE);
        final AtomicInteger installLegacyStatus = new AtomicInteger(PackageInstaller.STATUS_FAILURE);
        final AtomicReference<String> installMessage = new AtomicReference<>("");

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !resultAction.equals(intent.getAction())) {
                    return;
                }
                int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
                int legacyStatus = intent.getIntExtra(EXTRA_LEGACY_STATUS_COMPAT, Integer.MIN_VALUE);
                String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                installStatus.set(status);
                installLegacyStatus.set(legacyStatus);
                installMessage.set(message == null ? "" : message);
                latch.countDown();
            }
        };

        try {
            int sessionId = packageInstaller.createSession(params);
            session = packageInstaller.openSession(sessionId);
            os = session.openWrite(pkgname, 0, -1);
            is = new FileInputStream(AppPath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            session.fsync(os);
            os.close();
            os = null;
            is.close();
            is = null;

            IntentFilter filter = new IntentFilter(resultAction);
            if (Build.VERSION.SDK_INT >= 33) {
                sContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                sContext.registerReceiver(receiver, filter);
            }

            Intent resultIntent = new Intent(resultAction);
            IntentSender statusReceiver = PendingIntent.getBroadcast(
                    sContext,
                    sessionId,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            ).getIntentSender();

            session.commit(statusReceiver);

            boolean received = latch.await(60, TimeUnit.SECONDS);
            if (!received) {
                rtn = -10; // timeout
            } else if (installStatus.get() == PackageInstaller.STATUS_SUCCESS) {
                rtn = 0;
            } else {
                String msg = installMessage.get();
                Log.e(TAGS, "Install failed. status=" + installStatus.get()
                        + " legacy=" + installLegacyStatus.get()
                        + " msg=" + msg);
                if (installLegacyStatus.get() == INSTALL_FAILED_UPDATE_INCOMPATIBLE_COMPAT
                        || (msg != null && msg.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE"))) {
                    rtn = -11;
                } else {
                    rtn = -12;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rtn = -1;

        } finally {
            try {
                sContext.unregisterReceiver(receiver);
            } catch (Exception ignored) {
            }
            if (session != null) {
                try {
                    session.close();
                } catch (Exception ignored) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    rtn = -2;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    rtn = -3;
                }
            }
        }

        return rtn;
    }
    public boolean isAppInstalled(String packageName) {
        try {
            sContext.getPackageManager().getPackageInfo(packageName, 0);
            return true; // 如果找到应用程序包名，说明应用已安装
        } catch (PackageManager.NameNotFoundException e) {
            return false; // 如果找不到应用程序包名，说明应用未安装
        }
    }

    public String GetApkPkgName(String AppPath){

        PackageManager pm = sContext.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(AppPath, 0);
        if (packageInfo != null) {
            return packageInfo.packageName;
        }
        return null;

    }

    static String TAGS = "## [KO] ApkControl";
}
