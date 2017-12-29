package org.team2767.deadeye.rx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RxUdp {

    private final static int BUF_SZ = 512;

    private final DatagramSocket socket;

    private RxUdp() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static Observer<String> observerTo(SocketAddress address) {
        return new UdpObserver(address);
    }

    public static Observable<DatagramPacket> observableFrom(int port) {
        return Observable.<DatagramPacket>create(
                e -> {
                    Timber.d("observableFor create called");
                    DatagramSocket socket = new DatagramSocket(port);
                    e.setCancellable(
                            () -> {
                                socket.close();
                                Timber.d("closed socket");
                            });
                    while (!e.isDisposed()) {
                        DatagramPacket packet = new DatagramPacket(new byte[BUF_SZ], BUF_SZ);
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
                        e.onNext(packet);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    private static class UdpObserver implements Observer<String> {
        private final SocketAddress address;
        private DatagramSocket socket;
        private Disposable sub;

        public UdpObserver(SocketAddress address) {
            Timber.d("create UDP send observer to address %s", address);
            this.address = address;
        }

        @Override
        @DebugLog
        public void onSubscribe(Disposable d) {
            sub = d;
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                Timber.e(e, "error creating socket for sending");
                sub.dispose();
            }
        }

        @Override
        public void onNext(String s) {
            Timber.d("sending '%s' to %s", s, address);
            byte[] buf = s.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
            try {
                socket.send(packet);
            } catch (IOException e) {
                Timber.e(e, "observableFor() error");
                //        sub.dispose();
            }
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "observableFor onError");
        }

        @Override
        public void onComplete() {
            Timber.i("observableFor onComplete");
        }
    }
}
