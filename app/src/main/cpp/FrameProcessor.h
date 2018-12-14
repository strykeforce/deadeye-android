#pragma once

#include <GLES2/gl2.h>
#include <opencv2/core/types.hpp>
#include <jni.h>
#include "GripPipeline.h"

namespace deadeye {
    class FrameProcessor {
    private:
        struct Data {
            jint latency;
            jint reserved; // struct 64-bit member alignment
            jdouble values[4];
        };


        GLuint monitor_tex_;
        int width_, height_;
        cv::Mat source_;
        grip::GripPipeline pipeline_;
        Data data_;
        jobject byte_buffer_;
        int counter_;
        int monitor_mode_ = 0;
        int contours_mode_ = 0;

        void DumpContours(JNIEnv *env, jobject obj);

    public:
        FrameProcessor(
                JNIEnv *env,
                int feedback_tex,
                int width,
                int height
        );

        void HueRange(int low, int high);

        void SaturationRange(int low, int high);

        void ValueRange(int low, int high);

        void MonitorMode(int code);

        void ContoursMode(int code);

        void process(JNIEnv *env, jobject obj);

        jobject getData();

        void releaseData(JNIEnv *env);

    };
}
