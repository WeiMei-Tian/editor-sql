package com.gmobile.sqliteeditor.assistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;

import com.gmobile.library.base.assistant.utils.PackageUtils;
import com.gmobile.sqliteeditor.R;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class PermissionUtils {

    private PermissionUtils() {
    }

    public static final String[] INIT_CHECK_PERMISSONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS};

    public static final String[] GET_ACCOUNTS = new String[]{Manifest.permission.GET_ACCOUNTS};
    public static final int GET_ACCOUNTS_REQUEST = 4;

    public static final String[] WRITE_EXTERNAL_STORAGE_PER = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int WRITE_EXTERNAL_STORAGE_PER_REQUEST = 1;
    public static final String[] READ_PHONE_STATE_PER = new String[]{Manifest.permission.READ_PHONE_STATE};
    public static final int READ_PHONE_STATE_PER_REQUEST = 2;

    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean showRequestPermission(Activity context, String permisson) {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permisson);
    }

    public static boolean checkHasPermission(Activity mContext, String permisson) {
        if (ContextCompat.checkSelfPermission(mContext, permisson) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    public static void askStoragePermisson(final Activity context) {
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(context);
        alertDialogBuider.setTitle(R.string.storage_permission);
        alertDialogBuider.setMessage(R.string.storage_desc);
        alertDialogBuider.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(context, WRITE_EXTERNAL_STORAGE_PER, WRITE_EXTERNAL_STORAGE_PER_REQUEST);
            }
        });
        alertDialogBuider.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.finish();
            }
        });
        alertDialogBuider.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                    context.finish();
                }
                return false;
            }
        });
        alertDialogBuider.setCancelable(false);
        alertDialogBuider.create().show();
    }

    public static void setStoragePermisson(final Activity context) {
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(context);
        alertDialogBuider.setTitle(R.string.storage_permission);
        alertDialogBuider.setMessage(R.string.set_storage);
        alertDialogBuider.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openFeAppDesc(context);
                context.finish();
            }
        });
        alertDialogBuider.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.finish();
            }
        });
        alertDialogBuider.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                    context.finish();
                }
                return false;
            }
        });
        alertDialogBuider.setCancelable(false);
        alertDialogBuider.create().show();
    }

    public static void accountPermissonDesc(final Activity context) {
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(context);
        alertDialogBuider.setTitle(R.string.account_permission);
        alertDialogBuider.setMessage(R.string.account_permission_desc);
        alertDialogBuider.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(context, PermissionUtils.GET_ACCOUNTS,
                        PermissionUtils.GET_ACCOUNTS_REQUEST);
            }
        });
        alertDialogBuider.setNegativeButton(R.string.no, null);
        alertDialogBuider.create().show();
    }

    private static void openFeAppDesc(Context context) {
        Intent intent = new Intent();
        Uri uri = Uri.parse("package:" + "com.gmobile.sqliteeditor");
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void setAccountPermisson(final Context context) {
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(context);
        alertDialogBuider.setTitle(R.string.account_permission);
        alertDialogBuider.setMessage(R.string.set_account_permisson);
        alertDialogBuider.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openFeAppDesc(context);
            }
        });
        alertDialogBuider.setNegativeButton(R.string.cancel, null);
        alertDialogBuider.create().show();
    }

}
