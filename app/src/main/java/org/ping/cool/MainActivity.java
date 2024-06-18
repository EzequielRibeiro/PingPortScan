package org.ping.cool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import org.ping.cool.databinding.ActivityMainBinding;
import android.os.StrictMode;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public static AssetManager assetManager;
    public final static String FOOTER = "\nYou can report bugs through e-mail: aplicativoparamobile@gmail.com\nSoftware created by Ezequiel A. Ribeiro.\n";
    private ArrayAdapter<String> adapter;
    private List<UrlHistoric> urlHistoricList;
    private ArrayList<String> urlArray;
    private NavController navController;
    private MaterialAlertDialogBuilder materialAlertDialogBuilder;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        assetManager = getAssets();

        refreshAutoCompleteTextView();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /*List<String> testDeviceIds = Arrays.asList("EDA6CBEC34D7AE15BA471460139D49DA");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);*/


      rateApp();
      checkUpdate(MainActivity.this);

    }

    private void checkUpdate(Context context){

        Activity activity = (Activity) context;

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);

// Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("checkUpdate" + e.getMessage());

            }
        });

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlow(appUpdateInfo, activity, new AppUpdateOptions() {
                    @Override
                    public int appUpdateType() {
                        return AppUpdateType.IMMEDIATE;
                    }

                    @Override
                    public boolean allowAssetPackDeletion() {
                        return false;
                    }
                });
            }else{
                System.out.println("App is updated");

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        loadAdMob();
        loadAdMobExit();

    }

    private void rateApp() {

        final ReviewManager reviewManager = ReviewManagerFactory.create(MainActivity.this);
        //reviewManager = new FakeReviewManager(this);
        Task<ReviewInfo> request = reviewManager.requestReviewFlow();

        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull Task<ReviewInfo> task) {

                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = reviewManager.launchReviewFlow(MainActivity.this, reviewInfo);

                    flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.i("Rate Flow", "Result: "+task.isComplete());
                        }
                    });

                    flow.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.i("Rate Flow", "Fail");
                            System.err.println(e);
                        }
                    });

                } else {
                    try {
                        String reviewErrorCode = Objects.requireNonNull(task.getException()).getMessage();
                        Log.d("Rate Task Fail", "cause: " + reviewErrorCode);

                    } catch (NullPointerException | ClassCastException e) {
                        System.err.println(e.getMessage());

                    }
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                System.err.println(e.getMessage());
                Log.d("Rate Request", "Fail");

            }
        });

    }

     public void refreshAutoCompleteTextView() {

        DBAdapter dbAdapter = new DBAdapter(this);
        urlHistoricList = dbAdapter.getAllValuesGlyphs();
        dbAdapter.close();

        if (!urlHistoricList.isEmpty()) {

            urlArray = new ArrayList<>();

            for (UrlHistoric u : urlHistoricList) {
                urlArray.add(u.getText());
            }

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, urlArray);
            adapter.getFilter().filter(binding.autoCompleteTextUrl.getText(), null);
            binding.autoCompleteTextUrl.setAdapter(adapter);
        }
    }


    private void loadAdMobExit() {

        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this, R.style.Base_Theme_MaterialComponents_Light_Dialog_Alert);
        materialAlertDialogBuilder.setTitle(R.string.app_name);
        materialAlertDialogBuilder.setIcon(R.mipmap.ic_launcher_foreground);
        materialAlertDialogBuilder.setMessage("Close the application ?");
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //System.exit(1);
                        finishAffinity();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        materialAlertDialogBuilder = null;
                        adView = null;
                        loadAdMobExit();
                    }
                });

            AdRequest adRequest = new AdRequest.Builder().build();
            adView = new AdView(this);
            adView.setAdUnitId(getString(R.string.ad_banner_exit_id));
            adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    materialAlertDialogBuilder.setView(adView);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    //  loadAdStart();
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });



        }


    @Override
    public void onBackPressed() {

       if (navController.getCurrentDestination().getId() == R.id.FirstFragment) {
            if (materialAlertDialogBuilder != null) {
                try {

                      materialAlertDialogBuilder.show();

                }catch (IllegalStateException e){
                    System.err.println(e);

                }
            }
        } else
            super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_privacy) {
            Intent intent = new Intent(getBaseContext(), PrivacyPolicyHelp.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_rate) {
            rateAppOnPlayStore();
            return true;
        }

        if (id == R.id.action_about) {
            about();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void rateAppOnPlayStore(){
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void about() {

        TextView message = new TextView(MainActivity.this);
        message.setPadding(10, 0, 0, 0);
        String version = "v";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version += pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String msg = getString(R.string.app_name) + " " + version + "\nDeveloper: Ezequiel A. Ribeiro" + "\nContact: https://is.gd/supportcontact";
        final SpannableString s = new SpannableString(msg);
        Linkify.addLinks(s, Linkify.ALL);

        message.setText(s);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(message)
                .setTitle("About");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadAdMob() {
        /*List<String> testDeviceIds = Arrays.asList("DB530A1BBBDBFE8567328113528A19EF");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);*/

        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        adView.setAdSize(AdSize.LARGE_BANNER);
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                binding.linearLayoutAd.setVisibility(View.VISIBLE);
                binding.linearLayoutAd.removeAllViews();
                binding.linearLayoutAd.addView(adView);


            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                binding.linearLayoutAd.setVisibility(View.GONE);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });



    }




}
