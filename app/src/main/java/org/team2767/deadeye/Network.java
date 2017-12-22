package org.team2767.deadeye;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Network {

    private final static int PORT = 5555;
    private final static String PING = "ping";
    private final static String PONG = "pong";
    private final static int PING_INTERVAL = 100;
    private final static int PONG_LIMIT = 400;
    private final SocketAddress ADDRESS = new InetSocketAddress("192.168.42.71", PORT);
    private final RxBus bus;
    private final DatagramSocket socket;

    @Inject
    public Network(RxBus bus) {
        this.bus = bus;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            Timber.e(e);
            throw new RuntimeException(e);
        }
    }

    private static Observable<String> messageObserver(int port) {
        return Observable.create(e -> {
            Timber.d("create called");
            DatagramSocket socket = new DatagramSocket(port);
            e.setCancellable(() -> {
                socket.close();
                Timber.d("closed socket");
            });
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            for (int i = 0; ; i++) {
                try {
                    socket.receive(packet);
                } catch (IOException ioe) {
                    if (socket.isClosed()) {
                        Timber.d("socket.isClosed()");
                        e.onComplete();
                        break;
                    } else {
                        e.onError(ioe);
                        break;
                    }
                }
                e.onNext(new String(packet.getData(), 0, packet.getLength()));
            }
        });
    }

    private Completable sendMessage(final String message) {
        return Completable.fromAction(() -> {
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, ADDRESS);
            socket.send(packet);
        });
    }

    @DebugLog
    public void start() {
        // send pings
        Observable.interval(PING_INTERVAL, TimeUnit.MILLISECONDS)
                .flatMapCompletable(i -> sendMessage(PING))
                .doOnError(Timber::e)
                .subscribe();

        // monitor pongs
        Observable<String> messageObserver = messageObserver(PORT).subscribeOn(Schedulers.io()).share();

        Observable<Timed<String>> pongs = messageObserver.filter(s -> s.equals(PONG))
                .timestamp(MILLISECONDS);
        Observable<Timed<Long>> heartbeat = Observable.interval(PING_INTERVAL / 2, MILLISECONDS)
                .timestamp(MILLISECONDS);

        Observable.combineLatest(pongs, heartbeat, (p, h) -> h.time() - p.time())
//                .doOnNext(time -> Timber.d("pong time interval = %d ms", time))
                .distinctUntilChanged(time -> time > PONG_LIMIT)
                .map(time -> time > PONG_LIMIT ? ConnectionEvent.DISCONNECTED : ConnectionEvent.CONNECTED)
                .subscribe(bus::send, Timber::e);
    }

    public enum ConnectionEvent {
        CONNECTED,
        DISCONNECTED
    }

}
