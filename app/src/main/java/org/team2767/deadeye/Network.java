package org.team2767.deadeye;

import org.team2767.deadeye.rx.RxBus;
import org.team2767.deadeye.rx.RxUdp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class Network {

    private final static int PORT = 5555;
    private final static String INTERFACE = "rndis0";
    private final static String TETHERING = "Tethering";
    private final static String TETHER_COMMAND = "su -c service call connectivity 30 i32 1";
    private final static String PING = "ping";
    private final static byte[] PONG = "pong".getBytes();
    private final static int PONG_SZ = PONG.length;
    private final static int PONG_LIMIT = 400;
    private final RxBus bus;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

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

    private static Iterable<NetworkInterface> networkInterfaces() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            Timber.e(e);
        }
        return Collections.emptyList();
    }

    @DebugLog
    public void start() {
        Timber.i("Starting Network connections");

        // periodically check for tethering network interface and call tether command if missing
        Disposable disposable = Observable.interval(5, TimeUnit.SECONDS)
                .flatMapSingle(i -> Observable.fromIterable(networkInterfaces())
                        .map(NetworkInterface::getName)
                        .filter(INTERFACE::equals)
                        .singleOrError()
                        .onErrorResumeNext(Single.fromCallable(() -> {
                            Runtime.getRuntime().exec(TETHER_COMMAND);
                            return TETHERING;
                        }))
                )
                .filter(TETHERING::equals)
                .subscribe(Timber::i, Timber::e);
        compositeDisposable.add(disposable);

        // listen for messages on given port
        Observable<DatagramPacket> packets = RxUdp.observableFrom(PORT).share();

        // extract remote address from incoming messages
        Observable<SocketAddress> addresses = packets
                .map(DatagramPacket::getAddress)
                .distinctUntilChanged()
                .doOnNext(a -> Timber.i("Robot address = %s", a.getHostAddress()))
                .map(a -> new InetSocketAddress(a, PORT));

        // create pongs to send to remote address
        Observable<DatagramPacket> pongs = addresses.map(a -> new DatagramPacket(PONG, PONG_SZ, a));

        // send pong when ping arrives
        disposable = packets
                .filter(Network::isPing)
                .withLatestFrom(pongs, (ping, pong) -> pong)
                .subscribeWith(RxUdp.datagramPacketObserver());
        compositeDisposable.add(disposable);
    }

    @DebugLog
    public void stop() {
        Timber.w("Stopping Network connections");
        compositeDisposable.clear();
    }

    public enum ConnectionEvent {
        CONNECTED,
        DISCONNECTED
    }

}
