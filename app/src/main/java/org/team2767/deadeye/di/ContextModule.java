package org.team2767.deadeye.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger dependency-inject module for Application context.
 */

@Module
public class ContextModule {

    private final Context appContext;

    public ContextModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    public Context appContext() {
        return appContext;
    }
}
