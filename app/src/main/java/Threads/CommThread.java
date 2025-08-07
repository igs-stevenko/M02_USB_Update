package Threads;

import android.content.Context;
import android.os.SystemProperties;

public class CommThread {

    private Context sContext;

    public void Start() {
        new Thread(new Runnable() {

            @Override
            public void run() {

            }
        }).start();
    }

    static String TAGS = "## [KO] CommThread";
}
