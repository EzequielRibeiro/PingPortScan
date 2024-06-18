/**
 * @author Lorenzo Vaccher
 * Copyright (c) 2021 Lorenzo Vaccher.
 */

package org.ping.cool.utils;

import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.ping.cool.PortAdapter;
import org.ping.cool.SecondFragment;
import org.ping.cool.utils.logger.Color;
import org.ping.cool.utils.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static org.ping.cool.CheckPortTask.SEPARATOR;
import static org.ping.cool.utils.logger.Logger.PutLogConsole;

/**
 * This class  is used for handling the connections
 * to the server and to check the ports.
 */
public class PortScanner {


    /**
     * Server's IP address that needs to be tested.
     */
    private String ip;

    /**
     * Number of Threads, and therefore the maximun number of
     * connections that can be established with the server
     * simultaneously.
     */
    private int threads = 200;

    /**
     * Time expressed in milliseconds that automatically
     * disconnects the Socket.
     */
    private int timeout = 400;

    /**
     * Inital port
     */
    private int portFrom = 1;

    /**
     * End port. If it is set to -1, it means that the user
     * has decided to verify only one port, which is saved
     * in the "portFrom" variable.
     */
    private int portTo = 65535;

    /**
     * List of all verified ports.
     */
    private List<Future<Port>> ports;

    public PortScanner(String ip) {
        setIp(ip);
    }

    /**
     * This method is used to start the scanning of the server.
     *
     * @param context
     * @param editTextTextLog
     */
    public void start(SecondFragment context, EditText editTextTextLog, ListView listView) {
        Logger.log("Start scanning " + getIp() + "...", Color.CYAN);
        PutLogConsole(context, editTextTextLog, "\nStart scanning " + getIp() + ".....");
        StringBuilder stringBuffer = new StringBuilder();
        int openPorts = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(getThreads());
        ports = new ArrayList<>();

        // Adding ports to the list that are verified.
        if (this.portTo != -1)
            for (int i = getPortFrom(); i <= getPortTo(); i++)
                ports.add(Port.scan(executorService, getIp(), i, getTimeout()));
        else
            ports.add(Port.scan(executorService, getIp(), getPortFrom(), getTimeout()));

        try {
            // This for loop verifies that all Threads have completed their task and load list
            ArrayList<Port> arrayOfPorts = new ArrayList<Port>();
            PortAdapter adapter = new PortAdapter(context.getContext(), arrayOfPorts);
            context.requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(adapter);
                }
            });

            for (Future<Port> port : ports) {
                port.get();

                if (port.get().isOpen()) {
                    openPorts++;
                    stringBuffer.append(port.get().getPort()).append(" ");
                }

                context.requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            arrayOfPorts.add(port.get());
                            Collections.sort(arrayOfPorts);
                            adapter.notifyDataSetChanged();
                            listView.setSelection(adapter.getCount());


                        } catch (ExecutionException | InterruptedException e) {
                            System.err.println(e.getMessage());
                        }

                    }


                });


            }
            context.requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPosition(adapter.getCount());
                }
            });


            executorService.shutdown();

            Logger.log(Color.CYAN.getColor() + "There are " + Color.YELLOW.getColor() + openPorts + Color.CYAN.getColor() + " open ports on host " + Color.YELLOW.getColor() + ip);
            PutLogConsole(context, editTextTextLog, "\nThere are " + openPorts + " open ports on host " + stringBuffer);
            Logger.log(SEPARATOR);
        } catch (InterruptedException | ExecutionException ex) {
            System.err.println(ex.getMessage());
            PutLogConsole(context, editTextTextLog, "\nscanning canceled !");
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPortFrom() {
        return portFrom;
    }

    public void setPortFrom(int portFrom) {
        this.portFrom = portFrom;
    }

    public int getPortTo() {
        return portTo;
    }

    public void setPortTo(int portTo) {
        this.portTo = portTo;
    }
}
