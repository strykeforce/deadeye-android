package org.team2767.deadeye;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;

import org.team2767.deadeye.FrameProcessor.Contours;
import org.team2767.deadeye.FrameProcessor.Monitor;
import org.team2767.deadeye.di.Injector;
import org.team2767.deadeye.rx.RxBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import io.reactivex.Flowable;
import timber.log.Timber;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static org.team2767.deadeye.FrameProcessor.Contours.CONTOURS;
import static org.team2767.deadeye.FrameProcessor.Contours.NONE;
import static org.team2767.deadeye.FrameProcessor.Contours.TARGET;
import static org.team2767.deadeye.FrameProcessor.Monitor.CAMERA;
import static org.team2767.deadeye.FrameProcessor.Monitor.MASK;

public class DeadeyeActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final static int REQUEST_CAMERA_PERMISSION = 2767;
    private final static String FRAGMENT_DIALOG = "dialog";
    @BindView(R.id.deadeyeView)
    DeadeyeView deadeyeView;
    @BindView(R.id.sample_text)
    TextView tv;
    @BindView(R.id.hsvLayout)
    View hsvLayout;
    @BindView(R.id.hueRangeBar)
    RangeBar hueRangeBar;
    @BindView(R.id.satRangeBar)
    RangeBar satRangeBar;
    @BindView(R.id.valRangeBar)
    RangeBar valRangeBar;
    @BindView(R.id.monitorButton)
    Button monitorButton;
    @BindView(R.id.contoursButton)
    Button contoursButton;
    private Network network;
    private boolean hsvControlsEnabled;
    private boolean hsvControlsInitialized;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        network = Injector.get().network();

        RxBus bus = Injector.get().bus();
        Flowable<Object> connEventEmitter = bus.asFlowable();

        connEventEmitter
//                .ofType(Network.ConnectionEvent.class)
                .subscribe(event -> Timber.i("XXXXXXX" + event.toString()));


        getWindow().setFlags(FLAG_KEEP_SCREEN_ON, FLAG_KEEP_SCREEN_ON); // ...and bright

        setContentView(R.layout.activity_deadeye);
        ButterKnife.bind(this);
        setHsvRangeBarListeners();
        monitorButton.setText(MASK.toString());
        contoursButton.setText(TARGET.toString());

        tv.setText("OHAI");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        network.start();
        enableHsvControls(false);

    }

    @Override
    protected void onStop() {
        network.stop();
        super.onStop();
    }

    // buttons
    @OnClick(R.id.monitorButton)
    public void monitorButton(Button button) {
        Monitor nextState = Monitor.valueOf(monitorButton.getText().toString());
        Timber.d("monitor new state = %s", nextState);
        deadeyeView.setMonitor(nextState);
        switch (nextState) {
            case CAMERA:
                monitorButton.setText(MASK.toString());
                break;
            case MASK:
                monitorButton.setText(CAMERA.toString());
                break;
        }
    }

    @OnClick(R.id.contoursButton)
    public void contoursButton() {
        Contours nextState = Contours.valueOf(contoursButton.getText().toString());
        Timber.d("contours new state = %s", nextState);
        deadeyeView.setContour(nextState);
        switch (nextState) {
            case NONE:
                contoursButton.setText(TARGET.toString());
                break;
            case TARGET:
                contoursButton.setText(CONTOURS.toString());
                break;
            case CONTOURS:
                contoursButton.setText(NONE.toString());
                break;
        }
    }

    @DebugLog
    @OnClick(R.id.hsvButton)
    public void hsvButton() {
        enableHsvControls(!hsvControlsEnabled);
    }

    private void enableHsvControls(boolean enabled) {
        hsvLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);
        hsvControlsEnabled = enabled;
        if (hsvControlsInitialized || !hsvControlsEnabled) return;

        Settings settings = Injector.get().settings();
        Pair<Integer, Integer> range = settings.getHueRange();
        hueRangeBar.setRangePinsByIndices(range.first, range.second);
        range = settings.getSaturationRange();
        satRangeBar.setRangePinsByIndices(range.first, range.second);
        range = settings.getValueRange();
        valRangeBar.setRangePinsByIndices(range.first, range.second);
        hsvControlsInitialized = true;
    }

    // HSV range bars
    private void setHsvRangeBarListeners() {
        hueRangeBar.setOnRangeBarChangeListener(
                (rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> deadeyeView.setHueRange(leftPinIndex, rightPinIndex)
        );
        satRangeBar.setOnRangeBarChangeListener(
                (rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> deadeyeView.setSaturationRange(leftPinIndex, rightPinIndex)
        );
        valRangeBar.setOnRangeBarChangeListener(
                (rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> deadeyeView.setValueRange(leftPinIndex, rightPinIndex)
        );

    }

    // camera permissions

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // user said "nope" or revoked permissions in Settings, so explain...
            new ConfirmationDialog().show(getFragmentManager(), FRAGMENT_DIALOG);
        } else {
            // standard permission dialog
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.denied_permission))
                        .show(getFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    // explain that we really do need the camera
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok,
                            (dialog, which) -> activity.requestPermissions(
                                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION))
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        if (activity != null) {
                            activity.finish();
                        }
                    })
                    .create();
        }
    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> activity.finish())
                    .create();
        }

    }


}
