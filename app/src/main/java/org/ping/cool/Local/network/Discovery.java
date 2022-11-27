package org.ping.cool.Local.network;


import android.os.AsyncTask;
import android.util.Log;

import org.ping.cool.FirstFragment;
import org.ping.cool.Local.async.ScanHostsAsyncTask;

public class Discovery {

    private ScanHostsAsyncTask scanHostsAsyncTask;

    //Starts the host scanning
    public void scanHosts(String ip, FirstFragment firstFragment) {

      scanHostsAsyncTask =   new  ScanHostsAsyncTask(firstFragment);
      scanHostsAsyncTask.execute(ip);
    }

    public void stop(){
        if(scanHostsAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            scanHostsAsyncTask.cancel(true);
        }

    }
}
