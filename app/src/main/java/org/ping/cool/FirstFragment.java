package org.ping.cool;

import static org.ping.cool.MainActivity.isOnline;

import android.annotation.SuppressLint;
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
import android.view.WindowManager;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.ping.cool.Local.network.Discovery;
import org.ping.cool.Local.network.Wireless;
import org.ping.cool.Local.response.MainAsyncResponse;
import org.ping.cool.databinding.FragmentFirstBinding;
import org.ping.cool.network.TracerouteContainer;
import org.ping.cool.network.TraceroutePingCommand;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirstFragment extends Fragment implements MainAsyncResponse {

    public static final String tag = "TraceroutePing";
    public static final String INTENT_TRACE = "INTENT_TRACE";
    private Button buttonTracert, buttonPing, buttonExec, buttonLocal, buttonWhois, buttonSecondFragment;
    private FrameLayout frameLayout;
    private FloatingActionButton floatingActionButton;
    private EditText editTextTextConsole;
    private AutoCompleteTextView autoCompleteTextInput;
    private WebView webView;
    private ProgressBar progressBarPing;
    private TraceListAdapter traceListAdapter;
    private FragmentFirstBinding binding;
    private TraceroutePingCommand traceroutePingCommand;
    private final int maxTtl = 20;
    private MainActivity mainActivity;
    private Wireless wifi;
    private Discovery discovery = new Discovery();
    private Handler mHandler = new Handler();
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter = new IntentFilter();
    private ArrayAdapter hostsAdapter;
    private List<Map<String, String>> hosts;
    private List<TracerouteContainer> traces;
    private ListView listViewLocal;
    private InterstitialAd mInterstitialAd;
    private static boolean firstShowAd = true;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        traceroutePingCommand = new TraceroutePingCommand(FirstFragment.this);
        View v = inflater.inflate(R.layout.fragment_first, container, false);
        this.buttonTracert = (Button) v.findViewById(R.id.buttonTracert);
        this.buttonPing = (Button) v.findViewById(R.id.buttonPing);
        this.buttonLocal = (Button) v.findViewById(R.id.buttonLocal);
        this.buttonWhois = (Button) v.findViewById(R.id.buttonWhois);
        this.buttonExec = (Button) v.findViewById(R.id.buttonExec);
        this.frameLayout = (FrameLayout) v.findViewById(R.id.frameLayout);
        this.buttonSecondFragment = (Button) v.findViewById(R.id.buttonSecond);
        this.floatingActionButton = (FloatingActionButton) v.findViewById(R.id.fabFirstFragment);
        this.webView = (WebView) v.findViewById(R.id.webView);
        this.editTextTextConsole = (EditText) v.findViewById(R.id.editTextTextConsole);
        this.progressBarPing = (ProgressBar) v.findViewById(R.id.progressBarPing);
        this.listViewLocal = (ListView) v.findViewById(R.id.listViewFirstFragment);

        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            listViewLocal.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.layout_border));
            editTextTextConsole.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.layout_border));
        } else {
            listViewLocal.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.layout_border));
            editTextTextConsole.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.layout_border));
        }
        initView();
        return v;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        loadAdInter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void textInput(){
        if (((MainActivity)getActivity()) instanceof MainActivity){
            mainActivity = (MainActivity) getActivity();
        }
        this.autoCompleteTextInput = (AutoCompleteTextView) mainActivity.findViewById(R.id.autoCompleteTextUrl);
    }

    /**
     * initView, init the main view components (action, adapter...)
     */
    private void initView() {
        setupReceivers();
        webView.loadUrl("file:///android_asset/ping.html");
        webView.setVisibility(View.VISIBLE);

        floatingActionButton.setVisibility(View.GONE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.setVisibility(View.VISIBLE);
                editTextTextConsole.setVisibility(View.INVISIBLE);
                binding.listViewFirstFragment.setVisibility(View.GONE);
              //  listViewLocal.setVisibility(View.INVISIBLE);
              //  listViewTracert.setVisibility(View.INVISIBLE);
                view.setVisibility(View.GONE);

            }
        });

        buttonWhois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textInput();
                if (!autoCompleteTextInput.getText().toString().isEmpty()) {

                    webView.setVisibility(View.INVISIBLE);
                    listViewLocal.setVisibility(View.INVISIBLE);
                    editTextTextConsole.setVisibility(View.VISIBLE);

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new WhoisTask(FirstFragment.this, editTextTextConsole, autoCompleteTextInput.getText().toString()).execute();
                        }
                    });


                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_text_web), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wifi = new Wireless(getActivity());

                if (!wifi.isConnectedWifi()) {
                    Toast.makeText(getContext(), "You're not connected to a WiFi network!", Toast.LENGTH_SHORT).show();
                    return;
                }

                textInput();
                if (buttonLocal.getText().toString().equals("Local")) {
                    startProgressBar();
                    buttonLocal.setText("Stop");
                    buttonTracert.setEnabled(false);
                    buttonSecondFragment.setEnabled(false);
                    buttonPing.setEnabled(false);
                    buttonWhois.setEnabled(false);
                    buttonExec.setEnabled(false);
                    autoCompleteTextInput.setEnabled(false);
                   // listViewLocal.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.INVISIBLE);
                  //  listViewTracert.setVisibility(View.INVISIBLE);
                    editTextTextConsole.setVisibility(View.INVISIBLE);
                    listViewLocal.setVisibility(View.VISIBLE);
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
                textInput();
                if (autoCompleteTextInput.getText().length() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_text), Toast.LENGTH_SHORT).show();
                } else if (!isOnline(getActivity().getApplicationContext())) {
                    Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();

                } else {
                    if (buttonTracert.getText().equals("Tracert")) {

                        DBAdapter dbAdapter = new DBAdapter(getActivity());

                        if (dbAdapter.insertUrl(autoCompleteTextInput.getText().toString(), "") > 0) {
                            mainActivity.refreshAutoCompleteTextView();
                        }
                        dbAdapter.close();
                        startProgressBar();
                        buttonTracert.setText(getString(R.string.activity_buttonStop));
                        buttonPing.setEnabled(false);
                        buttonLocal.setEnabled(false);
                        buttonWhois.setEnabled(false);
                        buttonExec.setEnabled(false);
                        buttonSecondFragment.setEnabled(false);
                        webView.setVisibility(View.INVISIBLE);
                        editTextTextConsole.setVisibility(View.INVISIBLE);
                        traces = new ArrayList<TracerouteContainer>();
                        traceListAdapter = new TraceListAdapter(getActivity());
                        listViewLocal.setVisibility(View.VISIBLE);
                        listViewLocal.setAdapter(traceListAdapter);

                        traceroutePingCommand.executeTraceroute(editTextTextConsole, autoCompleteTextInput.getText().toString(), maxTtl);

                    } else {
                        stopProgressBar();
                    }
                }
            }
        });

        buttonPing.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onClick(View v) {
                textInput();
                if (autoCompleteTextInput.getText().length() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_text), Toast.LENGTH_SHORT).show();
                } else if (!isOnline(requireActivity().getApplicationContext())) {
                    Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();

                } else {
                    String command = "ping ";
                    String ip = "127.0.0.1";
                    InetAddress inetAddress;
                    try {
                        inetAddress = InetAddress.getByName(String.valueOf(autoCompleteTextInput.getText()));
                        ip = inetAddress.getHostAddress();
                    } catch (UnknownHostException e) {
                        System.err.println(e);
                    }

                    InetAddressValidator validator = InetAddressValidator.getInstance();

                    if (validator.isValidInet6Address(ip)) {
                        command = "ping6 ";
                    }

                    StringBuilder args = new StringBuilder();


                //remove first space and get command and args
               for (String c : autoCompleteTextInput.getText().toString().split(" +"))
                            if(!c.equals("ping") && !c.equals("ping6"))
                                args.append(c+" ");
                            else if(c.equals("ping6"))
                                command = "ping6 ";
                    

                      if (buttonPing.getText().equals("Ping")) {

                            webView.setVisibility(View.INVISIBLE);
                            listViewLocal.setVisibility(View.INVISIBLE);
                            editTextTextConsole.setVisibility(View.VISIBLE);

                            DBAdapter dbAdapter = new DBAdapter(getActivity());

                            if (dbAdapter.insertUrl(args.toString(), "") > 0) {
                                mainActivity.refreshAutoCompleteTextView();
                            }
                            dbAdapter.close();
                            startProgressBar();
                            editTextTextConsole.setText("");
                            traceroutePingCommand.executePingCommand(command,args.toString(), editTextTextConsole);

                            buttonPing.setText(getText(R.string.activity_buttonStop));
                            buttonTracert.setEnabled(false);
                            buttonLocal.setEnabled(false);
                            buttonWhois.setEnabled(false);
                            buttonExec.setEnabled(false);
                            buttonSecondFragment.setEnabled(false);

                        } else {
                            stopProgressBar();

                        }

                }
            }
        });

        buttonExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //test
              //  editTextTextConsole.setText("curl ifconfig.me");
                textInput();
                if (autoCompleteTextInput.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Type a command", Toast.LENGTH_SHORT).show();
                } else {

                    String command = null;
                    StringBuilder args = new StringBuilder();

                    for (String c : autoCompleteTextInput.getText().toString().split(" +"))
                        if (command == null) 
                            command = c;
                        else
                            args.append(" "+c);

                        if (buttonExec.getText().equals("Exec")) {

                            webView.setVisibility(View.INVISIBLE);
                            listViewLocal.setVisibility(View.INVISIBLE);
                            editTextTextConsole.setVisibility(View.VISIBLE);

                            DBAdapter dbAdapter = new DBAdapter(getActivity());

                            if (dbAdapter.insertUrl(command +" "+ args, "") > 0) {
                                mainActivity.refreshAutoCompleteTextView();
                            }
                            dbAdapter.close();

                            startProgressBar();
                            editTextTextConsole.setText("");
                            traceroutePingCommand.executePingCommand(command +" ",args.toString(),editTextTextConsole);
                            buttonExec.setText(getText(R.string.activity_buttonStop));
                            buttonPing.setEnabled(false);
                            buttonTracert.setEnabled(false);
                            buttonLocal.setEnabled(false);
                            buttonWhois.setEnabled(false);
                            buttonSecondFragment.setEnabled(false);

                        } else {
                            stopProgressBar();
                        }
                        return;
                }
            }
        });


    }

    public void refreshList(TracerouteContainer trace) {
        final TracerouteContainer fTrace = trace;
        requireActivity().runOnUiThread(new Runnable() {
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
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void startProgressBar() {

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                progressBarPing.setVisibility(View.VISIBLE);
                autoCompleteTextInput.setEnabled(false);
                hideSoftwareKeyboard(autoCompleteTextInput);
                floatingActionButton.setVisibility(View.GONE);

            }
        });
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
                buttonExec.setText("Exec");
                buttonExec.setEnabled(true);
                buttonSecondFragment.setEnabled(true);
                buttonLocal.setText("Local");
                buttonLocal.setEnabled(true);
                buttonWhois.setEnabled(true);
                floatingActionButton.setVisibility(View.VISIBLE);
                TraceroutePingCommand.StopPing();
                autoCompleteTextInput.setEnabled(true);


            }
        });
        showInterstitial();

    }

    private void setupHostsAdapter() {

        hosts = new ArrayList<>();
        this.hostsAdapter = new ArrayAdapter<Map<String, String>>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, this.hosts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text2.setTextColor(getResources().getColor(R.color.grey_color));
                text1.setText(hosts.get(position).get("First Line"));
                text2.setText(hosts.get(position).get("Second Line").replace("[]",""));

                return view;
            }
        };
        //listViewLocal.setAdapter(this.hostsAdapter);
        //frameLayout.addView(listViewLocal);
        listViewLocal.setAdapter(hostsAdapter);

    }

    private void setupHostDiscovery() {

        setupHostsAdapter();
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

    public void loadAdInter() {
        AdRequest adRequest = new AdRequest.Builder().build();
        String id = getString(R.string.interstitial_ad_unit_id);


        mInterstitialAd.load(getActivity(), id, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;

                    }
                });
    }

    public void showInterstitial(){

        if (mInterstitialAd != null) {
            mInterstitialAd.show(requireActivity());
        }else if(firstShowAd){
         //   startShowInterstitial();
        }
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
                    stopProgressBar();
                }

            }
        });
    }

    @Override
    public void processFinish(int output) {

    }

}
