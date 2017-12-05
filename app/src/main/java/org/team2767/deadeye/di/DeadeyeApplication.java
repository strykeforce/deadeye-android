package org.team2767.deadeye.di;

import android.app.Application;

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
    }
}
