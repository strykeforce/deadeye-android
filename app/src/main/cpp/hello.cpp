#include "hello.h"

#include <string>

#include "log.h"

using namespace deadeye;

extern "C"
jstring JNICALL ohai(JNIEnv *env, jobject) {
    std::string hello = "OHAI from C++";
    LOGD("OHAI");
    return env->NewStringUTF(hello.c_str());
}
