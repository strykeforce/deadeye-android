package org.team2767.deadeye;

import android.os.Environment;
import android.util.Pair;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import timber.log.Timber;

@AutoFactory
class FrameProcessor {

    static {
        System.loadLibrary("deadeye");
    }

    private final long objPtr;
    private final ByteBuffer data; // direct ByteBuffer, created in native FrameProcessor ctor
    private final Settings settings;

    FrameProcessor(int outputTex, int width, int height, @Provided Settings settings) {
        this.settings = settings;
        objPtr = init(outputTex, width, height);
        data = data(objPtr);
        data.order(ByteOrder.nativeOrder());

        // initialized saved HSV threshold settings
        Pair<Integer, Integer> range = settings.getHueRange();
        setHueRange(range.first, range.second);
        range = settings.getSaturationRange();
        setSaturationRange(range.first, range.second);
        range = settings.getValueRange();
        setValueRange(range.first, range.second);
    }

    // After calling process(), the data ByteBuffer contains results.
    byte[] getBytes(int latency) {
        data.putInt(0, latency);
        data.rewind();
        byte[] dest = new byte[data.capacity()];
        data.get(dest);
        return dest;
    }

    void setHueRange(int low, int high) {
        hueRange(objPtr, low, high);
        settings.setHueRange(low, high);
    }

    void setSaturationRange(int low, int high) {
        satRange(objPtr, low, high);
        settings.setSaturationRange(low, high);
    }

    void setValueRange(int low, int high) {
        valRange(objPtr, low, high);
        settings.setValueRange(low, high);
    }

    private void dumpContours(String json) {
        Timber.d("CONTOURS:\n" + json);

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Timber.e("External media is not available for dumping contours, state: %s",
                    Environment.getExternalStorageState());
            return;
        }

        File out = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "deadeye.json");

        try (Writer writer = new FileWriter(out)) {
            writer.write(json);
        } catch (IOException e) {
            Timber.e(e);
            return;
        }
        Timber.i("Contours written to: %s", out);
    }

    void setMonitor(Monitor monitor) {
        monitor(objPtr, monitor.code);
    }

    void setContours(Contours contours) {
        contours(objPtr, contours.code);
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

    private native void hueRange(long cppObjPtr, int low, int high);

    private native void satRange(long cppObjPtr, int low, int high);

    private native void valRange(long cppObjPtr, int low, int high);

    private native void monitor(long cppObjPtr, int code);

    private native void contours(long cppObjPtr, int code);

    public enum Monitor {
        CAMERA(0), MASK(1);
        final int code;

        Monitor(int code) {
            this.code = code;
        }
    }

    public enum Contours {
        NONE(0), TARGET(1), CONTOURS(2);
        final int code;

        Contours(int code) {
            this.code = code;
        }
    }
}
