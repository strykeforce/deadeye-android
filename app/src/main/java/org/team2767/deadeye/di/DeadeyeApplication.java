package org.team2767.deadeye.di;

import android.app.Application;

import org.team2767.deadeye.BuildConfig;

import timber.log.Timber;

/**
 * Application initializes the Dagger dependency-injection {@code SingletonComponent}.
 */

public class DeadeyeApplication extends Application {

    static DeadeyeApplication INSTANCE;
    SingletonComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerSingletonComponent.builder()
                .contextModule(new ContextModule(this))
                .build();
        INSTANCE = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.DebugTree()); // FIXME: configure release logging
            Timber.w("Release logging not configured!");
        }
    }
}
