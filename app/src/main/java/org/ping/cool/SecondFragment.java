package org.ping.cool;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.ping.cool.databinding.FragmentSecondBinding;

import java.util.ArrayList;

import static org.ping.cool.MainActivity.isOnline;
import static org.ping.cool.utils.logger.Logger.PutLogConsole;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private CheckPortTask checkPortTask;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        initView();
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      /*
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

       */

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


        binding.fabSecondFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.webViewPort.setVisibility(View.VISIBLE);

            }
        });


        binding.buttonScanPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listArgument.clear();
                if (checkPortTask != null)
                    if (checkPortTask.getStatus() == AsyncTask.Status.RUNNING) {
                        checkPortTask.cancel(true);
                        binding.buttonScanPort.setText("Scan");
                        binding.progressBarScan.setVisibility(View.INVISIBLE);
                        return;
                    }

                if (isOnline(getActivity().getApplicationContext())) {
                    if (!binding.editTextPortScan.getText().toString().isEmpty() &&
                            !binding.editTextPort1.getText().toString().isEmpty()) {

                        binding.webViewPort.setVisibility(View.GONE);
                        listArgument.add("-h " + binding.editTextPortScan.getText().toString());

                        if (!binding.editTextTimeout.getText().toString().isEmpty())
                            listArgument.add("-t " + binding.editTextTimeout.getText().toString());

                        if (!binding.editTextThreads.getText().toString().isEmpty())
                            listArgument.add("-th " + binding.editTextThreads.getText().toString());

                        listArgument.add("-p " + binding.editTextPort1.getText().toString());

                        String args[] = new String[listArgument.size()];
                        args = listArgument.toArray(args);

                        hideSoftwareKeyboard(binding.editTextPortScan);
                        new CheckPortTask(args, binding, SecondFragment.this).execute();


                    } else {
                        Toast.makeText(getActivity(), "type anything", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.buttonScanRangePort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listArgument.clear();
                if (checkPortTask != null)
                    if (checkPortTask.getStatus() == AsyncTask.Status.RUNNING) {
                        checkPortTask.cancel(true);
                        binding.buttonScanRangePort.setText("Scan");
                        binding.progressBarScan.setVisibility(View.INVISIBLE);
                        return;
                    }

                if (isOnline(getActivity().getApplicationContext())) {
                    if (!binding.editTextPortScan.getText().toString().isEmpty() &&
                            !binding.editTextPort2.getText().toString().isEmpty() &&
                            !binding.editTextPort3.getText().toString().isEmpty()) {

                        binding.webViewPort.setVisibility(View.GONE);
                        listArgument.add("-h " + binding.editTextPortScan.getText().toString());

                        if (!binding.editTextTimeout.getText().toString().isEmpty())
                            listArgument.add("-t " + binding.editTextTimeout.getText().toString());

                        if (!binding.editTextThreads.getText().toString().isEmpty())
                            listArgument.add("-th " + binding.editTextThreads.getText().toString());

                        listArgument.add("-p " + binding.editTextPort2.getText().toString() + "-" + binding.editTextPort3.getText().toString());

                        String args[] = new String[listArgument.size()];
                        args = listArgument.toArray(args);

                        hideSoftwareKeyboard(binding.editTextPortScan);
                        checkPortTask = new CheckPortTask(args, binding, SecondFragment.this);
                        checkPortTask.execute();

                    } else {
                        Toast.makeText(getActivity(), "type anything", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();
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

    }

    public void stopProgressBar() {
        binding.progressBarScan.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}