package org.team2767.deadeye;

import org.team2767.deadeye.rx.RxBus;
import org.team2767.deadeye.rx.RxTether;
import org.team2767.deadeye.rx.RxUdp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class Network {

    private final static int PORT = 5555;
    private final static String PING = "ping";
    private final static byte[] PONG = "pong".getBytes();
    private final static int PONG_SZ = PONG.length;
    private final static int PONG_LIMIT = 400;
    private final RxBus bus;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable disposable;
    private Observable<DatagramPacket> packets;

    @Inject
    public Network(RxBus bus) {
        this.bus = bus;
    }

//    private Observable

    private static boolean isPing(DatagramPacket packet) {
        byte[] data = packet.getData();
        if (data.length < 4) {
            return false;
        }
        return data[0] == (byte) 'p' && data[1] == (byte) 'i';
    }

    @DebugLog
    public void start() {
        RxTether.startTether().subscribe();

        // listen for messages on given port
        packets = RxUdp.observableFrom(PORT).share();

        // extract remote address from incoming messages
        Observable<SocketAddress> addresses = packets
                .map(DatagramPacket::getAddress)
                .distinctUntilChanged()
                .doOnNext(a -> Timber.i("Robot address = %s", a.getHostAddress()))
                .map(a -> new InetSocketAddress(a, PORT));

        // create pongs to send to remote address
        Observable<DatagramPacket> pongs = addresses.map(a -> new DatagramPacket(PONG, PONG_SZ, a));

        // send pong when ping arrives
        Disposable d = packets
                .filter(Network::isPing)
                .withLatestFrom(pongs, (ping, pong) -> pong)
                .subscribeWith(RxUdp.datagramPacketObserver());
        compositeDisposable.add(d);
    }

    @DebugLog
    public void stop() {
        Timber.w("Stopping Network connections");
        compositeDisposable.clear();
        packets = null;
    }

    public enum ConnectionEvent {
        CONNECTED,
        DISCONNECTED
    }

}
