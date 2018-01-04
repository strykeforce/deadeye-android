#pragma once

#include <GLES2/gl2.h>
#include <opencv2/core/types.hpp>
#include <jni.h>

namespace deadeye {
    class FrameProcessor {
    public:
        FrameProcessor(
                JNIEnv *env,
                int feedback_tex,
                int width,
                int height,
                int hue_min,
                int hue_max,
                int sat_min,
                int sat_max,
                int val_min,
                int val_max
        );

        void process();

        jobject getData();

        void releaseData(JNIEnv *env);

    private:
        struct Data {
            jint latency;
            jint reserved; // struct 64-bit member alignment
            jdouble values[4];
        };


        GLuint feedback_tex_;
        int width_, height_;
        cv::Scalar min_, max_;
        Data data_;
        jobject byte_buffer_;
        int counter_;
    };
}
