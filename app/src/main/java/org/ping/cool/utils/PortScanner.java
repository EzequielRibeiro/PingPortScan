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
import java.util.List;
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
        this.ip = ip;
    }

    /**
     * This method is used to start the scanning of the server.
     *
     * @param context
     * @param editTextTextLog
     */
    public void start(SecondFragment context, EditText editTextTextLog, ListView listView) {
        Logger.log("Start scanning " + this.ip + "...", Color.CYAN);
        PutLogConsole(context, editTextTextLog, "\nStart scanning " + this.ip + ".....");

        ExecutorService executorService = Executors.newFixedThreadPool(this.threads);
        ports = new ArrayList<>();

        // Adding ports to the list that are verified.
        if (this.portTo != -1)
            for (int i = this.portFrom; i <= this.portTo; i++)
                ports.add(Port.scan(executorService, this.ip, i, this.timeout));
        else
            ports.add(Port.scan(executorService, this.ip, this.portFrom, this.timeout));

        try {
            // This for loop verifies that all Threads have completed their task and load list
            ArrayList<Port> arrayOfPorts = new ArrayList<Port>();
            PortAdapter adapter = new PortAdapter(context.getContext(), arrayOfPorts);
            context.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(adapter);
                }
            });

            for (Future<Port> port : ports) {
                port.get();

                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            arrayOfPorts.add(port.get());

                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();

                      try {
                          context.getActivity().runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  listView.setSelection(adapter.getCount());
                              }
                          });
                      }catch(NullPointerException e){
                          e.printStackTrace();
                      }
                    }
                });

            }

            executorService.shutdown();
            StringBuffer stringBuffer = new StringBuffer();
            // This for loop counts the number of open ports
            int openPorts = 0;
            for (final Future<Port> f : ports) {

                if (f.get().isOpen()) {
                    openPorts++;
                    stringBuffer.append(f.get().getPort() + " ");
                }
            }


            Log.e("Port", "there are " + openPorts);

            Logger.log(Color.CYAN.getColor() + "There are " + Color.YELLOW.getColor() + openPorts + Color.CYAN.getColor() + " open ports on host " + Color.YELLOW.getColor() + ip);
            PutLogConsole(context, editTextTextLog, "\nThere are " + openPorts + " open ports on host " + stringBuffer.toString());
            Logger.log(SEPARATOR);
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            PutLogConsole(context, editTextTextLog, "\nscanning canceled !");
        }
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
