package org.team2767.deadeye;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
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
    monitorButton.setText(CAMERA.toString());
    contoursButton.setText(NONE.toString());

    tv.setText("OHAI");

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
      new AlertDialog.Builder(this)
          .setMessage(
              "Grant permissions in Settings → Apps → Deadeye → Permissions and restart app!")
          .setPositiveButton("OK", null)
          .create()
          .show();
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
    Monitor currentState = Monitor.valueOf(monitorButton.getText().toString());
    Monitor nextState;
    switch (currentState) {
      case CAMERA:
        nextState = MASK;
        break;
      case MASK:
        nextState = CAMERA;
        break;
      default:
        nextState = CAMERA;
    }
    deadeyeView.setMonitor(nextState);
    monitorButton.setText(nextState.toString());
  }

  @OnClick(R.id.contoursButton)
  public void contoursButton() {
    Contours currentState = Contours.valueOf(contoursButton.getText().toString());
    Contours nextState;
    switch (currentState) {
      case NONE:
        nextState = TARGET;
        break;
      case TARGET:
        nextState = CONTOURS;
        break;
      case CONTOURS:
        nextState = NONE;
        break;
      default:
        nextState = NONE;
    }
    deadeyeView.setContour(nextState);
    contoursButton.setText(nextState.toString());
  }

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
        (rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) ->
            deadeyeView.setHueRange(leftPinIndex, rightPinIndex));
    satRangeBar.setOnRangeBarChangeListener(
        (rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) ->
            deadeyeView.setSaturationRange(leftPinIndex, rightPinIndex));
    valRangeBar.setOnRangeBarChangeListener(
        (rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) ->
            deadeyeView.setValueRange(leftPinIndex, rightPinIndex));
  }
}
