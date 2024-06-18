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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
      /*
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

       */

        /*OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.e("Pressed","pressed");
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);*/


        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }

        this.autoCompleteTextViewUrl = (AutoCompleteTextView) mainActivity.findViewById(R.id.autoCompleteTextUrl);
        DBAdapter dbAdapter = new DBAdapter(getActivity());
        urlHistoricList = dbAdapter.getAllValuesGlyphs();

        if (urlHistoricList.size() > 0) {
            String[] urlArray = new String[urlHistoricList.size()];
            int i = 0;
            for (UrlHistoric u : urlHistoricList) {
                urlArray[i] = u.getText();
                i++;
            }

            adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, urlArray);
            autoCompleteTextViewUrl.setAdapter(adapter);
        }
        dbAdapter.close();


    }


    private void initView() {

        try {
            PutLogConsole(SecondFragment.this, binding.editTextTextLog, "Running version App: " + this.getActivity().getPackageManager()
                    .getPackageInfo(this.getActivity().getPackageName(), 0).versionName);
            PutLogConsole(SecondFragment.this, binding.editTextTextLog, "\nChecking parameters...");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
                    DBAdapter dbAdapter = new DBAdapter(getActivity());
                    if (dbAdapter.insertUrl(autoCompleteTextViewUrl.getText().toString(), "") > 0) {
                        mainActivity.refreshAutoCompleteTextView();
                    }
                    dbAdapter.close();

                    listArgument.clear();

                    binding.webViewPort.setVisibility(View.GONE);
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
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
