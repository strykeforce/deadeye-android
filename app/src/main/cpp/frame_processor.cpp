#include <opencv2/core/mat.hpp>
#include "frame_processor.h"

#include "log.h"

using namespace deadeye;

FrameProcessor::FrameProcessor(
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
}

void FrameProcessor::process() {
    static cv::Mat frame;
    frame.create(height_, width_, CV_8UC4);
    glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, frame.data);

    static cv::Mat feedback;
    feedback = frame;

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, feedback_tex_);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE,
                    feedback.data);
}
