package org.ping.cool.Local.runnable;


import static org.ping.cool.Local.async.ScanHostsAsyncTask.TAG;

import android.util.Log;

import org.ping.cool.Local.response.MainAsyncResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ScanHostsRunnable implements Runnable {
    private String[] ipParts;
    private int start;
    private int stop;
    private MainAsyncResponse delegate;

    //Constructor to set the necessary data to scan for hosts
    public ScanHostsRunnable(String[] ipParts, int start, int stop, MainAsyncResponse delegate) {
        this.ipParts = ipParts;
        this.start = start;
        this.stop = stop;
        this.delegate = delegate;
    }

    //Starts the host discovery
    @Override
    public void run() {
        for (int i = this.start; i <= this.stop; i++) {
            String ip = this.ipParts[0] + "." + this.ipParts[1] + "." + this.ipParts[2] + "." + i;
            Socket socket = new Socket();
            socket.setPerformancePreferences(1, 0, 0);

            try {
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(ip, 7), 150);
                socket.close();
            } catch (IOException ignored) {
            } finally {
                this.delegate.processFinish(1);
            }
        }
    }
}