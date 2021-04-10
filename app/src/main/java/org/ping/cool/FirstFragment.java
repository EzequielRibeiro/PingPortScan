package org.ping.cool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private Button buttonTracert, buttonPing, buttonLocal, buttonWhois, buttonSecondFragment;
    private FrameLayout frameLayout;
    private FloatingActionButton floatingActionButton;
    private EditText editTextTextConsole;
    private AutoCompleteTextView autoCompleteTextViewUrl;
    private WebView webView;
    private ProgressBar progressBarPing;
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
    private ViewGroup parentFrameLayout;
    private ListView listViewLocal, listViewTracert;
    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        this.tracerouteWithPing = new TracerouteWithPing(this);


        View v = inflater.inflate(R.layout.fragment_first, container, false);

        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }

        this.autoCompleteTextViewUrl = mainActivity.findViewById(R.id.autoCompleteTextViewUrl);
        this.buttonTracert = (Button) v.findViewById(R.id.buttonTracert);
        this.buttonPing = (Button) v.findViewById(R.id.buttonPing);
        this.buttonLocal = (Button) v.findViewById(R.id.buttonLocal);
        this.buttonWhois = (Button) v.findViewById(R.id.buttonWhois);
        this.frameLayout = (FrameLayout) v.findViewById(R.id.frameLayout);
        this.parentFrameLayout = (ViewGroup) frameLayout.getParent();
        this.buttonSecondFragment = (Button) v.findViewById(R.id.buttonSecond);
        this.floatingActionButton = (FloatingActionButton) v.findViewById(R.id.fabFirstFragment);
        this.webView = (WebView) v.findViewById(R.id.webView);
        this.editTextTextConsole = (EditText) v.findViewById(R.id.editTextTextConsole);
        this.progressBarPing = (ProgressBar) v.findViewById(R.id.progressBarPing);
        listViewLocal = new ListView(getActivity());
        listViewLocal.setBackgroundColor(getResources().getColor(R.color.white_color));
        listViewTracert = new ListView(getActivity());
        listViewTracert.setBackgroundColor(getResources().getColor(R.color.white_color));

        initView();
        return v;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        traces = new ArrayList<TracerouteContainer>();
        traceListAdapter = new TraceListAdapter(getActivity());
        listViewTracert.setAdapter(traceListAdapter);
        frameLayout.addView(listViewTracert);

        setupHostsAdapter();
        setupReceivers();
        webView.loadUrl("file:///android_asset/ping.html");
        webView.setVisibility(View.VISIBLE);
        editTextTextConsole.setVisibility(View.INVISIBLE);
        listViewLocal.setVisibility(View.INVISIBLE);
        listViewTracert.setVisibility(View.INVISIBLE);

        //  editTextPing.setText("www.google.com");
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.setVisibility(View.VISIBLE);
                editTextTextConsole.setVisibility(View.INVISIBLE);
                listViewLocal.setVisibility(View.INVISIBLE);
                listViewTracert.setVisibility(View.INVISIBLE);

            }
        });

        buttonWhois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!autoCompleteTextViewUrl.getText().toString().isEmpty()) {

                    webView.setVisibility(View.INVISIBLE);
                    listViewLocal.setVisibility(View.INVISIBLE);
                    listViewTracert.setVisibility(View.INVISIBLE);
                    editTextTextConsole.setVisibility(View.VISIBLE);

                    new WhoisTask(FirstFragment.this, editTextTextConsole, autoCompleteTextViewUrl.getText().toString()).execute();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_text_web), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (buttonLocal.getText().toString().equals("Local")) {
                    startProgressBar();
                    buttonLocal.setText("Stop");
                    buttonTracert.setEnabled(false);
                    buttonSecondFragment.setEnabled(false);
                    buttonPing.setEnabled(false);
                    buttonWhois.setEnabled(false);
                    listViewLocal.setVisibility(View.VISIBLE);
                    listViewTracert.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.INVISIBLE);
                    editTextTextConsole.setVisibility(View.INVISIBLE);
                    wifi = new Wireless(getActivity());

                    setupHostDiscovery();

                } else {
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

        buttonTracert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (autoCompleteTextViewUrl.getText().length() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_text), Toast.LENGTH_SHORT).show();
                } else if (!isOnline(getActivity().getApplicationContext())) {
                    Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();

                } else {
                    if (buttonTracert.getText().equals("Tracert")) {

                        DBAdapter dbAdapter = new DBAdapter(getActivity());

                        if (dbAdapter.insertUrl(autoCompleteTextViewUrl.getText().toString(), "") > 0) {
                            mainActivity.refreshAutoCompleteTextView();
                        }
                        dbAdapter.close();

                        traces.clear();
                        traceListAdapter.notifyDataSetChanged();
                        startProgressBar();
                        buttonTracert.setText(getString(R.string.activity_buttonStop));
                        buttonPing.setEnabled(false);
                        buttonLocal.setEnabled(false);
                        buttonWhois.setEnabled(false);
                        buttonSecondFragment.setEnabled(false);
                        webView.setVisibility(View.INVISIBLE);
                        editTextTextConsole.setVisibility(View.INVISIBLE);
                        listViewLocal.setVisibility(View.INVISIBLE);
                        listViewTracert.setVisibility(View.VISIBLE);
                        tracerouteWithPing.executeTraceroute(autoCompleteTextViewUrl.getText().toString(), maxTtl);
                        TracerouteWithPing.StopPing(false);
                    } else {
                        stopProgressBar();
                        TracerouteWithPing.StopPing(true);
                    }
                }
            }
        });

        buttonPing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (autoCompleteTextViewUrl.getText().length() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_text), Toast.LENGTH_SHORT).show();
                } else if (!isOnline(getActivity().getApplicationContext())) {
                    Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();

                } else {

                    if (buttonPing.getText().equals("Ping")) {

                        webView.setVisibility(View.INVISIBLE);
                        listViewTracert.setVisibility(View.INVISIBLE);
                        listViewLocal.setVisibility(View.INVISIBLE);
                        editTextTextConsole.setVisibility(View.VISIBLE);

                        DBAdapter dbAdapter = new DBAdapter(getActivity());

                        if (dbAdapter.insertUrl(autoCompleteTextViewUrl.getText().toString(), "") > 0) {
                            mainActivity.refreshAutoCompleteTextView();
                        }
                        dbAdapter.close();

                        startProgressBar();
                        editTextTextConsole.setText("");
                        tracerouteWithPing.executePing(autoCompleteTextViewUrl.getText().toString(), editTextTextConsole);
                        TracerouteWithPing.StopPing(false);
                        buttonPing.setText(getText(R.string.activity_buttonStop));
                        buttonTracert.setEnabled(false);
                        buttonLocal.setEnabled(false);
                        buttonWhois.setEnabled(false);
                        buttonSecondFragment.setEnabled(false);

                    } else {
                        TracerouteWithPing.StopPing(true);
                        stopProgressBar();

                    }

                }
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
        autoCompleteTextViewUrl.setEnabled(false);
        hideSoftwareKeyboard(autoCompleteTextViewUrl);


    }

    public void stopProgressBar() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarPing.setVisibility(View.GONE);
                buttonPing.setText("Ping");
                buttonPing.setEnabled(true);
                buttonTracert.setText("Tracert");
                buttonTracert.setEnabled(true);
                buttonSecondFragment.setEnabled(true);
                buttonLocal.setText("Local");
                buttonLocal.setEnabled(true);
                buttonWhois.setEnabled(true);
                autoCompleteTextViewUrl.setEnabled(true);
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
        listViewLocal.setAdapter(this.hostsAdapter);
        frameLayout.addView(listViewLocal);

    }

    private void setupHostDiscovery() {

        if (!wifi.isConnectedWifi()) {
            Toast.makeText(getContext(), "You're not connected to a WiFi network!", Toast.LENGTH_SHORT).show();
            return;
        }
        hosts.clear();
        hostsAdapter.notifyDataSetChanged();
        discovery.scanHosts(wifi.getInternalWifiIpAddress(), FirstFragment.this);
        hideSoftwareKeyboard(editTextTextConsole);

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
            progressBarPing.setVisibility(View.GONE);
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