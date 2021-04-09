package org.ping.cool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.ping.cool.Local.network.Discovery;
import org.ping.cool.Local.network.Wireless;
import org.ping.cool.Local.response.MainAsyncResponse;
import org.ping.cool.databinding.FragmentFirstBinding;
import org.ping.cool.network.TracerouteContainer;
import org.ping.cool.network.TracerouteWithPing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.ping.cool.MainActivity.isOnline;

public class FirstFragment extends Fragment implements MainAsyncResponse {

    public static final String tag = "TraceroutePing";
    public static final String INTENT_TRACE = "INTENT_TRACE";
    private Button buttonTracert, buttonPing,buttonLocal, buttonSecondFragment;
    private FloatingActionButton floatingActionButton;
    private EditText editTextTextConsole;
    private AutoCompleteTextView autoCompleteTextViewUrl;
    private WebView webView;
    private ProgressBar progressBarPing;
    private ListView listViewTraceroute;
    private TraceListAdapter traceListAdapter;
    private FragmentFirstBinding binding;
    private TracerouteWithPing tracerouteWithPing;
    private final int maxTtl = 40;
    private MainActivity mainActivity;
    private Wireless wifi;
    private Discovery discovery = new Discovery();
    private Handler mHandler = new Handler();
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter = new IntentFilter();
    private ArrayAdapter hostsAdapter;
    private List<Map<String, String>> hosts = new ArrayList<>();

    private List<TracerouteContainer> traces;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        this.tracerouteWithPing = new TracerouteWithPing(this);
        this.traces = new ArrayList<TracerouteContainer>();

        View v = inflater.inflate(R.layout.fragment_first, container, false);

        if ( getActivity() instanceof MainActivity){
            mainActivity = (MainActivity) getActivity();
        }

        this.autoCompleteTextViewUrl = mainActivity.findViewById(R.id.autoCompleteTextViewUrl);
        this.buttonTracert = (Button) v.findViewById(R.id.buttonTracert);
        this.buttonPing = (Button) v.findViewById(R.id.buttonPing);
        this.buttonLocal = (Button) v.findViewById(R.id.buttonLocal);
        this.buttonSecondFragment = (Button) v.findViewById(R.id.buttonSecond);
        this.floatingActionButton = (FloatingActionButton) v.findViewById(R.id.fabFirstFragment);
        this.webView = (WebView) v.findViewById(R.id.webView);
        this.editTextTextConsole = (EditText) v.findViewById(R.id.editTextTextConsole);
        this.listViewTraceroute = (ListView) v.findViewById(R.id.listViewTraceroute);
        this.progressBarPing = (ProgressBar) v.findViewById(R.id.progressBarPing);
       // editTextPing.setText("-c 5 www.google.com");
        this.wifi = new Wireless(getActivity());
        this.setupHostsAdapter();
        this.setupReceivers();

        initView();

