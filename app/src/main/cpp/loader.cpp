#include <jni.h>

#include <opencv2/opencv.hpp>

#include "frame_processor.h"
#include "log.h"

using namespace deadeye;

static const char *className = "org/team2767/deadeye/FrameProcessor";

extern "C" JNICALL
jlong init(
        JNIEnv *env,
        jobject,
        jint ouputTex,
        jint width,
        jint height
) {
    LOGD("Initializing native FrameProcessor");
    FrameProcessor *fp = new FrameProcessor(env, ouputTex, width, height);
    return reinterpret_cast<jlong>(fp);
}

extern "C" JNICALL
void release(JNIEnv *env, jobject, jlong pointer) {
    LOGD("Releasing native FrameProcessor");
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->releaseData(env);
    delete fp;
}

extern "C" JNICALL
jobject data(JNIEnv *, jobject, jlong pointer) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    return fp->getData();
}


extern "C" JNICALL
void process(JNIEnv *, jobject, jlong pointer) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->process();
}

extern "C" JNICALL
void minThreshold(JNIEnv *, jobject, jlong pointer, jint hue, jint sat, jint val) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->MinThreshold(hue, sat, val);
}

extern "C" JNICALL
void maxThreshold(JNIEnv *, jobject, jlong pointer, jint hue, jint sat, jint val) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->MaxThreshold(hue, sat, val);
}

static JNINativeMethod methods[] = {
        {"init",         "(III)J",                   (void *) init},
        {"data",         "(J)Ljava/nio/ByteBuffer;", (void *) data},
        {"process",      "(J)V",                     (void *) process},
        {"release",      "(J)V",                     (void *) release},
        {"minThreshold", "(JIII)V",                  (void *) minThreshold},
        {"maxThreshold", "(JIII)V",                  (void *) maxThreshold},
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

