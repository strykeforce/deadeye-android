package org.team2767.deadeye;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.team2767.deadeye.di.Injector;

import timber.log.Timber;

/**
 * Deadeye main view.
 */
public class DeadeyeView extends GLSurfaceView {

    public DeadeyeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);

        DeadeyeRenderer renderer = Injector.get().deadeyeRendererFactory().create(this);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        Timber.tag("LifeCycles");
        Timber.d("DeadeyeView constructed");

    }

    public DeadeyeView(Context context) {
        this(context, null);
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
