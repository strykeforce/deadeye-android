package org.team2767.deadeye;

import org.team2767.deadeye.rx.RxBus;
import org.team2767.deadeye.rx.RxUdp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import timber.log.Timber;

public class Network {

    private final static int PORT = 5555;
    private final static String PING = "ping";
    private final static String PONG = "pong";
    private final static int PONG_LIMIT = 400;
    private final RxBus bus;

    @Inject
    public Network(RxBus bus) {
        this.bus = bus;
    }

//    private Observable

    @DebugLog
    public void start() {
        Observable<DatagramPacket> packets = RxUdp.observableFrom(PORT).publish().autoConnect();

        packets.take(1)
                .map(DatagramPacket::getAddress)
                .map(a -> new InetSocketAddress(a, PORT))
                .doOnNext(sa -> Timber.i("discovered robot socket address = %s", sa))
                .subscribe(
                        sa -> packets
                                .map(p -> new String(p.getData(), 0, p.getLength()))
                                .filter(s -> s.equals(PING))
                                .map(s -> PONG)
                                .subscribe(RxUdp.observerTo(sa)),
                        Timber::e
                );
    }

    public enum ConnectionEvent {
        CONNECTED,
        DISCONNECTED
    }

}
