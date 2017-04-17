package com.gmobile.sqliteeditor.assistant;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gmobile.library.base.assistant.utils.BuildUtils;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by admin on 2016/11/29.
 */
public class AdMobView {

    private RelativeLayout mAdmobLayout;
    private RelativeLayout mRootLayout;
    private Button mAdBg;
    private AdView mAdView;
    private Activity activity;
    private ImageView adClose;

    public AdMobView() {}

    public void init(RelativeLayout mAdmobLayout, RelativeLayout mRootLayout, Button mAdBg, AdView mAdView, ImageView adClose,Activity activity){
        this.mAdmobLayout = mAdmobLayout;
        this.mRootLayout = mRootLayout;
        this.mAdBg = mAdBg;
        this.mAdView = mAdView;
        this.activity = activity;
        this.adClose = adClose;
        initListener();
    }

    /**
     * 设置广告布局
     */
    public void setupGoogleAdMob() {
//        if (FeFunBase.hideAdMob()) {
//            hideAdMob();
//        } else {
        if (!BuildUtils.thanMarshmallow() || PermissionUtils.checkHasPermission(activity, PermissionUtils.READ_PHONE_STATE_PER[0])) {
            loadAdMob();
        }
//        }
        loadAdMob();
    }

    public void loadAdMob() {
        setupAdMobLayout(View.VISIBLE, 50);
        mAdBg.setVisibility(View.VISIBLE);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(FeViewUtils.getAdMobClick(mAdBg));
        }
    }

    public void hideAdMob() {
        setupAdMobLayout(View.GONE, 0);
    }

    public void setupAdMobLayout(int layoutVisibility, int bottomPadding) {
        mAdmobLayout.setVisibility(layoutVisibility);
        int bottomPaddingPx = FeViewUtils.dpToPx(bottomPadding);
        mRootLayout.setPadding(0, 0, 0, bottomPaddingPx);
    }

    public void controlAdMob(Bundle args) {
        boolean show = args.getBoolean(ViewEvent.Keys.ACTION);
        boolean isInAppSkin = args.getBoolean(ViewEvent.Keys.REFRESH_TYPE);
        if (show) {
            if (isInAppSkin) {
                addGoogleAdMobInApp();
                return;
            }
            setupGoogleAdMob();
        } else {
            hideAdMob();
        }
    }

    public void addGoogleAdMobInApp() {
        if (PermissionUtils.checkHasPermission(activity, PermissionUtils.READ_PHONE_STATE_PER[0])) {
            loadAdMob();
        }
    }

    private void initListener() {
        mAdBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("mAdBg","点击背景");
//                    StatisticsDataApp.recodeOther(StatisticsData.ActionId.MAIN_AD_CLICK);
                PackageManager mgr = activity.getPackageManager();
                Intent intent;
//                    if (PackageUtils.isPackageInstalled(ViewConstant.JIKE_ZHUSHOU_PKG, mgr)) {
//                        intent = mgr.getLaunchIntentForPackage(ViewConstant.JIKE_ZHUSHOU_PKG);
//                        if (intent != null) {
//                            startActivity(intent);
//                        }
//                    } else {
//                        if (PackageUtils.isPackageInstalled(ViewConstant.GOOGLE_PLAY_PKG, mgr)) {
//                            Intent launchIntent = mgr.getLaunchIntentForPackage(ViewConstant.GOOGLE_PLAY_PKG);
//                            ComponentName comp = new ComponentName(ViewConstant.GOOGLE_PLAY_PKG,
//                                    "com.google.android.finsky.activities.LaunchUrlHandlerActivity");
//                            launchIntent.setComponent(comp);
//                            launchIntent.setData(Uri.parse("market://details?id=" + ViewConstant.JIKE_ZHUSHOU_PKG));
//                            startActivity(launchIntent);
//                        } else {
//                            String url = LanguageUtils.isChinaBySystem() ? "http://www.jizhushou.com/" : "http://www.webpcsuite.com/";
//                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                            startActivity(intent);
//                        }
//                    }
            }
        });

        adClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("adClose","点击关闭按钮");
//                    FeFunBase.isCloseAdmobByMain = true;
//                    WeiXinPay.isInMainViewRemoveAd = true;
//                    if (!PurchaseUtil.goToBuyRemoveAd(DbTablesActivity.this)) {
//                        hideAdMob();
//
//                        boolean b = PreferenceUtils.getPrefBoolean(SettingConstant.HIDE_ADMOB,
//                                SettingConstant.HIDE_ADMOB_DEFAULT_VALUE);
//                        PreferenceUtils.setPrefBoolean(SettingConstant.HIDE_ADMOB, !b);
//
//                        EventBusHelper.controlAdMob(b, b);
//                    }
            }
        });
    }

}
