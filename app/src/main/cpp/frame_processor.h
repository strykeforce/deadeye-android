#pragma once

#include <GLES2/gl2.h>
#include <opencv2/core/types.hpp>

namespace deadeye {
    class FrameProcessor {
    public:
        FrameProcessor(
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

    private:
        GLuint feedback_tex_;
        int width_, height_;
        cv::Scalar min_, max_;
    };
}
