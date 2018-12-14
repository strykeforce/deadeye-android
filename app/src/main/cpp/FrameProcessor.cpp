#include <opencv2/imgproc.hpp>
#include <fstream>
#include "FrameProcessor.h"
#include "json.hpp"
#include "log.h"

using namespace deadeye;
using json = nlohmann::json;

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

    // Put results in data_ struct for return. Java calling method FrameProcessor getData
    // will overwrite the latency field with its own computed value.
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

    static int count = 0;
    switch (contours_mode_) {
        case 0:
            count = 0;
            break;
        case 1:
            break;
        case 2:
            if (count++ == 0) DumpContours();
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

// Return the ByteBuffer that was initialized in our constructor. This will be reused to pass
// data back to the FrameProcessor class.
jobject FrameProcessor::getData() {
    return byte_buffer_;
}

void FrameProcessor::releaseData(JNIEnv *env) {
    env->DeleteGlobalRef(byte_buffer_);
}

void FrameProcessor::DumpContours() {
    auto contours = *pipeline_.GetFindContoursOutput();

    json j;
    for (auto contour : contours) {
        auto size = contour.size();
        if (size < 2) continue;

        json contour_obj;

        contour_obj["size"] = contour.size();
        auto area = cv::contourArea(contour);
        contour_obj["area"] = area;
        contour_obj["arclength"] = cv::arcLength(contour, true);
        auto bb = cv::boundingRect(contour);
        contour_obj["width"] = bb.width;
        contour_obj["height"] = bb.height;
        contour_obj["ratio"] = bb.width / bb.height;

        std::vector<cv::Point> hull;
        cv::convexHull(cv::Mat(contour, true), hull);
        auto solid = 100 * area / cv::contourArea(hull);
        contour_obj["solid"] = solid;

        json point_ary;

        for (cv::Point point : contour) {
            json p;
            p["x"] = point.x;
            p["y"] = point.y;
            point_ary.push_back(p);
        }
        contour_obj["points"] = point_ary;
        j.push_back(contour_obj);
    }

    auto file_name = "/storage/emulated/0/deadeye.json";
    FILE *file = fopen(file_name, "w+");
    if (file != NULL) {
        LOGI("Contours dump file open for writing");
        fputs(j.dump(4).c_str(), file);
        fclose(file);
        LOGI("DUMP JSON to %s", file_name);
    } else
        LOGE("Unable to open contours dump file: %s", strerror(errno));

//    std::ofstream outfile;
//    outfile.open("/sdcard/deadeye.json", std::ios_base::out | std::ios_base::ate);
//    outfile << j.dump() << std::endl;
//    LOGD("DUMP JSON out = %d", out);
}