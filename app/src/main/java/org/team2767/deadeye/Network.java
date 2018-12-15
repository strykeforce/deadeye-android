package org.team2767.deadeye;

import org.team2767.deadeye.rx.RxUdp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.SECONDS;

@Singleton
public class Network {

  public static final int TYPE_FRAME_DATA = 0xDEADDA7A;
  public static final int TYPE_PING = 0xDEADBACC;
  public static final int TYPE_PONG = 0xDEADCCAB;
  public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

  private static final int PORT = 5555;
  private static final String INTERFACE = "rndis0";
  private static final int TETHER_CHECK_SEC = 5;
  private static final String TETHER_COMMAND = "su -c service call connectivity 30 i32 1";
  private static final byte[] PING =
      ByteBuffer.allocate(4).order(Network.BYTE_ORDER).putInt(TYPE_PING).array();
  private static final byte[] PONG =
      ByteBuffer.allocate(4).order(Network.BYTE_ORDER).putInt(TYPE_PONG).array();
  private static final int SIZE = 4;
  private static final int PONG_LIMIT = 400;
  private final PublishSubject<byte[]> visionData = PublishSubject.create();
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  @Inject
  public Network() {}

  //  private static void debugDatagramPacket(DatagramPacket p) {
  //    byte[] b = Arrays.copyOf(p.getData(), p.getLength());
  //    debugByteArray(b);
  //  }
  //
  //  private static void debugByteBuffer(ByteBuffer b) {
  //    b.rewind();
  //    byte[] bytes = new byte[b.remaining()];
  //    b.get(bytes);
  //    debugByteArray(bytes);
  //  }
  //
  //  private static void debugByteArray(byte[] b) {
  //    Timber.d("Bytes = %s", Arrays.toString(b));
  //  }

  //    private Observable

  private static boolean isPing(DatagramPacket packet) {
    byte[] data = packet.getData();
    //    Timber.d("type = %s, PING = %s", new String(Arrays.copyOf(data, 4)),
    // Integer.toString(TYPE_PING));
    //    return type == TYPE_PING;
    for (int i = 0; i < SIZE; i++) if (data[i] != PING[i]) return false;
    return true;
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
    Timber.i("Starting Network connections");

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

    // listen for messages on given port
    Observable<DatagramPacket> packets = RxUdp.observableFrom(PORT).share();

    // extract remote address from incoming messages
    Observable<SocketAddress> addresses =
        packets
            .map(DatagramPacket::getAddress)
            .distinctUntilChanged()
            .doOnNext(a -> Timber.i("Robot address = %s", a.getHostAddress()))
            .map(a -> new InetSocketAddress(a, PORT));

    // create pongs to send to remote address
    Observable<DatagramPacket> pongs = addresses.map(a -> new DatagramPacket(PONG, SIZE, a));

    // send pong when ping arrives
    disposable =
        packets
            //            .doOnNext(p -> Timber.d("Packet = %s", new
            // String(Arrays.copyOf(p.getData(), 4))))
            .filter(Network::isPing)
            .withLatestFrom(pongs, (ping, pong) -> pong)
            .subscribeWith(RxUdp.datagramPacketObserver());
    compositeDisposable.add(disposable);

    // send frame analysis data
    disposable =
        visionData
            .observeOn(Schedulers.io())
            .withLatestFrom(
                addresses, (bytes, address) -> new DatagramPacket(bytes, bytes.length, address))
            .subscribeWith(RxUdp.datagramPacketObserver());
    compositeDisposable.add(disposable);
  }

  public void stop() {
    Timber.w("Stopping Network connections");
    compositeDisposable.clear();
  }

  public PublishSubject<byte[]> getVisionDataSubject() {
    return visionData;
  }

  public enum ConnectionEvent {
    CONNECTED,
    DISCONNECTED
  }
}
