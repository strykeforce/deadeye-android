package org.team2767.deadeye;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.team2767.deadeye.di.Injector;

public class DeadeyeActivity extends AppCompatActivity {

    private final static String TAG = "DeadeyeActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private GLSurfaceView deadeyeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        deadeyeView = Injector.get().deadeyeView();
        super.onCreate(savedInstanceState);
        setContentView(deadeyeView);

        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
        Log.d(TAG, stringFromJNI());
        Log.d(TAG, "onCreate() finished");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
