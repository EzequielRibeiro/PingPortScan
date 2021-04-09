package org.ping.cool.Local.network;


import android.os.AsyncTask;

import org.ping.cool.Local.async.ScanHostsAsyncTask;
import org.ping.cool.Local.response.MainAsyncResponse;

public class Discovery {

    private ScanHostsAsyncTask scanHostsAsyncTask;

    //Starts the host scanning
    public void scanHosts(String ip, MainAsyncResponse delegate) {
      scanHostsAsyncTask =   new  ScanHostsAsyncTask(delegate);
      scanHostsAsyncTask.execute(ip);
    }

    public void stop(){
        if(scanHostsAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            scanHostsAsyncTask.cancel(true);
        }

    }
}
