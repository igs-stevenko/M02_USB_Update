// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("usb_update");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("usb_update")
//      }
//    }

#include <jni.h>
#include <string>
#include <android/log.h>
#include "inc/native_usb_update.h"


extern "C"
JNIEXPORT jint JNICALL
Java_NativeJni_NativeJni_DetectUSB(JNIEnv *env, jclass thiz) {
    int rtn = 0;

    rtn = need_update();

    return rtn;
}



extern "C"
JNIEXPORT void JNICALL
Java_NativeJni_NativeJni_DoChmod(JNIEnv *env, jclass thiz, jstring javaString) {
    // TODO: implement DoChmod()
    const char *cString = (*env).GetStringUTFChars(javaString, 0);
    __android_log_print(ANDROID_LOG_INFO, "## [KO] JNI", "cString : %s", cString);
    do_chmod(cString);
}