#include <jni.h>

#include <opencv2/opencv.hpp>

#include "FrameProcessor.h"
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
void hueRange(JNIEnv *, jobject, jlong pointer, jint low, jint high) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->HueRange(low, high);
}

extern "C" JNICALL
void satRange(JNIEnv *, jobject, jlong pointer, jint low, jint high) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->SaturationRange(low, high);
}

extern "C" JNICALL
void valRange(JNIEnv *, jobject, jlong pointer, jint low, jint high) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->ValueRange(low, high);
}

extern "C" JNICALL
void monitor(JNIEnv *, jobject, jlong pointer, jint code) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->MonitorMode(code);
}

extern "C" JNICALL
void contours(JNIEnv *, jobject, jlong pointer, jint code) {
    FrameProcessor *fp = reinterpret_cast<FrameProcessor *>(pointer);
    fp->ContoursMode(code);
}

static JNINativeMethod methods[] = {
        {"init",     "(III)J",                   (void *) init},
        {"data",     "(J)Ljava/nio/ByteBuffer;", (void *) data},
        {"process",  "(J)V",                     (void *) process},
        {"release",  "(J)V",                     (void *) release},
        {"hueRange", "(JII)V",                   (void *) hueRange},
        {"satRange", "(JII)V",                   (void *) satRange},
        {"valRange", "(JII)V",                   (void *) valRange},
        {"monitor",  "(JI)V",                    (void *) monitor},
        {"contours", "(JI)V",                    (void *) contours},
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

