package org.ping.cool;

import android.annotation.SuppressLint;
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
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.RuntimeExecutionException;
import com.google.android.play.core.tasks.Task;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public static AssetManager assetManager;
    private boolean showAd = true;
    private SharedPreferences sharedPrefs;
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

        //The Return Ad is a new ad unit which is displayed once the user returns to your application
        // after a certain period of time
        StartAppSDK.init(this, getString(R.string.startapp_app_id), true);
        StartAppAd.disableSplash();
        sharedPrefs = getSharedPreferences("pingcool", MODE_PRIVATE);
        showAd = sharedPrefs.getBoolean("showAd",true);

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



    }


    @Override
    protected void onResume() {
        super.onResume();

        if (showAd) {
            loadAdMob();
            sharedPrefs.edit().putBoolean("showAd", false).apply();

        } else {
            loadAdStart();
            sharedPrefs.edit().putBoolean("showAd", true).apply();
        }
        loadAdMobExit();

    }

     public void refreshAutoCompleteTextView() {

        DBAdapter dbAdapter = new DBAdapter(this);
        urlHistoricList = dbAdapter.getAllValuesGlyphs();
        dbAdapter.close();

        if (urlHistoricList.size() > 0) {

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

        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this,R.style.Base_Theme_Material3_Dark_Dialog);
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
                    e.printStackTrace();

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
            rateApp();
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
        @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
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
                            Log.i("Rate Flow", "Complete");
                        }
                    });

                    flow.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            rateAppOnPlayStore();
                            Log.i("Rate Flow", "Fail");
                            e.printStackTrace();
                        }
                    });

                } else {
                    try {
                        @ReviewErrorCode int reviewErrorCode = ((RuntimeExecutionException) task.getException()).getErrorCode();
                        Log.e("Rate Task Fail", "code: " + reviewErrorCode);
                        rateAppOnPlayStore();
                    }catch (NullPointerException | ClassCastException e){
                        rateAppOnPlayStore();
                        e.printStackTrace();
                    }
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Log.e("Rate Request", "Fail");
                rateAppOnPlayStore();
            }
        });

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

    private void loadAdStart() {

        Banner startAppBanner = new Banner(this);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.linearLayoutAd.getLayoutParams();
        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        startAppBanner.setBannerListener(new BannerListener() {

            @Override
            public void onReceiveAd(View banner) {

            }

            @Override
            public void onFailedToReceiveAd(View view) {

                binding.linearLayoutAd.removeAllViews();

            }

            @Override
            public void onImpression(View view) {

            }

            @Override
            public void onClick(View view) {

            }

        });

        binding.linearLayoutAd.addView(startAppBanner);

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
                binding.linearLayoutAd.removeAllViews();
                binding.linearLayoutAd.addView(adView);


            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                binding.linearLayoutAd.removeAllViews();
                loadAdStart();
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
