#pragma once

#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d.hpp>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <map>
#include <vector>
#include <string>
#include <math.h>

namespace grip {

/**
* A representation of the different types of blurs that can be used.
*
*/
    enum BlurType {
        BOX, GAUSSIAN, MEDIAN, BILATERAL
    };

/**
* GripPipeline class.
* 
* An OpenCV pipeline generated by GRIP.
*/
    class GripPipeline {
    private:
        cv::Mat blurOutput;
        cv::Mat hsvThresholdOutput;
        cv::Mat cvDilateOutput;
        cv::Mat cvErodeOutput;
        std::vector<std::vector<cv::Point> > findContoursOutput;
        std::vector<std::vector<cv::Point> > filterContoursOutput;

        void blur(cv::Mat &, BlurType &, double, cv::Mat &);

        void hsvThreshold(cv::Mat &, double [], double [], double [], cv::Mat &);

        void cvDilate(cv::Mat &, cv::Mat &, cv::Point &, double, int, cv::Scalar &, cv::Mat &);

        void cvErode(cv::Mat &, cv::Mat &, cv::Point &, double, int, cv::Scalar &, cv::Mat &);

        void findContours(cv::Mat &, bool, std::vector<std::vector<cv::Point> > &);

        void filterContours(std::vector<std::vector<cv::Point> > &, double, double, double, double,
                            double, double, double [], double, double, double, double,
                            std::vector<std::vector<cv::Point> > &);


    public:
        GripPipeline();

        void Process(cv::Mat &source0);

        cv::Mat *GetBlurOutput();

        cv::Mat *GetHsvThresholdOutput();

        cv::Mat *GetCvDilateOutput();

        cv::Mat *GetCvErodeOutput();

        std::vector<std::vector<cv::Point> > *GetFindContoursOutput();

        std::vector<std::vector<cv::Point> > *GetFilterContoursOutput();

        // added manually
        double hsvThresholdHue[2];
        double hsvThresholdSaturation[2];
        double hsvThresholdValue[2];

    };


} // end namespace grip


