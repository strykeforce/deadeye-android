#include <opencv2/imgproc.hpp>
#include "frame_processor.h"

#include "log.h"

using namespace deadeye;

FrameProcessor::FrameProcessor(
        JNIEnv *env,
        int feedback_tex,
        int width,
        int height,
        int hue_min,
        int hue_max,
        int sat_min,
        int sat_max,
        int val_min,
        int val_max) :
        feedback_tex_(static_cast<GLuint>(feedback_tex)),
        width_(width),
        height_(height),
        min_(hue_min, sat_min, val_min),
        max_(hue_max, sat_max, val_max) {

    LOGI("FrameProcessor: size %dx%d, H %.0f-%.0f, S %.0f-%.0f, V %.0f-%.0f",
         width_, height_, min_[0], max_[0], min_[1], max_[1], min_[2], max_[2]);
    jobject ref = env->NewDirectByteBuffer((void *) &data_, sizeof(Data));
    byte_buffer_ = env->NewGlobalRef(ref);
}

void FrameProcessor::process() {
    static cv::Mat frame;

    // allocate frame storage and load pixels from current frame buffer
    // on entry, we assume frame buffer is bound
    frame.create(height_, width_, CV_8UC4);
    glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, frame.data);

    cv::circle(frame, cv::Point(320, 240), 40, cv::Scalar(244, 226, 66), 3);

    // return data
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
