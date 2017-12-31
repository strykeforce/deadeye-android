package org.team2767.deadeye.rx;

import java.net.NetworkInterface;
import java.util.Collections;

import io.reactivex.Completable;
import timber.log.Timber;

public class RxTether {

    public static Completable startTether() {
        return Completable.fromAction(() -> {
            for (NetworkInterface net : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                Timber.v("network = %s", net.getName());
            }
        });
    }
}
