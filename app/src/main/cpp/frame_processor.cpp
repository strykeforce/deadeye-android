#include <opencv2/imgproc.hpp>
#include "frame_processor.h"

#include "log.h"

using namespace deadeye;

FrameProcessor::FrameProcessor(
        JNIEnv *env,
        int feedback_tex,
        int width,
        int height) :
        feedback_tex_(static_cast<GLuint>(feedback_tex)),
        width_(width),
        height_(height),
        min_(100, 0, 0),
        max_(200, 255, 255) {

    LOGI("FrameProcessor: size %dx%d", width_, height_);
    jobject ref = env->NewDirectByteBuffer((void *) &data_, sizeof(Data));
    byte_buffer_ = env->NewGlobalRef(ref);
}

void FrameProcessor::MinThreshold(jint hue, jint sat, jint val) {
    min_[0] = hue;
    min_[1] = sat;
    min_[2] = val;
    LOGD("Min Threshold: H = %f, S = %f, V = %f", min_[0], min_[1], min_[2]);
}

void FrameProcessor::MaxThreshold(jint hue, jint sat, jint val) {
    max_[0] = hue;
    max_[1] = sat;
    max_[2] = val;
    LOGD("Max Threshold: H = %f, S = %f, V = %f", max_[0], max_[1], max_[2]);
}

void FrameProcessor::process() {
    static cv::Mat frame;

    // allocate frame storage and load pixels from current frame buffer
    // on entry, we assume frame buffer is bound
    frame.create(height_, width_, CV_8UC4);
    glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, frame.data);

    cv::circle(frame, cv::Point(320, 240), 40, min_, 3);

    // sleep 20ms
    struct timespec tim, tim2;
    tim.tv_sec = 0;
    tim.tv_nsec = 20 * 1000000L;
    nanosleep(&tim, &tim2);

    // return data
    data_.latency = 0;
    for (int i = 0; i < 4; ++i) {
        data_.values[i] = counter_++;
    }

    // bind texture to sampler and update texture with annotated frame
    // on return, renderer will assume texture is bound
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, feedback_tex_);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE,
                    frame.data);
}

jobject FrameProcessor::getData() {
    return byte_buffer_;
}

void FrameProcessor::releaseData(JNIEnv *env) {
    env->DeleteGlobalRef(byte_buffer_);
}
