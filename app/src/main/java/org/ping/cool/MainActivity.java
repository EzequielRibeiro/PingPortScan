package org.ping.cool;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdProperties;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import org.ping.cool.databinding.ActivityMainBinding;
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

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public static AssetManager assetManager;
    private FirebaseAnalytics mFirebaseAnalytics;
    public  static boolean SHOWED = true;
    public final static String FOOTER = "\nYou can report bugs through e-mail: aplicativoparamobile@gmail.com\nSoftware created by Ezequiel A. Ribeiro.\n";
    private ArrayAdapter<String> adapter;
    private List<UrlHistoric> urlHistoricList;
    private ArrayList<String> urlArray;
    private AdRequest adRequest;
    private com.amazon.device.ads.AdLayout amazonAdView;
    private com.google.android.gms.ads.AdView admobAdView;
    private com.startapp.sdk.ads.banner.Banner startAppBanner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The Return Ad is a new ad unit which is displayed once the user returns to your application
        // after a certain period of time
        StartAppSDK.init(this, getString(R.string.startapp_app_id), true);
        StartAppAd.disableSplash();
        com.amazon.device.ads.AdRegistration.setAppKey(getString(R.string.amazon_ads_app_key));
        // com.amazon.device.ads.AdRegistration.enableTesting(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        assetManager = getAssets();
        /*
        List<String> testDeviceIds = Arrays.asList("DB530A1BBBDBFE8567328113528A19EF");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
         */

        refreshAutoCompleteTextView();

        adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);


        binding.adView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Log.i("Admob", "failed code " + adError.getCode() + ": " + adError.getMessage());
                binding.linearLayoutAd.removeView(binding.adView);
                loadAdStartApp();

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


    private void loadAdAmazon() {
        Log.i("Amazon","AmazonAd Banner");
        amazonAdView = new com.amazon.device.ads.AdLayout(this, com.amazon.device.ads.AdSize.SIZE_320x50);
        admobAdView = new com.google.android.gms.ads.AdView(this);
        admobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        admobAdView.setAdUnitId(getString(R.string.amazon_ads_app_key));

        if(startAppBanner != null)
            binding.linearLayoutAd.removeView(startAppBanner);

        binding.linearLayoutAd.addView(amazonAdView);
        amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());
        amazonAdView.setListener(new com.amazon.device.ads.AdListener() {
            @Override
            public void onAdLoaded(Ad ad, AdProperties adProperties) {

            }

            @Override
            public void onAdFailedToLoad(Ad ad, com.amazon.device.ads.AdError adError) {
                Log.i("AdAmazon", "failed code " + adError.getCode() + ": " + adError.getMessage());
            }

            @Override
            public void onAdExpanded(Ad ad) {

            }

            @Override
            public void onAdCollapsed(Ad ad) {

            }

            @Override
            public void onAdDismissed(Ad ad) {

            }
        });
    }



    private void loadAdStartApp() {
        Log.i("StartApp","loadAdStartapp");
        startAppBanner = new Banner(MainActivity.this, new BannerListener() {
            @Override
            public void onReceiveAd(View view) {
                Log.i("StartApp", "onReceived");
            }

            @Override
            public void onFailedToReceiveAd(View view) {

                Log.i("StartApp", "Failed To ReceiveAd");
                loadAdAmazon();

            }

            @Override
            public void onImpression(View view) {
                Log.i("StartApp", "ReceiveAd");

            }

            @Override
            public void onClick(View view) {

            }
        });
        binding.linearLayoutAd.addView(startAppBanner);

    }



    @Override
    public void onBackPressed() {
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


}
