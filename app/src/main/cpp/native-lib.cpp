#include <jni.h>
#include <string>

#include "log.h"
extern "C"
JNIEXPORT jstring

JNICALL
Java_org_team2767_deadeye_DeadeyeActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    LOGD("OHAI");
    return env->NewStringUTF(hello.c_str());
}
