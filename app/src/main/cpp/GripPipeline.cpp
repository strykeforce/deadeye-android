
#include "GripPipeline.h"

namespace grip {

    GripPipeline::GripPipeline() {
    }

/**
* Runs an iteration of the pipeline and updates outputs.
*/
    void GripPipeline::Process(cv::Mat &source) {
        //Step HSV_Threshold0:
        //input
        cv::Mat hsvThresholdInput = source;
        // MANUALLY UPDATED - move hsvThreshold local vars to public members
        hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue,
                     this->hsvThresholdOutput);
        //Step Find_Contours0:
        //input
        cv::Mat findContoursInput = hsvThresholdOutput;
        bool findContoursExternalOnly = true;  // default Boolean
        findContours(findContoursInput, findContoursExternalOnly, this->findContoursOutput);
        //Step Filter_Contours0:
        //input
        std::vector<std::vector<cv::Point> > filterContoursContours = findContoursOutput;
        double filterContoursMinArea = 500.0;  // default Double
        double filterContoursMinPerimeter = 0.0;  // default Double
        double filterContoursMinWidth = 0.0;  // default Double
        double filterContoursMaxWidth = 1000.0;  // default Double
        double filterContoursMinHeight = 0.0;  // default Double
        double filterContoursMaxHeight = 1000.0;  // default Double
        double filterContoursSolidity[] = {60.263653483992464, 100};
        double filterContoursMaxVertices = 1000000.0;  // default Double
        double filterContoursMinVertices = 0.0;  // default Double
        double filterContoursMinRatio = 0.0;  // default Double
        double filterContoursMaxRatio = 1000.0;  // default Double
        filterContours(filterContoursContours, filterContoursMinArea, filterContoursMinPerimeter,
                       filterContoursMinWidth, filterContoursMaxWidth, filterContoursMinHeight,
                       filterContoursMaxHeight, filterContoursSolidity, filterContoursMaxVertices,
                       filterContoursMinVertices, filterContoursMinRatio, filterContoursMaxRatio,
                       this->filterContoursOutput);

        if (this->filterContoursOutput.size() == 0) {
            bounding_rect = cv::Rect(0, 0, 20, 10);
            for (int i = 0; i < 4; ++i) {
                values[i] = 0.0;
            }
            return;
        }

        std::sort(this->filterContoursOutput.begin(), this->filterContoursOutput.end(),
                  [](std::vector<cv::Point> const &a, std::vector<cv::Point> const &b) {
                      return cv::arcLength(a, true) > cv::arcLength(b, true);
                  });

        bounding_rect = cv::boundingRect(this->filterContoursOutput.front());

        values[0] = bounding_rect.x;
        values[1] = bounding_rect.y;
        values[2] = bounding_rect.height;
        values[3] = bounding_rect.width;
    }

/**
 * This method is a generated getter for the output of a Blur.
 * @return Mat output from Blur.
 */
    cv::Mat *GripPipeline::GetBlurOutput() {
        return &(this->blurOutput);
    }

/**
 * This method is a generated getter for the output of a HSV_Threshold.
 * @return Mat output from HSV_Threshold.
 */
    cv::Mat *GripPipeline::GetHsvThresholdOutput() {
        return &(this->hsvThresholdOutput);
    }

/**
 * This method is a generated getter for the output of a CV_dilate.
 * @return Mat output from CV_dilate.
 */
    cv::Mat *GripPipeline::GetCvDilateOutput() {
        return &(this->cvDilateOutput);
    }

/**
 * This method is a generated getter for the output of a CV_erode.
 * @return Mat output from CV_erode.
 */
    cv::Mat *GripPipeline::GetCvErodeOutput() {
        return &(this->cvErodeOutput);
    }

/**
 * This method is a generated getter for the output of a Find_Contours.
 * @return ContoursReport output from Find_Contours.
 */
    std::vector<std::vector<cv::Point> > *GripPipeline::GetFindContoursOutput() {
        return &(this->findContoursOutput);
    }

