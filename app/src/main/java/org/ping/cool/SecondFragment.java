package org.ping.cool;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.ping.cool.databinding.FragmentSecondBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.ping.cool.MainActivity.isOnline;
import static org.ping.cool.utils.logger.Logger.PutLogConsole;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private CheckPortTask checkPortTask;
    private MainActivity mainActivity;
    private AutoCompleteTextView autoCompleteTextViewUrl;
    private List<UrlHistoric> urlHistoricList;
    private ArrayAdapter<String> adapter;
    private SharedPreferences sharedPreferences;
    private String timeout, threads;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }

        this.autoCompleteTextViewUrl = mainActivity.findViewById(R.id.autoCompleteTextUrl);

        if (!sharedPreferences.contains("timeout")) {
            sharedPreferences.edit().putString("timeout", "1000").apply();
        }
        if (!sharedPreferences.contains("threads")) {
            sharedPreferences.edit().putString("threads", "32").apply();
        }
        timeout = sharedPreferences.getString("timeout", "1000");
        threads = sharedPreferences.getString("threads", "32");

        initView();
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        DBAdapter dbAdapter = new DBAdapter(getActivity());
        urlHistoricList = dbAdapter.getAllValuesGlyphs();

        if (!urlHistoricList.isEmpty()) {
            String[] urlArray = new String[urlHistoricList.size()];
            int i = 0;
            for (UrlHistoric u : urlHistoricList) {
                urlArray[i] = u.getText();
                i++;
            }
            adapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()),
                    android.R.layout.simple_spinner_dropdown_item, urlArray);

            try {
                autoCompleteTextViewUrl.setAdapter(adapter);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        dbAdapter.close();


    }
    private void textInput() {
        if (((MainActivity) getActivity()) instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }
        this.autoCompleteTextViewUrl = mainActivity.findViewById(R.id.autoCompleteTextUrl);

    }

    private void initView() {

        try {
            PutLogConsole(SecondFragment.this, binding.editTextTextLog, "Running version App: " + this.getActivity().getPackageManager()
                    .getPackageInfo(this.getActivity().getPackageName(), 0).versionName);
            PutLogConsole(SecondFragment.this, binding.editTextTextLog, "\nChecking parameters...");
        } catch (PackageManager.NameNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
        }
        //  binding.editTextPortScan.setText("www.google.com");
        binding.webViewPort.loadUrl("file:///android_asset/port.html");
        ArrayList<String> listArgument = new ArrayList<>();
        binding.fabSecondFragment.setVisibility(View.GONE);

        binding.editTextTimeout.setText(timeout);
        binding.editTextThreads.setText(threads);

        binding.fabSecondFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.webViewPort.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);

            }
        });


        binding.buttonScanRangePort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   textInput();
                if (!binding.editTextTimeout.getText().toString().isEmpty())
                    sharedPreferences.edit().putString("timeout", binding.editTextTimeout.getText().toString()).apply();

                if (!binding.editTextThreads.getText().toString().isEmpty())
                    sharedPreferences.edit().putString("threads", binding.editTextThreads.getText().toString()).apply();


                if (checkPortTask != null)
                    if (checkPortTask.getStatus() == AsyncTask.Status.RUNNING) {
                        checkPortTask.cancel(true);
                        stopProgressBar();
                        return;
                    }

                if (autoCompleteTextViewUrl != null)
                    if (autoCompleteTextViewUrl.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Enter a target to scan", Toast.LENGTH_SHORT).show();
                        return;
                    }

                if (binding.editTextPort2.getText().toString().isEmpty() &&
                        binding.editTextPort3.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Enter number ports to scan", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isOnline(requireActivity().getApplicationContext())) {

                    hideSoftwareKeyboard(autoCompleteTextViewUrl);
                    binding.buttonScanRangePort.setText("Stop");
                    //  binding.buttonScanRangePort.setTextColor(R.color.red_color);
                    DBAdapter dbAdapter = new DBAdapter(getActivity());
                    if (autoCompleteTextViewUrl != null)
                        if (dbAdapter.insertUrl(autoCompleteTextViewUrl.getText().toString(), "") > 0) {
                            mainActivity.refreshAutoCompleteTextView();
                        }
                    dbAdapter.close();

                    listArgument.clear();

                    binding.webViewPort.setVisibility(View.GONE);
                    if (autoCompleteTextViewUrl != null)
                        listArgument.add("-h " + autoCompleteTextViewUrl.getText().toString());

                    if (!binding.editTextTimeout.getText().toString().isEmpty())
                        listArgument.add("-t " + binding.editTextTimeout.getText().toString());

                    if (!binding.editTextThreads.getText().toString().isEmpty())
                        listArgument.add("-th " + binding.editTextThreads.getText().toString());


                    if (!binding.editTextPort2.getText().toString().isEmpty() &&
                            !binding.editTextPort3.getText().toString().isEmpty()) {

                        listArgument.add("-p " + binding.editTextPort2.getText().toString() + "-" + binding.editTextPort3.getText().toString());

                        String args[] = new String[listArgument.size()];
                        args = listArgument.toArray(args);

                        checkPortTask = new CheckPortTask(args, binding, SecondFragment.this);
                        checkPortTask.execute();

                    } else if (!binding.editTextPort2.getText().toString().isEmpty() &&
                            binding.editTextPort3.getText().toString().isEmpty()) {
                        listArgument.add("-p " + binding.editTextPort2.getText().toString());

                        String args[] = new String[listArgument.size()];
                        args = listArgument.toArray(args);

                        new CheckPortTask(args, binding, SecondFragment.this).execute();
                    } else if (binding.editTextPort2.getText().toString().isEmpty() &&
                            !binding.editTextPort3.getText().toString().isEmpty()) {
                        listArgument.add("-p " + binding.editTextPort3.getText().toString());

                        String args[] = new String[listArgument.size()];
                        args = listArgument.toArray(args);

                        new CheckPortTask(args, binding, SecondFragment.this).execute();
                    }
                } else {
                    Toast.makeText(getActivity(), "Without internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void hideSoftwareKeyboard(EditText currentEditText) {

        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void startProgressBar() {
        binding.progressBarScan.setVisibility(View.VISIBLE);
        binding.webViewPort.setVisibility(View.GONE);
        binding.fabSecondFragment.setVisibility(View.GONE);
    }

    public void stopProgressBar() {
        binding.progressBarScan.setVisibility(View.GONE);
        binding.fabSecondFragment.setVisibility(View.VISIBLE);
        binding.buttonScanRangePort.setText("Scan");
        //  binding.buttonScanRangePort.setTextColor(R.color.white_color);


    }

    private void stopTask() {
        if (checkPortTask != null)
            if (checkPortTask.getStatus() == AsyncTask.Status.RUNNING) {
                checkPortTask.cancel(true);
            }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        stopTask();
    }


}
