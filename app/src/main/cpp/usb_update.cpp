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
#include <asm-generic/fcntl.h>
#include <fcntl.h>
#include <unistd.h>
#include "inc/native_usb_update.h"
#include "inc/sysinfo_ioctl.h"
#include <linux/ioctl.h>


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


extern "C"
JNIEXPORT jstring JNICALL
Java_com_igs_usb_1update_FirstStartActivity_GeySysInfo(JNIEnv *env, jobject thiz) {
    // TODO: implement GeySysInfo()

    __android_log_print(ANDROID_LOG_INFO, "## [KO] JNI", "TestJNI");

    struct system_info sysinfo_st;
    int fd = 0;
    int rtn = 0;

    fd = open("/dev/system_info", O_RDONLY);
    if(fd <= 0){
        return env->NewStringUTF("");
    }

    rtn = ioctl(fd, GET_SYSINFO, &sysinfo_st);
    if(rtn != 0){
        return env->NewStringUTF("");
    }

    if(fd)  close(fd);

    std::string ProjName(reinterpret_cast<const char*>(sysinfo_st.proj_name), strlen((const char *)sysinfo_st.proj_name));

    return env->NewStringUTF(ProjName.c_str());
}