        return v;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null)
            this.hosts = (ArrayList<Map<String, String>>) savedInstanceState.getSerializable("hosts");

        if (this.hosts != null)
            this.setupHostsAdapter();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * initView, init the main view components (action, adapter...)
     */
    private void initView() {

        webView.loadUrl("file:///android_asset/ping.html");
      //  editTextPing.setText("www.google.com");
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.setVisibility(View.VISIBLE);
                listViewTraceroute.setVisibility(View.GONE);
                editTextTextConsole.setVisibility(View.GONE);
                TracerouteWithPing.StopPing(true);
                stopProgressBar();
                traces.clear();
                traceListAdapter.notifyDataSetChanged();
            }
        });

        buttonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(buttonLocal.getText().toString().equals("Local")) {
                  buttonLocal.setText("Stop");
                  buttonTracert.setEnabled(false);
                  buttonSecondFragment.setEnabled(false);
                  buttonPing.setEnabled(false);
                  setupHostDiscovery();
              }else{
                  buttonLocal.setText("Local");
                  stopProgressBar();
                  discovery.stop();

              }
            }
        });

        buttonSecondFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                buttonTracert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (autoCompleteTextViewUrl.getText().length() == 0) {
                            Toast.makeText(getActivity(), getString(R.string.no_text), Toast.LENGTH_SHORT).show();
                        } else if (!isOnline(getActivity().getApplicationContext())) {
                            Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();

                        } else {
                            if(buttonTracert.getText().equals("Tracert")) {

                                DBAdapter dbAdapter = new DBAdapter(getActivity());

                                if(dbAdapter.insertUrl(autoCompleteTextViewUrl.getText().toString(),"") > 0) {
                                    mainActivity.refreshAutoCompleteTextView();
                                }
                                dbAdapter.close();

                                traces.clear();
                                traceListAdapter.notifyDataSetChanged();
                                startProgressBar();
                                buttonTracert.setText(getString(R.string.activity_buttonStop));
                                hideSoftwareKeyboard(autoCompleteTextViewUrl);
                                tracerouteWithPing.executeTraceroute(autoCompleteTextViewUrl.getText().toString(), maxTtl);
                                TracerouteWithPing.StopPing(false);
                                buttonPing.setEnabled(false);
                                buttonSecondFragment.setEnabled(false);
                                listViewTraceroute.setVisibility(View.VISIBLE);
                                webView.setVisibility(View.GONE);
                                editTextTextConsole.setVisibility(View.GONE);
                            }else{
                                stopProgressBar();
                                TracerouteWithPing.StopPing(true);
                            }
                        }
                    }
                });
                traceListAdapter = new TraceListAdapter(getActivity());
                listViewTraceroute.setAdapter(traceListAdapter);
            }
        });


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonPing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (autoCompleteTextViewUrl.getText().length() == 0) {
                            Toast.makeText(getActivity(), getString(R.string.no_text), Toast.LENGTH_SHORT).show();
                        } else if (!isOnline(getActivity().getApplicationContext())) {
                            Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();

                        } else {

                            if (buttonPing.getText().equals("Ping")) {

                                DBAdapter dbAdapter = new DBAdapter(getActivity());

                                if(dbAdapter.insertUrl(autoCompleteTextViewUrl.getText().toString(),"") > 0) {
                                    mainActivity.refreshAutoCompleteTextView();
                                }
                                dbAdapter.close();

                                startProgressBar();
                                hideSoftwareKeyboard(autoCompleteTextViewUrl);
                                editTextTextConsole.setText("");

                                if(autoCompleteTextViewUrl.getText().toString().contains("whois ")){
                                    Whois whois = new Whois(FirstFragment.this);
                                    String host = autoCompleteTextViewUrl.getText().toString().replace("whois ","").
                                            replace("www.","");
                                    editTextTextConsole.setText(whois.getWhois(host));
                                    webView.setVisibility(View.GONE);
                                    return;
                                }

                                tracerouteWithPing.executePing(autoCompleteTextViewUrl.getText().toString(),editTextTextConsole);
                                TracerouteWithPing.StopPing(false);
                                buttonPing.setText(getText(R.string.activity_buttonStop));
                                buttonTracert.setEnabled(false);
                                buttonSecondFragment.setEnabled(false);
                                listViewTraceroute.setVisibility(View.GONE);
                                webView.setVisibility(View.GONE);
                                editTextTextConsole.setVisibility(View.VISIBLE);
                            } else {
                                TracerouteWithPing.StopPing(true);
                                stopProgressBar();

                            }

                        }
                    }
                });
            }
        });
    }

    public void refreshList(TracerouteContainer trace) {
        final TracerouteContainer fTrace = trace;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                traces.add(fTrace);
                traceListAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * The adapter of the listview (build the views)
     */
    public class TraceListAdapter extends BaseAdapter {

        private FragmentActivity context;

        public TraceListAdapter(FragmentActivity c) {
            context = c;
        }

        public int getCount() {
            return traces.size();
        }

        public TracerouteContainer getItem(int position) {
            return traces.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            // first init
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.item_list_trace, null);

                TextView textViewNumber = (TextView) convertView.findViewById(R.id.textViewNumber);
                TextView textViewIp = (TextView) convertView.findViewById(R.id.textViewIp);
                TextView textViewTime = (TextView) convertView.findViewById(R.id.textViewTime);
                ImageView imageViewStatusPing = (ImageView) convertView.findViewById(R.id.imageViewStatusPing);

                // Set up the ViewHolder.
                holder = new ViewHolder();
                holder.textViewNumber = textViewNumber;
                holder.textViewIp = textViewIp;
                holder.textViewTime = textViewTime;
                holder.imageViewStatusPing = imageViewStatusPing;

                // Store the holder with the view.
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TracerouteContainer currentTrace = getItem(position);

            if (position % 2 == 1) {
                convertView.setBackgroundResource(R.drawable.table_odd_lines);
            } else {
                convertView.setBackgroundResource(R.drawable.table_pair_lines);
            }

            if (currentTrace.isSuccessful()) {
                holder.imageViewStatusPing.setImageResource(R.drawable.check);
            } else {
                holder.imageViewStatusPing.setImageResource(R.drawable.cross);
            }

            holder.textViewNumber.setText(position + "");
            holder.textViewIp.setText(currentTrace.getHostname() + " (" + currentTrace.getIp() + ")");
            holder.textViewTime.setText(currentTrace.getMs() + "ms");

            return convertView;
        }

        // ViewHolder pattern
        class ViewHolder {
            TextView textViewNumber;
            TextView textViewIp;
            TextView textViewTime;
            ImageView imageViewStatusPing;
        }
    }

    /**
     * Hides the keyboard
     *
     * @param currentEditText The current selected edittext
     */
    public void hideSoftwareKeyboard(EditText currentEditText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void startProgressBar() {
        progressBarPing.setVisibility(View.VISIBLE);

    }

    public void stopProgressBar() {

       getActivity().runOnUiThread(new Runnable() {
           @Override
           public void run() {
               progressBarPing.setVisibility(View.INVISIBLE);
               buttonPing.setText("Ping");
               buttonPing.setEnabled(true);
               buttonTracert.setText("Tracert");
               buttonTracert.setEnabled(true);
               buttonSecondFragment.setEnabled(true);
               buttonLocal.setText("Local");
               buttonLocal.setEnabled(true);
           }
       });



    }

    private void setupHostsAdapter() {
        this.hostsAdapter = new ArrayAdapter<Map<String, String>>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, this.hosts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text2.setTextColor(getResources().getColor(R.color.grey_color));
                text1.setText(hosts.get(position).get("First Line"));
                text2.setText(hosts.get(position).get("Second Line"));
                return view;
            }
        };
        listViewTraceroute.setAdapter(this.hostsAdapter);
    }

    private void setupHostDiscovery() {

                if (!wifi.isConnectedWifi()) {
                    Toast.makeText(getContext(), "You're not connected to a WiFi network!", Toast.LENGTH_SHORT).show();
                    return;
                }
                hosts.clear();
                hostsAdapter.notifyDataSetChanged();
                progressBarPing.setVisibility(View.VISIBLE);
                discovery.scanHosts(wifi.getInternalWifiIpAddress(), FirstFragment.this);
                listViewTraceroute.setVisibility(View.VISIBLE);
                editTextTextConsole.setVisibility(View.GONE);
                webView.setVisibility(View.GONE);

    }

    private void setupReceivers() {
        this.receiver = new BroadcastReceiver() {

            //Detect if a network connection has been lost
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
            }
        };
        this.intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        requireActivity().registerReceiver(receiver, this.intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressBarPing != null && (progressBarPing.getVisibility() == View.VISIBLE)) {
            progressBarPing.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.receiver != null) {
            requireActivity().unregisterReceiver(this.receiver);
        }
    }


    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(this.receiver, this.intentFilter);
    }


    @Override
    public void processFinish(Map<String, String> output) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                synchronized (hosts) {
                    if (!hosts.contains(output)) {
                        hosts.add(output);
                    } else {
                        hosts.set(hosts.indexOf(output), output);
                    }
                    Collections.sort(hosts, new Comparator<Map<String, String>>() {
                        @Override
                        public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                            int left = Integer.parseInt(lhs.get("Second Line").substring(lhs.get("Second Line").lastIndexOf(".") + 1, lhs.get("Second Line").indexOf("[") - 1));
                            int right = Integer.parseInt(rhs.get("Second Line").substring(rhs.get("Second Line").lastIndexOf(".") + 1, rhs.get("Second Line").indexOf("[") - 1));
                            return left - right;
                        }
                    });
                    hostsAdapter.notifyDataSetChanged();
                }
                stopProgressBar();
            }
        });
    }

    @Override
    public void processFinish(int output) {
        stopProgressBar();
    }

}