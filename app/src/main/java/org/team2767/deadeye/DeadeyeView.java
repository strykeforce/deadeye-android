package org.team2767.deadeye;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.inject.Inject;

/**
 * Deadeye main view.
 */
public class DeadeyeView extends GLSurfaceView {

    private final static String TAG = "DeadeyeView";

    private final DeadeyeRenderer renderer;

    @Inject
    public DeadeyeView(Context context, DeadeyeRendererFactory rendererFactory) {
        super(context);

        // inject and use a factory to break the dependency-injection cycle with renderer
        this.renderer = rendererFactory.create(this);

        setEGLContextClientVersion(2);

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        Log.d(TAG, "constructor finished");
    }
}
