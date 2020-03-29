package ru.myandroidhelper.quizapp.checkyourself.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ru.myandroidhelper.quizapp.checkyourself.R;
import ru.myandroidhelper.quizapp.checkyourself.adapters.CategoryAdapter;
import ru.myandroidhelper.quizapp.checkyourself.constants.AppConstants;
import ru.myandroidhelper.quizapp.checkyourself.data.sqlite.NotificationDbController;
import ru.myandroidhelper.quizapp.checkyourself.listeners.ListItemClickListener;
import ru.myandroidhelper.quizapp.checkyourself.models.notification.NotificationModel;
import ru.myandroidhelper.quizapp.checkyourself.models.quiz.CategoryModel;
import ru.myandroidhelper.quizapp.checkyourself.utilities.ActivityUtilities;
import ru.myandroidhelper.quizapp.checkyourself.utilities.AdsUtilities;
import ru.myandroidhelper.quizapp.checkyourself.utilities.AppUtilities;
import ru.myandroidhelper.quizapp.checkyourself.utilities.DialogUtilities;

public class MainActivity extends BaseActivity implements DialogUtilities.OnCompleteListener, BillingProcessor.IBillingHandler{

    private Activity activity;
    private Context context;

    private Toolbar toolbar;

    private RelativeLayout mNotificationView;
    private AccountHeader header = null;
    private Drawer drawer = null;

    private ArrayList<CategoryModel> categoryList;
    private CategoryAdapter adapter = null;
    private RecyclerView recyclerView;

    CheckSubscribe checks;
    BillingProcessor bp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity = MainActivity.this;
        context = getApplicationContext();
        categoryList = new ArrayList<>();


        mNotificationView = (RelativeLayout) findViewById(R.id.notificationView);
        recyclerView = (RecyclerView) findViewById(R.id.rvContentScore);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

        adapter = new CategoryAdapter(context, activity, categoryList);
        recyclerView.setAdapter(adapter);

        initLoader();
        loadData();
        initListener();

        final IProfile profile = new ProfileDrawerItem().withIcon(R.drawable.question);

