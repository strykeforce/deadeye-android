package org.team2767.deadeye;

import android.support.annotation.NonNull;

public class FrameProcessor {

    static {
        System.loadLibrary("deadeye");
    }

    private final long objPtr;

    FrameProcessor(int outputTex, int width, int height, int hueMin, int hueMax,
                          int satMin, int satMax, int valMin, int valMax) {
        objPtr = init(outputTex, width, height, hueMin, hueMax, satMin, satMax, valMin, valMax);
    }

    @NonNull
    public native String stringFromJNI();

    void process() {
        process(objPtr);
    }

    void release() {
        release(objPtr);
    }


    private native long init(int outputTex, int width, int height, int hueMin, int hueMax,
                             int satMin, int satMax, int valMin, int valMax);

    private native void process(long cppObjPtr);

    private native void release(long cppObjPtr);
}
