package org.team2767.deadeye;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import timber.log.Timber;

public class FrameProcessor {

    static {
        System.loadLibrary("deadeye");
    }

    private final long objPtr;
    private final ByteBuffer data;

    FrameProcessor(int outputTex, int width, int height, int hueMin, int hueMax,
                   int satMin, int satMax, int valMin, int valMax) {
        objPtr = init(outputTex, width, height, hueMin, hueMax, satMin, satMax, valMin, valMax);
        data = data(objPtr);
        data.order(ByteOrder.nativeOrder());
    }

    ByteBuffer getByteBuffer() {
        return data;
    }

    byte[] getBytes() {
        data.rewind();
        byte[] dest = new byte[data.capacity()];
        data.get(dest);
        return dest;
    }

    void process() {
        process(objPtr);
    }

    void release() {
        release(objPtr);
    }


    private native long init(int outputTex, int width, int height, int hueMin, int hueMax,
                             int satMin, int satMax, int valMin, int valMax);

    private native ByteBuffer data(long cppObjPtr);

    private native void process(long cppObjPtr);

    private native void release(long cppObjPtr);
}