        header = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                //.withHeaderBackground(R.layout.n)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        ActivityUtilities.getInstance().invokeCustomUrlActivity(activity, CustomUrlActivity.class,
                                getResources().getString(R.string.site), getResources().getString(R.string.site_url), false);
                        return false;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .addProfiles(profile)
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(header)
                .addDrawerItems(
                       /* new PrimaryDrawerItem().withName("О приложении").withIcon(R.drawable.ic_dev).withIdentifier(10).withSelectable(false),


                        new SecondaryDrawerItem().withName("Facebook").withIcon(R.drawable.ic_facebook).withIdentifier(21).withSelectable(false),
                        new SecondaryDrawerItem().withName("Twitter").withIcon(R.drawable.ic_twitter).withIdentifier(22).withSelectable(false),
                        new SecondaryDrawerItem().withName("Google+").withIcon(R.drawable.ic_google_plus).withIdentifier(23).withSelectable(false),*/

                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Настройки").withIcon(R.drawable.ic_settings).withIdentifier(30).withSelectable(false),
                        new SecondaryDrawerItem().withName("Оцените приложение").withIcon(R.drawable.ic_rating).withIdentifier(31).withSelectable(false),
                        new SecondaryDrawerItem().withName("Поделитесь").withIcon(R.drawable.ic_share).withIdentifier(32).withSelectable(false),
                     //   new SecondaryDrawerItem().withName("Результат").withIcon(R.drawable.ic_check_box_black_24dp).withIdentifier(33).withSelectable(false),

                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Выход").withIcon(R.drawable.ic_exit).withIdentifier(40).withSelectable(false)


                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 10) {
                                ActivityUtilities.getInstance().invokeNewActivity(activity, AboutDevActivity.class, false);
                            } else if (drawerItem.getIdentifier() == 30) {
                                ActivityUtilities.getInstance().invokeNewActivity(activity, SettingsActivity.class, true);
                            } else if (drawerItem.getIdentifier() == 31) {
                                AppUtilities.rateThisApp(activity);
                            } else if (drawerItem.getIdentifier() == 32) {
                                AppUtilities.shareApp(activity);
                           // } else if (drawerItem.getIdentifier() == 33) {
                              //  ActivityUtilities.getInstance().invokeCustomUrlActivity(activity, ScoreCardActivity.class,
                                     //   getResources().getString(R.string.privacy), getResources().getString(R.string.privacy_url), false);
                            } else if (drawerItem.getIdentifier() == 40) {

                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(false)
                .withShowDrawerUntilDraggedOpened(false)
                .build();

        checks = new CheckSubscribe();
        checks.execute();
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            AppUtilities.tapPromtToExit(this);
        }
    }

    private void loadData() {
        showLoader();
        loadJson();

        // show banner ads
        AdsUtilities.getInstance(context).showBannerAd((AdView) findViewById(R.id.adsView));
    }

    private void loadJson() {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try{
            br = new BufferedReader(new InputStreamReader(getAssets().open(AppConstants.CONTENT_FILE)));
            String temp;
            while ((temp = br.readLine()) != null)
                sb.append(temp);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parseJson(sb.toString());
    }

    private void parseJson(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray(AppConstants.JSON_KEY_ITEMS);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                String categoryId = object.getString(AppConstants.JSON_KEY_CATEGORY_ID);
                String categoryName = object.getString(AppConstants.JSON_KEY_CATEGORY_NAME);

                categoryList.add(new CategoryModel(categoryId, categoryName));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hideLoader();
        adapter.notifyDataSetChanged();
    }

    private void initListener() {

        //notification view click listener
        mNotificationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtilities.getInstance().invokeNewActivity(activity, NotificationListActivity.class, false);
            }
        });

        // recycler list item click listener
        adapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {

                //show snackbar and return if not purchased

                boolean purchased = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(AppConstants.PRODUCT_ID_BOUGHT, false);
                boolean subscribed = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(AppConstants.PRODUCT_ID_SUBSCRIBE, false);

                if (position > 3 && !purchased && !subscribed) {
                    // Toast.makeText(mActivity, R.string.alert_for_purchase , Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, R.string.alert_for_purchase, Snackbar.LENGTH_LONG)
                            .setAction("Ок", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityUtilities.getInstance().invokeNewActivity(activity, SettingsActivity.class, true);
                                }
                            }).setDuration(4000).show();
                    return;
                }

                CategoryModel model = categoryList.get(position);
                ActivityUtilities.getInstance().invokeCommonQuizActivity(activity, QuizPromptActivity.class, model.getCategoryId(), true);
            }
        });
    }
    // received new broadcast
    private BroadcastReceiver newNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            initNotification();
        }
    };

    public void initNotification() {
        NotificationDbController notificationDbController = new NotificationDbController(context);
        TextView notificationCount = (TextView) findViewById(R.id.notificationCount);
        notificationCount.setVisibility(View.INVISIBLE);

        ArrayList<NotificationModel> notiArrayList = notificationDbController.getUnreadData();

        if (notiArrayList != null && !notiArrayList.isEmpty()) {
            int totalUnread = notiArrayList.size();
            if (totalUnread > 0) {
                notificationCount.setVisibility(View.VISIBLE);
                notificationCount.setText(String.valueOf(totalUnread));
            } else {
                notificationCount.setVisibility(View.INVISIBLE);
            }
        }

    }



    @Override
    protected void onResume() {
        super.onResume();

        //register broadcast receiver
        IntentFilter intentFilter = new IntentFilter(AppConstants.NEW_NOTI);
        LocalBroadcastManager.getInstance(this).registerReceiver(newNotificationReceiver, intentFilter);

        initNotification();

        // load full screen ad
       AdsUtilities.getInstance(context).loadFullScreenAd(activity);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

        if (bp.isSubscribed(SUBSCRIPTION_ID())) {
            setIsSubscribe(true, context);
            Log.v("TAG", "Subscribe actually restored");

        } else {
            setIsSubscribe(false, context);
        }
    }

    private String SUBSCRIPTION_ID(){
        return getResources().getString(R.string.subscription_id);
    }
    public void setIsSubscribe(boolean purchased, Context c){
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(c);

        SharedPreferences.Editor editor= prefs.edit();

        editor.putBoolean(AppConstants.PRODUCT_ID_SUBSCRIBE, purchased);
        editor.apply();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    public void onComplete(Boolean isOkPressed, String viewIdText) {
        if (isOkPressed) {
            if (viewIdText.equals(AppConstants.BUNDLE_KEY_EXIT_OPTION)) {
                activity.finishAffinity();
            }
        }

    }

    private class CheckSubscribe extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String license = getResources().getString(R.string.google_play_license);
            bp = new BillingProcessor(context, license, MainActivity.this);
            bp.loadOwnedPurchasesFromGoogle();
            return null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newNotificationReceiver);

    }


}



