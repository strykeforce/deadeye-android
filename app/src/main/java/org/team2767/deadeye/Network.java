package org.team2767.deadeye;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.strykeforce.thirdcoast.deadeye.DeadeyeMessage.Type.HEARTBEAT;
import static org.team2767.deadeye.Network.ConnectionEvent.CONNECTED;
import static org.team2767.deadeye.Network.ConnectionEvent.DISCONNECTED;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;
import io.reactivex.subjects.PublishSubject;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.strykeforce.thirdcoast.deadeye.DeadeyeMessage;
import org.strykeforce.thirdcoast.deadeye.rx.RxBus;
import org.strykeforce.thirdcoast.deadeye.rx.RxUdp;
import timber.log.Timber;

@Singleton
public class Network {

  private static final int PORT = 5555;
  private static final String INTERFACE = "rndis0";
  private static final int TETHER_CHECK_SEC = 5;
  private static final String TETHER_COMMAND = "su -c service call connectivity 30 i32 1";
  private static final int PING_INTERVAL = 100;
  private static final int PING_LIMIT = 400;
  private final PublishSubject<byte[]> visionData = PublishSubject.create();
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();
  private final RxBus rxBus;

  @Inject
  public Network(RxBus rxBus) {
    this.rxBus = rxBus;
  }

  private static Iterable<NetworkInterface> networkInterfaces() {
    try {
      return Collections.list(NetworkInterface.getNetworkInterfaces());
    } catch (SocketException e) {
      Timber.e(e);
    }
    return Collections.emptyList();
  }

  public void start() {
    Timber.i("starting network connections");
    // periodically check for tethering network interface and call tether command if missing
    Disposable disposable =
        Observable.interval(TETHER_CHECK_SEC, SECONDS)
            .flatMapMaybe(
                i ->
                    Maybe.fromCallable(() -> NetworkInterface.getByName(INTERFACE))
                        .doOnComplete(() -> Runtime.getRuntime().exec(TETHER_COMMAND)))
            .ignoreElements()
            .subscribe(() -> Timber.w("Tether check exited"), Timber::e);
    compositeDisposable.add(disposable);

    // listen for UDP packets on given port
    Observable<DatagramPacket> packetObservable = RxUdp.observableReceivingFrom(PORT).share();

    // extract remote address from incoming UDP packets
    Observable<SocketAddress> addressObservable =
        packetObservable
            .map(DatagramPacket::getAddress)
            .distinctUntilChanged()
            .doOnNext(a -> Timber.i("Robot address = %s", a.getHostAddress()))
            .map(a -> new InetSocketAddress(a, PORT));

    // create pongs to send to remote address
    Observable<DatagramPacket> pongObservable =
        addressObservable.map(a -> new DatagramPacket(DeadeyeMessage.HEARTBEAT_BYTES, 1, a));

    // stream pings
    Observable<DeadeyeMessage> pingObservable =
        packetObservable
            .map(DatagramPacket::getData)
            .map(DeadeyeMessage::new)
            .filter(deadeyeMessage -> deadeyeMessage.type == HEARTBEAT);

    // send pong when ping arrives
    disposable =
        pingObservable
            .withLatestFrom(pongObservable, (ping, pong) -> pong)
            .subscribeWith(RxUdp.observerSendingDatagramPacket());
    compositeDisposable.add(disposable);

    // monitor pings
    Observable<Timed<Long>> heartbeat =
        Observable.interval(PING_INTERVAL / 2, MILLISECONDS).timestamp(MILLISECONDS);

    disposable =
        Observable.combineLatest(
                pingObservable.timestamp(MILLISECONDS), heartbeat, (p, h) -> h.time() - p.time())
            .distinctUntilChanged(time -> time > PING_LIMIT)
            .map(time -> time > PING_LIMIT ? DISCONNECTED : CONNECTED)
            .startWith(DISCONNECTED)
            .subscribe(rxBus::send, Timber::e);
    compositeDisposable.add(disposable);

    disposable = rxBus.asFlowable().map(Object::toString).subscribe(Timber::d, Timber::e);
    compositeDisposable.add(disposable);

    // send frame analysis data
    disposable =
        visionData
            .observeOn(Schedulers.io())
            .withLatestFrom(
                addressObservable,
                (bytes, address) -> new DatagramPacket(bytes, bytes.length, address))
            .subscribeWith(RxUdp.observerSendingDatagramPacket());
    compositeDisposable.add(disposable);

    Timber.i("started network connections");
  }

  public void stop() {
    compositeDisposable.clear();
    Timber.i("stopped network connections");
  }

  public PublishSubject<byte[]> getVisionDataSubject() {
    return visionData;
  }

  public enum ConnectionEvent {
    CONNECTED,
    DISCONNECTED
  }
}
