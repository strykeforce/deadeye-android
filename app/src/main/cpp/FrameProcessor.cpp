#include <opencv2/imgproc.hpp>
#include "FrameProcessor.h"

#include "log.h"

using namespace deadeye;

FrameProcessor::FrameProcessor(
        JNIEnv *env,
        int feedback_tex,
        int width,
        int height) :
        monitor_tex_(static_cast<GLuint>(feedback_tex)),
        width_(width),
        height_(height) {
    jobject ref = env->NewDirectByteBuffer((void *) &data_, sizeof(Data));
    byte_buffer_ = env->NewGlobalRef(ref);
    source_.create(height_, width_, CV_8UC4);
    LOGI("FrameProcessor: size %dx%d", width_, height_);
}

void FrameProcessor::HueRange(int low, int high) {
    pipeline_.hsvThresholdHue[0] = static_cast<double>(low);
    pipeline_.hsvThresholdHue[1] = static_cast<double>(high);
    LOGD("HUE low = %.0f, high = %.0f", pipeline_.hsvThresholdHue[0], pipeline_.hsvThresholdHue[1]);
}

void FrameProcessor::SaturationRange(int low, int high) {
    pipeline_.hsvThresholdSaturation[0] = static_cast<double>(low);
    pipeline_.hsvThresholdSaturation[1] = static_cast<double>(high);
    LOGD("SAT low = %.0f, high = %.0f", pipeline_.hsvThresholdSaturation[0],
         pipeline_.hsvThresholdSaturation[1]);
}

void FrameProcessor::ValueRange(int low, int high) {
    pipeline_.hsvThresholdValue[0] = static_cast<double>(low);
    pipeline_.hsvThresholdValue[1] = static_cast<double>(high);
    LOGD("VAL low = %.0f, high = %.0f", pipeline_.hsvThresholdValue[0],
         pipeline_.hsvThresholdValue[1]);
}

void FrameProcessor::MonitorMode(int code) {
    monitor_mode_ = code;
    LOGD("MONITOR state = %d", monitor_mode_);
}

void FrameProcessor::ContoursMode(int code) {
    contours_mode_ = code;
    LOGD("CONTOURS state = %d", contours_mode_);
}

void FrameProcessor::process() {

    // allocate frame storage and load pixels from current frame buffer
    // on entry, we assume frame buffer is bound
    glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, source_.data);

    pipeline_.Process(source_);

    // return data
    data_.latency = 0;
    for (int i = 0; i < 4; ++i) {
        data_.values[i] = counter_++;
    }

    cv::Mat monitor;

    switch (monitor_mode_) {
        case 0:
            monitor = source_;
            break;
        case 1:
            cv::cvtColor(*pipeline_.GetHsvThresholdOutput(), monitor, CV_GRAY2RGBA);
            break;
        default:
            LOGE("Unrecognized monitor mode = %d", monitor_mode_);
    }

    switch (contours_mode_) {
        case 0:
            break;
        case 1:
            break;
        case 2:
            cv::drawContours(monitor, *pipeline_.GetFindContoursOutput(), -1, cv::Scalar(255, 0, 0),
                             1);
            break;
        default:
            LOGE("Unrecognized contours mode = %d", contours_mode_);
    }

    // bind texture to sampler and update texture with annotated frame
    // on return, renderer will assume texture is bound
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, monitor_tex_);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE,
                    monitor.data);
}

jobject FrameProcessor::getData() {
    return byte_buffer_;
}

void FrameProcessor::releaseData(JNIEnv *env) {
    env->DeleteGlobalRef(byte_buffer_);
}
