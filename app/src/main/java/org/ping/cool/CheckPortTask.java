/**
 * @author Lorenzo Vaccher
 * Copyright (c) 2021 Lorenzo Vaccher.
 */

package org.ping.cool;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;

import org.apache.commons.cli.*;
import org.ping.cool.databinding.FragmentSecondBinding;
import org.ping.cool.utils.PortScanner;
import org.ping.cool.utils.WellKnownPort;
import org.ping.cool.utils.logger.Color;
import org.ping.cool.utils.logger.Logger;

import java.io.IOException;
import java.net.*;

import static org.ping.cool.MainActivity.FOOTER;
import static org.ping.cool.utils.logger.Logger.PutLogConsole;

public class CheckPortTask extends AsyncTask<Void, Void, Void> {

    public static final String VERSION = "v0.0.1";
    public static final String SEPARATOR = Color.WHITE_BOLD.getColor() + "-------------------------------------------------------------" + Color.RESET.getColor();
    private FragmentSecondBinding binding;
    private Fragment context;
    private String args[];

    public CheckPortTask(String[] args, FragmentSecondBinding binding, Fragment context) {
        this.binding = binding;
        this.args = args;
        this.context = context;

    }

    private void portCheckStart() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Logger initialization
        new Logger();
        Logger.clearConsole();
        Logger.log(SEPARATOR);
        Logger.log("Running PortScanner " + VERSION, Color.MAGENTA);
        Logger.log("Checking parameters...", Color.YELLOW);

        Options options = new Options();
        // IPV4 parameter
        Option hostOption = new Option("h", "host", true, "The host of the machine to be tested.");
        hostOption.setRequired(true);
        options.addOption(hostOption);
        // Threads parameter
        Option threadsOption = new Option("th", "threads", true, "Number of threads the program can execute.");
        options.addOption(threadsOption);
        // Timeout parameter
        Option timeoutOption = new Option("t", "timeout", true, "Time in milliseconds for the socket to disconnect");
        options.addOption(timeoutOption);
        // Port(s) parameter
        Option portsOption = new Option("p", "ports", true, "Port range to scan");
        options.addOption(portsOption);

        //header of the menu section
        String header = "";
        //footer of the menu section

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("portscanner", header, options, FOOTER, true);
            PutLogConsole(context, binding.editTextTextLog, e.getLocalizedMessage()+'\n'+options+FOOTER);
            // System.exit(1);
        }

        // Check data validity
        if (cmd != null)
            if (checkIp(cmd.getOptionValue("host").trim())) {
                try {
                    final InetAddress inetAddress = InetAddress.getByName(cmd.getOptionValue("host").trim());
                    Logger.log("The host is reachable (" + inetAddress.getHostAddress() + ")!", Color.GREEN);
                    PutLogConsole(context, binding.editTextTextLog, "\nThe host is reachable (" + inetAddress.getHostAddress() + ")!");
                    PortScanner portScanner = new PortScanner(inetAddress.getHostAddress());

                    if (cmd.getOptionValue("threads") != null) {
                        int threads = Integer.parseInt(cmd.getOptionValue("threads").replace("h ", "").trim());
                        portScanner.setThreads(threads);
                    }

                    if (cmd.getOptionValue("timeout") != null) {
                        int timeout = Integer.parseInt(cmd.getOptionValue("timeout").replace("h ", "").trim());
                        portScanner.setTimeout(timeout);
                    }

                    if (cmd.getOptionValue("ports") != null)
                        if (cmd.getOptionValue("ports").trim().contains("-")) {
                            int portFrom = Integer.parseInt(cmd.getOptionValue("ports").trim().split("-")[0]);
                            int portTo = Integer.parseInt(cmd.getOptionValue("ports").trim().split("-")[1]);
                            if (portFrom < portTo) {
                                portScanner.setPortFrom(portFrom);
                                portScanner.setPortTo(portTo);
                            } else if (portFrom > portTo) {
                                portScanner.setPortFrom(portTo);
                                portScanner.setPortTo(portFrom);
                            } else {
                                portScanner.setPortFrom(portFrom);
                                portScanner.setPortTo(-1);
                            }
                        } else {
                            portScanner.setPortFrom(Integer.parseInt(cmd.getOptionValue("ports").trim()));
                            portScanner.setPortTo(-1); //-1 indicates that there is no end
                        }

                    // Loading of known ports
                    WellKnownPort.load(context,binding.editTextTextLog);
                    // Start scan
                    portScanner.start(context,binding.editTextTextLog, binding.listViewPort);
                } catch (UnknownHostException | NumberFormatException e) {
                    e.printStackTrace();
                    PutLogConsole(context, binding.editTextTextLog, '\n'+"Error: "+e.getLocalizedMessage());
                }
            }
    }

    /**
     * This Function is used to check the validity of an IPV4 address
     * or of a HostName.
     *
     * @param ip The ip or the name of the host that needs to be verified
     * @return Authenticity of the ip passed down as an input to the function
     */

    private boolean checkIp(String ip) {
        boolean isIPv4 = false;

        try {
            final InetAddress inetAddress = InetAddress.getByName(ip);
            isIPv4 = (inetAddress.getHostAddress().equals(ip) || inetAddress.getHostName().equals(ip))
                    && inetAddress instanceof Inet4Address;

        } catch (final UnknownHostException e) {
            Logger.log("Host unreachable...", Color.RED);
            PutLogConsole(context, binding.editTextTextLog, "\nHost unreachable...");
            return false;
        } catch (IOException e) {
            Logger.log("An error has occurred...", Color.RED);
            PutLogConsole(context, binding.editTextTextLog, "\nAn error has occurred...");
            e.printStackTrace();
        }
        return isIPv4;
    }

    public static boolean isHostAvailable(final String host, final int port, final int timeout) {
        try (final Socket socket = new Socket()) {
            final InetAddress inetAddress = InetAddress.getByName(host);
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
            socket.connect(inetSocketAddress, timeout);
            return true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    protected Void doInBackground(Void... voids) {

        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.progressBarScan.setVisibility(View.VISIBLE);
                binding.buttonScanRangePort.setText("Stop");
            }
        });

        portCheckStart();

        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);

        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.progressBarScan.setVisibility(View.INVISIBLE);
                binding.buttonScanRangePort.setText("Scan");
            }
        });

    }
}