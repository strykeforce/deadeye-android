package org.team2767.deadeye;

import android.os.Environment;
import android.util.Pair;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
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
    data.order(Network.BYTE_ORDER); // Android is always LITTLE_ENDIAN
    Timber.d("byte order is %s", data.order());

    // initialized saved HSV threshold settings
    Pair<Integer, Integer> range = settings.getHueRange();
    setHueRange(range.first, range.second);
    range = settings.getSaturationRange();
    setSaturationRange(range.first, range.second);
    range = settings.getValueRange();
    setValueRange(range.first, range.second);
  }

  public static void dumpFrameData(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(Network.BYTE_ORDER);
    String type = String.format("0x%08X", buffer.getInt());
    String latency = Integer.toString(buffer.getInt());
    double[] data = new double[4];
    for (int i = 0; i < 4; i++) {
      data[i] = buffer.getDouble();
    }
  }

  // After calling process(), the data ByteBuffer contains results.
  byte[] getBytes(int latency) {
    data.rewind();
    data.putInt(Network.TYPE_FRAME_DATA);
    data.putInt(latency);
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

    String state = Environment.getExternalStorageState();
    if (!state.equals(Environment.MEDIA_MOUNTED))
      throw new IllegalStateException("external media: " + state);

    File downloadDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File out = new File(downloadDir, "deadeye.json");

    Disposable d =
        Completable.fromCallable(
                () -> {
                  try (Writer writer = new FileWriter(out)) {
                    writer.write(json);
                  }
                  return null;
                })
            .subscribeOn(Schedulers.io())
            .subscribe(
                () -> Timber.i("contours written to: %s (%d)", out, json.length()), Timber::e);
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
    CAMERA(0),
    MASK(1);
    final int code;

    Monitor(int code) {
      this.code = code;
    }
  }

  public enum Contours {
    NONE(0),
    TARGET(1),
    CONTOURS(2);
    final int code;

    Contours(int code) {
      this.code = code;
    }
  }
}
