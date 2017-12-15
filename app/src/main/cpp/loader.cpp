#include <jni.h>

#include <opencv2/opencv.hpp>

#include "frame_processor.h"
#include "hello.h"
#include "log.h"

using namespace deadeye;

static const char *className = "org/team2767/deadeye/FrameProcessor";

extern "C" JNICALL
jlong init(
        JNIEnv *env,
        jobject,
        jint ouputTex,
        jint width,
        jint height,
        jint hue_min,
        jint hue_max,
        jint sat_min,
        jint sat_max,
        jint val_min,
        jint val_max
) {
    LOGD("Initializing native FrameProcessor");
    return reinterpret_cast<jlong>(new FrameProcessor(ouputTex, width, height, hue_min, hue_max,
                                                      sat_min, sat_max, val_min, val_max));
}

extern "C" JNICALL
void release(JNIEnv *env, jobject, jlong pointer) {
    LOGD("Releasing native FrameProcessor");
    FrameProcessor *frameProcessor = reinterpret_cast<FrameProcessor *>(pointer);
    delete frameProcessor;
}

extern "C" JNICALL
void process(JNIEnv *env, jobject, jlong pointer) {
    FrameProcessor *frameProcessor = reinterpret_cast<FrameProcessor *>(pointer);
    frameProcessor->process();
}

static JNINativeMethod methods[] = {
        {"stringFromJNI", "()Ljava/lang/String;", (void *) ohai},
        {"init",          "(IIIIIIIII)J",         (void *) init},
        {"process",       "(J)V",                 (void *) process},
        {"release",       "(J)V",                 (void *) release},
};

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_ERR;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        LOGE("Native registration failed for class '%s'", className);
        return JNI_ERR;
    }

    LOGD("Done with JNI_OnLoad");

    return JNI_VERSION_1_6;
}

