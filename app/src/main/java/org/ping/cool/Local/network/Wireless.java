package org.ping.cool.Local.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class Wireless {

    private Activity activity;

    //Constructor to set the activity for context
    public Wireless(Activity activity) {
        this.activity = activity;
    }

    //Local WiFi network LAN IP address
    public String getInternalWifiIpAddress() {
        int ip = this.getWifiInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }
        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
        try {
            return InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    //Determines if the device is connected to a WiFi network or not
    public boolean isConnectedWifi() {
        NetworkInfo info = this.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

    //WifiManager
    public WifiManager getWifiManager() {
        return (WifiManager) this.activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    //WiFi information
    private WifiInfo getWifiInfo() {
        return this.getWifiManager().getConnectionInfo();
    }

    //Connectivity manager
    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    //Network information
    private NetworkInfo getNetworkInfo(int type) {
        return this.getConnectivityManager().getNetworkInfo(type);
    }

}
