package org.team2767.deadeye.rx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RxUdp {

    private final static int BUF_SZ = 512;

    private RxUdp() {}

    public static DisposableObserver<DatagramPacket> datagramPacketObserver() {
        return new DisposableObserver<DatagramPacket>() {
            DatagramSocket socket;

            @Override
            protected void onStart() {
                try {
                    Timber.d("Initializing datagramPacketObserver socket");
                    socket = new DatagramSocket();
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onNext(DatagramPacket datagramPacket) {
                try {
                    socket.send(datagramPacket);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {
                Timber.w("Completed");
            }
        };
    }

    public static Observable<DatagramPacket> observableFrom(int port) {
        return Observable.<DatagramPacket>create(
                e -> {
                    Timber.d("Create observableFrom");
                    DatagramSocket socket = new DatagramSocket(port);
                    e.setCancellable(
                            () -> {
                                socket.close();
                                Timber.d("Closed socket listening on port %d", port);
                            });
                    while (!e.isDisposed()) {
                        DatagramPacket packet = new DatagramPacket(new byte[BUF_SZ], BUF_SZ);
                        try {
                            socket.receive(packet);
                        } catch (IOException ioe) {
                            if (socket.isClosed()) {
                                e.onComplete();
                                break;
                            } else {
                                e.onError(ioe);
                                break;
                            }
                        }
                        e.onNext(packet);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

}
