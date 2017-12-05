package org.team2767.deadeye.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Dagger dependency-injection component.
 */

@Component(modules = {
        ContextModule.class
})
@Singleton
public interface SingletonComponent {

    Context appContext();
}
