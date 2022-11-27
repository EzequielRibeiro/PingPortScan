package org.ping.cool.Local.async;

import android.os.AsyncTask;
import android.util.Log;

import org.ping.cool.FirstFragment;
import org.ping.cool.Local.response.MainAsyncResponse;
import org.ping.cool.Local.runnable.ScanHostsRunnable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScanHostsAsyncTask extends AsyncTask<String, Void, Void> {
    private MainAsyncResponse delegate;
    private final int SCAN_THREADS = 8;
    private final int HOST_THREADS = 255;
    public static final String TAG = "PINGTOOL";
    private String parts[];
    private String subnet;
    private InetAddress inetAddress = null;

    //Constructor to set the delegate
    public ScanHostsAsyncTask(FirstFragment firstFragment) {
        this.delegate = firstFragment;
    }

    //Scans for active hosts on the network
    @Override
    protected Void doInBackground(String... params) {

        String ip = params[0];
        parts = ip.split("\\.");
        subnet = parts[0] + "." + parts[1] + "." + parts[2] + ".";

        ExecutorService executor = Executors.newFixedThreadPool(SCAN_THREADS);
        int chunk = (int) Math.ceil((double) 255 / SCAN_THREADS);
        int previousStart = 1;
        int previousStop = chunk;

        for (int i = 0; i < SCAN_THREADS; i++) {
            if (previousStop >= 255) {
                previousStop = 255;
                executor.execute(new ScanHostsRunnable(parts, previousStart, previousStop, delegate));
                break;
            }
            executor.execute(new ScanHostsRunnable(parts, previousStart, previousStop, delegate));
            previousStart = previousStop + 1;
            previousStop = previousStop + chunk;

        }
        executor.shutdown();

        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {

        }
        publishProgress();
        return null;
    }


    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);

    }

    @Override
    protected final void onProgressUpdate(final Void... params) {

        try {
            ExecutorService executor = Executors.newFixedThreadPool(HOST_THREADS);
            for (int i = 1; i < 255; i++) {
                int finalIp = i;

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> item = new HashMap<String, String>() {
                            @Override
                            public boolean equals(Object object) {
                                if (this == object) {
                                    return true;
                                }
                                if (object == null) {
                                    return false;
                                }
                                if (!(object instanceof HashMap)) {
                                    return false;
                                }

                                @SuppressWarnings("unchecked")
                                Map<String, String> entry = (Map<String, String>) object;
                                return entry.get("Second Line").equals(this.get("Second Line"));
                            }
                        };

                        try {
                            String ip = subnet + finalIp;

                            inetAddress = InetAddress.getByName(ip);
                            if (inetAddress.isReachable(5000)) {
                                Log.e(TAG,"IP: "+ip);
                                inetAddress = InetAddress.getByName(ip);
                                item.put("First Line", inetAddress.getCanonicalHostName());

                                String secondLine = ip + "\n[" + getMacAddr(inetAddress) + "]";
                                item.put("Second Line", secondLine);
                                delegate.processFinish(item);


                            }
                        } catch (IOException ignored) {
                            ignored.printStackTrace();

                        }
                    }

                });

            }
            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMacAddr(InetAddress addr) {


        return "";

        }
    }


    //Scans the ARP table and updates the list with hosts on the network
  /*  @Override
    protected final void onProgressUpdate(final Void... params) {
        BufferedReader reader = null;

        try {
            ExecutorService executor = Executors.newFixedThreadPool(HOST_THREADS);
            reader = new BufferedReader(new FileReader("/proc/net/arp"));
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] arpLine = line.split("\\s+");

                final String ip = arpLine[0];
                String flag = arpLine[2];
                final String macAddress = arpLine[3];

                if (!"0x0".equals(flag) && !"00:00:00:00:00:00".equals(macAddress)) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, String> item = new HashMap<String, String>() {
                                @Override
                                public boolean equals(Object object) {
                                    if (this == object) {
                                        return true;
                                    }
                                    if (object == null) {
                                        return false;
                                    }
                                    if (!(object instanceof HashMap)) {
                                        return false;
                                    }

                                    @SuppressWarnings("unchecked")
                                    Map<String, String> entry = (Map<String, String>) object;
                                    return entry.get("Second Line").equals(this.get("Second Line"));
                                }
                            };

                            String secondLine = ip + " [" + macAddress + "]";
                            item.put("Second Line", secondLine);

                            try {
                                InetAddress add = InetAddress.getByName(ip);
                                String hostname = add.getCanonicalHostName();
                                item.put("First Line", hostname);
                                delegate.processFinish(item);
                            } catch (UnknownHostException ignored) {
                                ignored.printStackTrace();
                                return;
                            }
                        }

                    });
                }
            }
            executor.shutdown();
        } catch (IOException ignored) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
            }
        }
    }*/

