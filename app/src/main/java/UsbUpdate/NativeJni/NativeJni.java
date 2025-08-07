package UsbUpdate.NativeJni;

public class NativeJni {

    static {
        System.loadLibrary("usb_update");
    }

    public static native int DetectUSB();
    public static native void DoChmod(String dir);

}
