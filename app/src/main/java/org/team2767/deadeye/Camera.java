package org.team2767.deadeye;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Represents camera used for target detection.
 */
public class Camera {

    final static int WIDTH = 640;
    final static int HEIGHT = 480;

    private final Size PREVIEW_SIZE = new Size(WIDTH, HEIGHT);

    private final Context context;
    private final Semaphore openCloseLock = new Semaphore(1);
    private final ConcurrentLinkedQueue<TimeStamp> captureTimeStamps = new ConcurrentLinkedQueue<>();
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private SurfaceTexture surfaceTexture;
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Timber.d("Open state callback for %s", cameraDevice);
            openCloseLock.release();
            Camera.this.cameraDevice = cameraDevice;
            createCameraPreviewSession(surfaceTexture);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Timber.w("Disconnect state callback for %s", cameraDevice);
            openCloseLock.release();
            cameraDevice.close();
            Camera.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            Timber.wtf("Error state callback for %s (%d)", cameraDevice, error);
            openCloseLock.release();
            cameraDevice.close();
            Camera.this.cameraDevice = null;
        }

    };

    /**
     * Constructor.
     *
     * @param context the Application context.
     */
    @Inject
    public Camera(Context context) {
        this.context = context;
    }

    /**
     * Start camera capture to this SurfaceTexture.
     *
     * @param surfaceTexture the SurfaceTexture to use.
     */
    void start(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
        startBackgroundThread();
        open();
    }

    /**
     * Stop camera capture.
     */
    void stop() {
        closeCamera();
        stopBackgroundThread();
    }

    /**
     * Find TimeStamp record using the frame metadata timestamp and calculate latency.
     *
     * @param captureTimeStamp the timestamp from camera capture metadata
     * @return the latency in milliseconds
     */
    int latencyForFrameWithTimeStamp(long captureTimeStamp) {
        for (; ; ) {
            TimeStamp ts = captureTimeStamps.poll();
            if (ts == null) {
                Timber.w("captureTimeStamps queue was empty, setting latency = 0");
                return 0;
            }
            if (ts.captureTimeStamp == captureTimeStamp) {
                return ts.latency();
            }
        }
    }

    private void open() {
        CameraManager manager = context.getSystemService(CameraManager.class);
        assert manager != null;
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                for (Size size : map.getOutputSizes(SurfaceTexture.class)) {
                    Timber.v("preview size = %s", size);
                }


                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                assert focalLengths != null;
                if (focalLengths.length != 1) {
                    Timber.e("More than one focal length supported");
                }
                Timber.d("Camera focal length: %f mm", focalLengths[0]);

                SizeF sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                Timber.d("Sensor size: %s mm", sensorSize);
                assert sensorSize != null;
                double widthDim = sensorSize.getWidth();
                double heightDim = sensorSize.getHeight();

                Rect activeArrayCoords = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                assert activeArrayCoords != null;
                Timber.d("Active array size: %s", activeArrayCoords.toShortString());
                int widthPixelDimActual = activeArrayCoords.width();
                Timber.d("Active array width: %d pixels", widthPixelDimActual);

                Size totalArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
                assert totalArraySize != null;
                Timber.d("Pixel array size: %s", totalArraySize);
                int widthPixelDimSensor = totalArraySize.getWidth();
                double widthRatio = (double) widthPixelDimActual / (double) widthPixelDimSensor;
                widthDim *= widthRatio;
                Timber.d("Actual width: %f mm", widthDim);
                int heightPixelDimActual = activeArrayCoords.height();
                Timber.d("Active array height: %d pixels", heightPixelDimActual);
                int heightPixelDimSensor = totalArraySize.getHeight();
                double heightRatio = (double) heightPixelDimActual / (double) heightPixelDimSensor;
                heightDim *= heightRatio;
                Timber.d("Actual height: %f mm", heightDim);

                // We now know how large the effective imager is, but depending on the capture aspect
                // ratio, this will be letterboxed or cropped.
                double horizontalPixelSize = widthDim / WIDTH;
                double verticalPixelSize = heightDim / HEIGHT;
                Timber.d("Horizontal pixel size is: %f mm/pixel", horizontalPixelSize);
                Timber.d("Vertical pixel size is: %f mm/pixel", verticalPixelSize);
                if (horizontalPixelSize > verticalPixelSize) {
                    widthDim = verticalPixelSize * WIDTH;
                    Timber.d("Cropping width to %f mm", widthDim);
                } else if (verticalPixelSize > horizontalPixelSize) {
                    heightDim = horizontalPixelSize * HEIGHT;
                    Timber.d("Cropping height to %f mm", heightDim);
                }

                double focalLengthPixels = WIDTH * focalLengths[0] / widthDim;
//                mView.setFocalLengthPixels(focalLengthPixels);
                Timber.d("Camera focal length: %f pixels", focalLengthPixels);
                Timber.d("Camera horizontal FOV %f deg", 2 * Math.toDegrees(Math.atan(.5 * widthDim / focalLengths[0])));
                Timber.d("Camera vertical FOV %f deg", 2 * Math.toDegrees(Math.atan(.5 * heightDim / focalLengths[0])));


                Timber.d("using camera: %s", cameraId);

                try {
                    if (!openCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                        throw new RuntimeException("Time out waiting to lock camera opening.");
                    }
                    manager.openCamera(cameraId, mStateCallback, backgroundHandler);

                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while trying to lock camera opening");
                } catch (CameraAccessException | SecurityException e) {
                    Timber.e(e);
                }
            }
        } catch (CameraAccessException e) {
            Timber.e(e, "Error reading camera characteristics");
        }
    }

    /**
     * Close the current CameraDevice.
     */
    private void closeCamera() {
        try {
            openCloseLock.acquire();
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            openCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its Handler.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its Handler.
     */
    private void stopBackgroundThread() {
        if (backgroundThread == null) {
            return;
        }
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            Timber.e(e);
        }
    }

    @DebugLog
    private void createCameraPreviewSession(SurfaceTexture surfaceTexture) {
        int w = PREVIEW_SIZE.getWidth(), h = PREVIEW_SIZE.getHeight();
        Timber.d("createCameraPreviewSession(%dx%d)", w, h);
        if (w < 0 || h < 0)
            return;
        try {
            openCloseLock.acquire();
            if (cameraDevice == null) {
                openCloseLock.release();
                Timber.e("createCameraPreviewSession: camera isn't opened");
                return;
            }
            if (captureSession != null) {
                openCloseLock.release();
                Timber.e("createCameraPreviewSession: captureSession is already started");
                return;
            }
            if (surfaceTexture == null) {
                openCloseLock.release();
                Timber.e("createCameraPreviewSession: preview SurfaceTexture is null");
                return;
            }
            surfaceTexture.setDefaultBufferSize(w, h);

            Surface surface = new Surface(surfaceTexture);

            CaptureRequest.Builder previewRequestBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession captureSession) {
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF);
                        previewRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
                        previewRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
                        previewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 1 * 1_000_000L); // ns
                        previewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, .2f);
                        captureSession.setRepeatingRequest(previewRequestBuilder.build(),
                                new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                                                 @NonNull CaptureRequest request,
                                                                 long timestamp, long frameNumber) {
                                        captureTimeStamps.add(new TimeStamp(timestamp));
                                    }
                                },
                                backgroundHandler);
                        Timber.d("CameraPreviewSession has been started");
                    } catch (CameraAccessException e) {
                        Timber.e(e, "createCaptureSession failed");
                    }
                    openCloseLock.release();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Timber.e("createCameraPreviewSession failed");
                    openCloseLock.release();
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            Timber.e(e, "createCameraPreviewSession failed");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while createCameraPreviewSession", e);
        }
    }

    /**
     * Correlate camera frame metadata timestamp with system time.
     */
    final static class TimeStamp {
        final long captureTimeStamp;
        final long systemTimeStamp;

        TimeStamp(long frame) {
            this.captureTimeStamp = frame;
            systemTimeStamp = System.nanoTime();
        }

        int latency() {
            return (int) ((System.nanoTime() - systemTimeStamp) / 1000000L); // in milliseconds
        }
    }
}
