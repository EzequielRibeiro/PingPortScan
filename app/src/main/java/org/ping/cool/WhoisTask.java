package org.ping.cool;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.widget.EditText;

public class WhoisTask extends AsyncTask<Void, Void, Void> {

    private FirstFragment context;
    private EditText editText;
    private String url;

    public WhoisTask(FirstFragment context, EditText editText, String url){
        this.context = context;
        this.editText = editText;
        this.url     = url;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.startProgressBar();
            }
        });
        Whois whois = new Whois(context);
        String host = url.replace("www.", "");
        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
               try {
                   editText.setText(whois.getWhois(host));
               }catch (Exception e){
                   editText.setText(e.getMessage());
               }
            }
        });


        return null;
    }
    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);

        context.stopProgressBar();

    }

}
