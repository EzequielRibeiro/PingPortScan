package org.ping.cool;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.Html;
import android.util.JsonReader;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.InternetDomainName;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;


public class WhoisTask{

    private FirstFragment context;
    private EditText editText;

    private String errorMessage = "";
    private String url;
      public static final String whoisRequest = "https://api.apilayer.com/whois/query?domain=";

      public static final String apiKey = "2vDOGKj9VnkwM4fAumoyIIYFdIsU1OTL";

    public WhoisTask(FirstFragment context, EditText editText, String url){
        this.context = context;
        this.editText = editText;
        this.url     = url;

    }


    public void startApi(){

        String completeUrl = "";

        try {
            completeUrl = whoisRequest.concat(getDomainName(url));
            Log.e("resultado",completeUrl);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            assert errorMessage != null;
            Snackbar.make(editText,errorMessage,Snackbar.LENGTH_LONG).show();
        }

        String finalCompleteUrl = completeUrl;
        context.requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    editText.setText(requestShortlink(finalCompleteUrl));
                }catch (Exception e){
                    editText.setText(e.getMessage());
                }
            }
        });
    }

    public static String getDomainName(String url) throws MalformedURLException {
        String domainName =  url.replaceAll("http(s)?://|www\\.|/.*", "");
        return domainName;
    }

    public String requestShortlink(@NonNull String urlLongLink) {

        JsonReader reader = null;
        HttpURLConnection conn = null;
        String result ="";

        try {

            URL url = new URL(urlLongLink);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
        //    conn.setRequestProperty("Accept", "application/json");
         //   conn.setRequestProperty("Content-Type", "application/json");
         //   conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("apikey", apiKey);


            if (!(conn.getResponseCode() == HttpURLConnection.HTTP_OK) &&
                    !(conn.getResponseCode() == HttpURLConnection.HTTP_CREATED)) {

                result = getMessage(conn, true);
                showError(result);

            }else {

                try {
                    result = getMessage(conn, false);
                    JSONObject mainObject = new JSONObject(result);
                    InputStream is = new ByteArrayInputStream(mainObject.toString().getBytes(StandardCharsets.UTF_8));
                    reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                  return result;

                } catch (JSONException e) {
                    showError(e.getMessage());
                    System.err.println(e.getMessage());
                }
            }

        } catch (SocketTimeoutException e) {
            showError("Error: Socket Timeout");
            System.err.println(e);

        } catch (FileNotFoundException e) {
            showError("Error: File NotFound");
            System.err.println(e);

        } catch (MalformedURLException e) {
            showError("Error: MalformedURL");
            System.err.println(e);

        } catch (UnknownHostException e) {
            showError("Error: Unknown Host");
            System.err.println(e);

        } catch (NullPointerException | IOException e) {
            showError(e.getMessage());
            System.err.println(e);


        } catch (IllegalStateException e) {
            showError(getMessage(conn, false));
            System.err.println(e);


        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
            if (conn != null)
                conn.disconnect();

        }

        return errorMessage;
    }


    private static String getMessage(HttpURLConnection conn, boolean error) {

        try {
            String result;
            BufferedInputStream bis;

            if (error)
                bis = new BufferedInputStream(conn.getErrorStream());
            else bis = new BufferedInputStream(conn.getInputStream());

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result2 = bis.read();
            while (result2 != -1) {
                buf.write((byte) result2);
                result2 = bis.read();
            }
            result = buf.toString();
            System.err.println("getMessage() result "+ result);
            return result;


        } catch (Exception e) {
            System.err.println("getMessage() "+ e.getMessage());
             return e.getMessage();
        }


    }

    private void showError(String error) {
        System.err.println("showError() "+error);
        errorMessage = error;
    }


}
