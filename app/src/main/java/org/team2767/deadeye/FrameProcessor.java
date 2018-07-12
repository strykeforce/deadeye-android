package org.team2767.deadeye;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class FrameProcessor {

    static {
        System.loadLibrary("deadeye");
    }

    private final long objPtr;
    private final ByteBuffer data;

    FrameProcessor(int outputTex, int width, int height) {
        objPtr = init(outputTex, width, height);
        data = data(objPtr);
        data.order(ByteOrder.nativeOrder());
    }

    byte[] getBytes(int latency) {
        data.putInt(0, latency);
        data.rewind();
        byte[] dest = new byte[data.capacity()];
        data.get(dest);
        return dest;
    }

    void setMinThreshold(int hue, int sat, int val) {
        minThreshold(objPtr, hue, sat, val);
    }

    void setMaxThreshold(int hue, int sat, int val) {
        maxThreshold(objPtr, hue, sat, val);
    }

    void process() {
        process(objPtr);
    }

    void release() {
        release(objPtr);
    }


    private native long init(int outputTex, int width, int height);

    private native ByteBuffer data(long cppObjPtr);

    private native void process(long cppObjPtr);

    private native void release(long cppObjPtr);

    private native void minThreshold(long cppObjPtr, int hue, int sat, int val);

    private native void maxThreshold(long cppObjPtr, int hue, int sat, int val);
}
