package org.team2767.deadeye.di;

/**
 * Convenience class to get global singleton Dagger component.
 */
public class Injector {

    private Injector() {}

    public static SingletonComponent get() {
        return DeadeyeApplication.INSTANCE.component;
    }
}