/**
 * This method is a generated getter for the output of a Filter_Contours.
 * @return ContoursReport output from Filter_Contours.
 */
    std::vector<std::vector<cv::Point> > *GripPipeline::GetFilterContoursOutput() {
        return &(this->filterContoursOutput);
    }

    /**
     * Softens an image using one of several filters.
     *
     * @param input The image on which to perform the blur.
     * @param type The blurType to perform.
     * @param doubleRadius The radius for the blur.
     * @param output The image in which to store the output.
     */
    void GripPipeline::blur(cv::Mat &input, BlurType &type, double doubleRadius, cv::Mat &output) {
        int radius = (int) (doubleRadius + 0.5);
        int kernelSize;
        switch (type) {
            case BOX:
                kernelSize = 2 * radius + 1;
                cv::blur(input, output, cv::Size(kernelSize, kernelSize));
                break;
            case GAUSSIAN:
                kernelSize = 6 * radius + 1;
                cv::GaussianBlur(input, output, cv::Size(kernelSize, kernelSize), radius);
                break;
            case MEDIAN:
                kernelSize = 2 * radius + 1;
                cv::medianBlur(input, output, kernelSize);
                break;
            case BILATERAL:
                cv::bilateralFilter(input, output, -1, radius, radius);
                break;
        }
    }

    /**
     * Segment an image based on hue, saturation, and value ranges.
     *
     * @param input The image on which to perform the HSL threshold.
     * @param hue The min and max hue.
     * @param sat The min and max saturation.
     * @param val The min and max value.
     * @param output The image in which to store the output.
     */
    void GripPipeline::hsvThreshold(cv::Mat &input, double hue[], double sat[], double val[],
                                    cv::Mat &out) {
        cv::cvtColor(input, out, cv::COLOR_RGBA2BGR); // THIS IS MANUALLY CHANGED
        cv::inRange(out, cv::Scalar(hue[0], sat[0], val[0]), cv::Scalar(hue[1], sat[1], val[1]),
                    out);
    }

    /**
     * Expands area of higher value in an image.
     * @param src the Image to dilate.
     * @param kernel the kernel for dilation.
     * @param anchor the center of the kernel.
     * @param iterations the number of times to perform the dilation.
     * @param borderType pixel extrapolation method.
     * @param borderValue value to be used for a constant border.
     * @param dst Output Image.
     */
    void GripPipeline::cvDilate(cv::Mat &src, cv::Mat &kernel, cv::Point &anchor, double iterations,
                                int borderType, cv::Scalar &borderValue, cv::Mat &dst) {
        cv::dilate(src, dst, kernel, anchor, (int) iterations, borderType, borderValue);
    }

    /**
     * Expands area of lower value in an image.
     * @param src the Image to erode.
     * @param kernel the kernel for erosion.
     * @param anchor the center of the kernel.
     * @param iterations the number of times to perform the erosion.
     * @param borderType pixel extrapolation method.
     * @param borderValue value to be used for a constant border.
     * @param dst Output Image.
     */
    void GripPipeline::cvErode(cv::Mat &src, cv::Mat &kernel, cv::Point &anchor, double iterations,
                               int borderType, cv::Scalar &borderValue, cv::Mat &dst) {
        cv::erode(src, dst, kernel, anchor, (int) iterations, borderType, borderValue);
    }

    /**
     * Finds contours in an image.
     *
     * @param input The image to find contours in.
     * @param externalOnly if only external contours are to be found.
     * @param contours vector of contours to put contours in.
     */
    void GripPipeline::findContours(cv::Mat &input, bool externalOnly,
                                    std::vector<std::vector<cv::Point> > &contours) {
        std::vector<cv::Vec4i> hierarchy;
        contours.clear();
        int mode = externalOnly ? cv::RETR_EXTERNAL : cv::RETR_LIST;
        int method = cv::CHAIN_APPROX_SIMPLE;
        cv::findContours(input, contours, hierarchy, mode, method);
    }

    /**
     * Filters through contours.
     * @param inputContours is the input vector of contours.
     * @param minArea is the minimum area of a contour that will be kept.
     * @param minPerimeter is the minimum perimeter of a contour that will be kept.
     * @param minWidth minimum width of a contour.
     * @param maxWidth maximum width.
     * @param minHeight minimum height.
     * @param maxHeight  maximimum height.
     * @param solidity the minimum and maximum solidity of a contour.
     * @param minVertexCount minimum vertex Count of the contours.
     * @param maxVertexCount maximum vertex Count.
     * @param minRatio minimum ratio of width to height.
     * @param maxRatio maximum ratio of width to height.
     * @param output vector of filtered contours.
     */
    void GripPipeline::filterContours(std::vector<std::vector<cv::Point> > &inputContours,
                                      double minArea, double minPerimeter, double minWidth,
                                      double maxWidth, double minHeight, double maxHeight,
                                      double solidity[], double maxVertexCount,
                                      double minVertexCount, double minRatio, double maxRatio,
                                      std::vector<std::vector<cv::Point> > &output) {
        std::vector<cv::Point> hull;
        output.clear();
        for (std::vector<cv::Point> contour: inputContours) {
            cv::Rect bb = boundingRect(contour);
            if (bb.width < minWidth || bb.width > maxWidth) continue;
            if (bb.height < minHeight || bb.height > maxHeight) continue;
            double area = cv::contourArea(contour);
            if (area < minArea) continue;
            if (arcLength(contour, true) < minPerimeter) continue;
            cv::convexHull(cv::Mat(contour, true), hull);
            double solid = 100 * area / cv::contourArea(hull);
            if (solid < solidity[0] || solid > solidity[1]) continue;
            if (contour.size() < minVertexCount || contour.size() > maxVertexCount) continue;
            double ratio = (double) bb.width / (double) bb.height;
            if (ratio < minRatio || ratio > maxRatio) continue;
            output.push_back(contour);
        }
    }

} // end grip namespace

