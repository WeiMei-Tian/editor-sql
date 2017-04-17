package com.gmobile.sqliteeditor.assistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmobile.library.base.assistant.utils.FileUtils;
import com.gmobile.library.base.assistant.utils.PreferenceUtils;
import com.gmobile.library.base.view.operation.ToastManger;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLManager;
import com.gmobile.sqliteeditor.constant.SqlConstant;
import com.gmobile.sqliteeditor.ui.activity.DbTablesActivity;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sg on 2016/11/23.
 */
public class Utils {

    public static boolean isRoot(Context context){
        boolean canRoot = ShellUtils.canExeRoot();
        if (!canRoot) {
            Observable.just(context)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Context>() {
                        @Override
                        public void call(Context context) {
                            DialogUtils.createMsgMiddleMsgDialog(context, R.string.needroot);
                        }
                    });
            return false;
        }

        return true;
    }

    public static void openDB(Activity mActivity, String dbFilePath, String dbName) {

        if (!isCanOpen(mActivity, dbFilePath)){
            return;
        }

        Intent intent = new Intent(mActivity, DbTablesActivity.class);
        intent.putExtra(DbTablesActivity.DB_PATH, dbFilePath);
        intent.putExtra(DbTablesActivity.DB_NAME, dbName);
        mActivity.startActivity(intent);
    }

    public static boolean isCanOpen(Activity mActivity, String dbFilePath){
        SQLManager.destroyManager();
        PreferenceUtils.setPrefString(mActivity, SqlConstant.SQL_PATH, dbFilePath);
        if (!SQLManager.getSQLHelper(mActivity).isDataBase) {
            ToastManger.showErrorToast(mActivity, R.string.open_db_fail);
            SQLManager.destroyManager();
            return false;
        }

        return true;
    }

    public static ArrayMap<String, String> suffixMap = new ArrayMap<String, String>() {
        {
            put("application/rar", ".rar");
            put("x-rar-compressed", ".rar");
            put("application/zip", ".zip");
            put("application/x-7z-compressed", ".7z");
            put("application/x-tar", ".tar");
            put("application/java-archive", ".jar");
            put("text/plain", ".txt");
            put("text/html", ".html");
            put("image/jpeg", ".jpeg");
            put("application/pdf", ".pdf");
        }
    };

    public static String saveAttachment(Context context, Intent intent) {
        int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        String tmpFileName = getCurrentSecondString() + suffixMap.get(intent.getType());
        String tmpFilePath = TmpFolderUtils.getAttachmentsFile(tmpFileName).getPath();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(intent.getData());
            if (inputStream == null) {
                return null;
            }

            OutputStream outputStream = new FileOutputStream(tmpFilePath);

            int count = 0;
            do {
                try {
                    count = inputStream.read(buffer, 0, bufferSize);
                    if (count != -1) {
                        outputStream.write(buffer, 0, count);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while (count != -1);

            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            tmpFilePath = null;
        }

        return tmpFilePath;
    }


    public final static String DATE_FORMAT_FOR_ATTACHMENTS = "yyyy-MM-dd-hh-mm-ss";
    public static String getCurrentSecondString() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_FOR_ATTACHMENTS, Locale.getDefault());
        return dateFormat.format(now);
    }

    public static String isFileExist(Context context, MaterialEditText fileName, String suffix, String parentPath) {
        return isFileNameOk(context, fileName, suffix, parentPath, true);
    }

    public static String isFileNameOk(Context context, MaterialEditText fileName, String suffix, String parentPath, boolean needCheckExist) {
        if (fileName.getText() == null) {
            fileName.setError(context.getString(R.string.empty_name_tip));
            return null;
        }

        String name = fileName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            fileName.setError(context.getString(R.string.empty_name_tip));
            return null;
        } else if (FileUtils.hasIllegalChar(name)) {
            fileName.setError(context.getString(R.string.file_name_illegal));
            return null;
        } else if (FileUtils.fileNameOnlyOne(name)) {
            fileName.setError(context.getString(R.string.file_name_unqualified));
            return null;
        }

        if (needCheckExist){
            File file = new File(parentPath, name + suffix);
            if (file.exists()) {
                fileName.setError(context.getString(R.string.same_name));
                return null;
            }
        }

        return name + suffix;
    }
    public static Point MeasureString(Context context, String text, float fontSize, int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        if (null == context || null == text || text.isEmpty() || 0 == fontSize) {
            return null;
        }

        TextView tv = new TextView(context);

        tv.setText(text);// 待测文本
        tv.setTextSize(fontSize);// 字体

        if (ViewGroup.LayoutParams.WRAP_CONTENT != widthMeasureSpec && ViewGroup.LayoutParams.MATCH_PARENT != widthMeasureSpec) {
            tv.setWidth(widthMeasureSpec);// 如果设置了宽度，字符串的宽度则为所设置的宽度
        }

        if (ViewGroup.LayoutParams.WRAP_CONTENT != heightMeasureSpec && ViewGroup.LayoutParams.MATCH_PARENT != heightMeasureSpec) {
            tv.setHeight(heightMeasureSpec);
        }

        tv.setSingleLine(false);// 多行

        tv.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        width = tv.getMeasuredWidth();
        height = tv.getMeasuredHeight();

        Point point = new Point();
        point.x = width;
        point.y = height;

        return point;
    }



}
