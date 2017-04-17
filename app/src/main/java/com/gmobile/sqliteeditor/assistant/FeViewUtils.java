package com.gmobile.sqliteeditor.assistant;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.PopupMenu;

import com.gmobile.library.base.view.customview.bottomview.BottomTip;
import com.gmobile.library.base.view.customview.dialog.FeDialog;
import com.gmobile.library.base.view.operation.ToastManger;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.ui.activity.DbTablesActivity;
import com.google.android.gms.ads.AdListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by liucheng on 2015/10/20.
 */
public class FeViewUtils {

    private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static int appBarHeight = 0;

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenHeight = dm.heightPixels;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
        }
        return screenWidth;
    }

    public static void setAppBarHeight(int height) {
        appBarHeight = height;
    }

    public static int getAppBarHeight() {
        return appBarHeight;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        if (context != null) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public static void showMsg(View view, CharSequence msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void showLongMsg(View view, CharSequence msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void showMsg(Context context, View view, int strId) {
        Snackbar.make(view, context.getResources().getText(strId), Snackbar.LENGTH_SHORT).show();
    }

    public static void listItemUpAnim(View view, int position,
                                      AnimatorListenerAdapter animatorListenerAdapter) {
        view.setTranslationY(150);
        view.setAlpha(0.f);
        ViewPropertyAnimator animate = view.animate();
        animate.translationY(0).alpha(1.f)
                .setStartDelay(20 * (position))
                .setInterpolator(new DecelerateInterpolator(2.f))
                .setDuration(400);

        if (animatorListenerAdapter != null) {
            animate.setListener(animatorListenerAdapter);
        }

        animate.withLayer().start();
    }

    public static void listItemUpAnimSlow(View view, int position,
                                          AnimatorListenerAdapter animatorListenerAdapter) {
        view.setTranslationY(150);
        view.setAlpha(0.f);
        ViewPropertyAnimator animate = view.animate();
        animate.translationY(0).alpha(1.f)
                .setStartDelay(70 * (position))
                .setInterpolator(new DecelerateInterpolator(2.f))
                .setDuration(400);

        if (animatorListenerAdapter != null) {
            animate.setListener(animatorListenerAdapter);
        }

        animate.withLayer().start();
    }

    public static Drawable getTintDrawable(Drawable drawable, int color) {
        Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrap, color);
        return wrap;
    }

    public static void showPopMenu(Context context,View view ,int menuId, android.support.v7.widget.PopupMenu.OnMenuItemClickListener listener){

        android.support.v7.widget.PopupMenu popupMenu = new android.support.v7.widget.PopupMenu(context,view);
        popupMenu.getMenuInflater().inflate(menuId, popupMenu.getMenu());
        //menu item的点击事件监听
        popupMenu.setOnMenuItemClickListener(listener);
        popupMenu.show();
    }

    public static AdListener getAdMobClick(final Button btn_bg) {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                btn_bg.setVisibility(View.GONE);
            }
        };
    }

    public static void createMsgDialog(Context context, int msgId, int cancelId,
                                       int okId, final Runnable okRun) {
        FeDialog.Builder builder = new FeDialog.Builder(context);
        builder.setMessage(msgId);
        builder.setNeutralButton(cancelId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(okId,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (okRun != null) {
                            okRun.run();
                        }
                    }
                });

        builder.create().show();
    }

    public static BottomTip createLoadingDialog(Context context, String msg) {
        BottomTip.Builder tipBuilder = new BottomTip.Builder(context);
        tipBuilder.setMessage(msg).setButtonVisibility(View.GONE);

        return tipBuilder.create();
    }


    public static void sendFileShortcut(Context context,File file) {
        String[] keys = {"PATH"};
        String[] values = {file.getAbsolutePath()};
        int iconId = R.drawable.ic_database;

        sendShortcutIntent(context,file.getName(), iconId, getKeyValuePairs(keys, values));
    }

    private static Map<String, String> getKeyValuePairs(String[] keys, String values[]) {
        if (keys == null || values == null) {
            return null;
        }

        if (keys.length != values.length) {
            return null;
        }

        Map<String, String> pairs = new HashMap<>();
        int count = keys.length;
        for (int i = 0; i < count; i++) {
            pairs.put(keys[i], values[i]);
        }

        return pairs;
    }

    private static void sendShortcutIntent(Context context, String name, int iconId, Map<String, String> keyValuePairs) {
        if (context == null || name == null) {
            return;
        }

        if (name.length() == 0) {
            name = File.separator;
        }

        File file = null;
        String filePath = null;
        if (keyValuePairs != null) {
            filePath = keyValuePairs.get("PATH");
            file = new File(filePath);
        }

        Intent intent;
        intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ComponentName componentName = new ComponentName("com.gmobile.sqliteeditor", "com.gmobile.sqliteeditor.ui.activity.DbTablesActivity");
        intent.setComponent(componentName);

        Bundle bundle = new Bundle();
        bundle.putString(DbTablesActivity.DB_PATH, filePath);
        bundle.putString(DbTablesActivity.DB_NAME,file.getName());
        intent.putExtras(bundle);

        if (intent == null) {
            ToastManger.showErrorToast((Activity) context, R.string.create_to_desktop_failed);
            return;
        }

        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, iconId));
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        context.sendBroadcast(shortcutIntent);

        ToastManger.showDoneToast((Activity) context, R.string.create_to_desktop_success);
    }

}
