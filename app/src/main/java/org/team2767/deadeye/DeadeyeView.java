package org.team2767.deadeye;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import org.team2767.deadeye.di.Injector;

import javax.inject.Inject;

/**
 * Deadeye main view.
 */
public class DeadeyeView extends GLSurfaceView {

    private final static String TAG = "DeadeyeView";

//    private final DeadeyeRenderer renderer;

    public DeadeyeView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        this.renderer = Injector.get().deadeyeRendererFactory().create(this);

        setEGLContextClientVersion(2);

        setRenderer(Injector.get().deadeyeRendererFactory().create(this));
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        Log.d(TAG, "constructor finished");

    }

    public DeadeyeView(Context context) {
        this(context, null);

    }
}
