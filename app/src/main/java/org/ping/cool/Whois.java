package org.ping.cool;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;

import org.apache.commons.net.whois.WhoisClient;
import java.io.IOException;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Whois {

        private static Pattern pattern;
        private Matcher matcher;
        // regex whois parser
        private static final String WHOIS_SERVER_PATTERN = "Whois Server:\\s(.*)";
        private FirstFragment context;

        static {
            pattern = Pattern.compile(WHOIS_SERVER_PATTERN);
        }


        public Whois(FirstFragment context){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            this.context = context;
        }

        public String getWhois(String domainName) {

            StringBuilder result = new StringBuilder("");

            WhoisClient whois = new WhoisClient();
            try {

               // whois.connect(WhoisClient.DEFAULT_HOST);
                whois.connect("whois.geektools.com",43);

                // whois =google.com
                //String whoisData1 = whois.query("=" + domainName);
                String whoisData1 = whois.query(domainName);

                // append first result
                result.append(whoisData1);
                whois.disconnect();

                // get the google.com whois server - whois.markmonitor.com
                String whoisServerUrl = getWhoisServer(whoisData1);
                if (!whoisServerUrl.equals("")) {

                    // whois -h whois.markmonitor.com google.com
                    String whoisData2 =
                            queryWithWhoisServer(domainName, whoisServerUrl);

                    // append 2nd result
                    result.append(whoisData2);
                }

            } catch (SocketException e) {
                e.printStackTrace();
                if(e.getMessage() != null)
                result.append(e.getMessage());
            } catch (IOException | NetworkOnMainThreadException e) {
                e.printStackTrace();
                if(e.getMessage() != null)
                    result.append(e.getMessage());
            }
            context.stopProgressBar();
            return result.toString();

        }

        private String queryWithWhoisServer(String domainName, String whoisServer) {

            String result = "";
            WhoisClient whois = new WhoisClient();
            try {

                whois.connect(whoisServer);
                result = whois.query(domainName);
                whois.disconnect();

            } catch (SocketException e) {
                e.printStackTrace();
                if(e.getMessage() != null)
                    result = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                if(e.getMessage() != null)
                    result = e.getMessage();
            }

            return result;

        }

        private String getWhoisServer(String whois) {

            String result = "";

            matcher = pattern.matcher(whois);

            // get last whois server
            while (matcher.find()) {
                result = matcher.group(1);
            }
            return result;
        }

}
