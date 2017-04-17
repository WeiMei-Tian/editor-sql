package com.gmobile.sqliteeditor.assistant;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.gmobile.library.base.assistant.utils.FileUtils;
import com.gmobile.sqliteeditor.R;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liucheng on 2015/11/2.
 */
public class FeThumbUtils {


    public static final ConcurrentHashMap<String, Integer> thumbArray = new ConcurrentHashMap<>();

    public static final float HIDE_FILE_ALPHA = 0.48f;
    public static final float NORMAL_FILE_ALPHA = 1f;

    static {
        int thumb;

        //image
        thumb = R.drawable.ic_image_main;
        add("application/vnd.oasis.opendocument.graphics", thumb);
        add("application/vnd.oasis.opendocument.graphics-template", thumb);
        add("application/vnd.oasis.opendocument.image", thumb);
        add("application/vnd.stardivision.draw", thumb);
        add("application/vnd.sun.xml.draw", thumb);
        add("application/vnd.sun.xml.draw.template", thumb);
        add("image/jpeg", thumb);
        add("image/png", thumb);

        //music
        thumb = R.drawable.ic_music_main;
        add("application/ogg", thumb);
        add("audio/mpeg", thumb);

        //video

        //pdf
        thumb = R.drawable.ic_pdf_main;
        add("application/pdf", thumb);

        thumb = R.drawable.ic_txt_icon;
        add("text/plain", thumb);

        //zip
        thumb = R.drawable.ic_zip_main;
        add("application/zip", thumb);
        add("application/rar", thumb);
        add("application/x-7z-compressed", thumb);
        add("application/x-tar", thumb);
        add("application/x-gtar", thumb);
        add("application/java-archive", thumb);

        //doc
        thumb = R.drawable.ic_word_main;
        add("application/msword", thumb);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.template", thumb);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document", thumb);

        //apk
        thumb = R.drawable.ic_apk_main;
        add("application/vnd.android.package-archive", thumb);

        //link file
        thumb = R.drawable.ic_link;
        add("link", thumb);

        //ppt
        thumb = R.drawable.ic_ppt_main;
        add("application/vnd.ms-powerpoint", thumb);
        add("application/vnd.openxmlformats-officedocument.presentationml.presentatio", thumb);
        add("application/vnd.openxmlformats-officedocument.presentationml.template", thumb);
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation", thumb);

        //excel
        thumb = R.drawable.ic_xlc_main;
        add("application/vnd.ms-excel", thumb);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", thumb);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.template", thumb);

        //folder
        thumb = R.drawable.ic_folder_main;
        add("folder", thumb);
    }

    private static void add(String mimeType, int resId) {
        if (thumbArray.put(mimeType, resId) != null) {
            throw new RuntimeException(mimeType + " already registered!");
        }
    }

    public static Bitmap getDefaultThumb(Context context, String mimeType) {
        return BitmapFactory.decodeResource(context.getResources(), getDefaultThumbRes(mimeType));
    }

    public static int getDefaultThumbRes(String mimeType) {
        int resId;

        try {
            if (mimeType.startsWith("audio")) {
                resId = R.drawable.ic_music_main;
            } else if (mimeType.startsWith("video")) {
                resId = R.drawable.ic_video_main;
            } else if(mimeType.startsWith("db")){
                resId = R.drawable.ic_database;
            } else if(mimeType.startsWith("sqlite")){
                resId = R.drawable.ic_database;
            } else {
                resId = thumbArray.get(mimeType);
            }

        } catch (Exception e) {
            resId = R.drawable.ic_alldocument_main;
        }

        return resId;
    }

    /**
     * 从系统媒体库获取图片，视频，音乐，apk文件的缩略图
     *
     * @param context  Context
     * @param mimeType 文件类型
     * @param path     文件路径
     * @param isList   是否为列表模式
     * @return 缩略图
     */
    public static Bitmap getThumbFromDb(Context context, String mimeType, String path, boolean isList) {

        int width = isList ? 40 : 104;
        int height = isList ? 40 : 72;
        int thumbnailsValues = MediaStore.Images.Thumbnails.MINI_KIND;
        if (mimeType.startsWith("image")) {
            return ThumbnailUtils.extractThumbnail(getThumbImage(context, path, thumbnailsValues), FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height));
        } else if (mimeType.startsWith("video")) {
            return ThumbnailUtils.extractThumbnail(getVideoImage(context, path, thumbnailsValues), FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height));
        } else if (mimeType.startsWith("audio") || mimeType.equals("application/ogg")) {
            return ThumbnailUtils.extractThumbnail(getMusicThumb(path), FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height));
        } else if (mimeType.equals("application/vnd.android.package-archive")) {
            //app 图标在平铺模式下 56dp  设计规定
            width = isList ? 40 : 56;
            height = isList ? 40 : 56;
            Bitmap bitmap = getLocalApkIcon(context.getPackageManager(), path);
            if (bitmap != null) {
                return FeThumbUtils.scaleBitMap(bitmap, FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height));
            }

            return null;
        }

        return null;
    }

    /**
     * 直接生成图片，视频，音乐，apk文件的缩略图
     *
     * @param context  context
     * @param mimeType 文件类型
     * @param path     文件路径
     * @param isList   是否为列表模式
     * @return 缩略图
     */
    public static Bitmap createThumb(Context context, String mimeType, String path, boolean isList) {

        int width = isList ? 40 : 104;
        int height = isList ? 40 : 72;
        int thumbnailsValues = MediaStore.Images.Thumbnails.MINI_KIND;
        if (mimeType.startsWith("image")) {

            return createImageThumbnailBySize(context, path, FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height), thumbnailsValues);

        } else if (mimeType.startsWith("video")) {

            return ThumbnailUtils.extractThumbnail(createVideoThumb(path),
                    FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height));

        } else if (mimeType.startsWith("audio") || mimeType.equals("application/ogg")) {

            return ThumbnailUtils.extractThumbnail(getMusicThumb(path),
                    FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height));

        } else if (mimeType.equals("application/vnd.android.package-archive")) {
            //app 图标在平铺模式下 56dp  设计规定
            width = isList ? 40 : 56;
            height = isList ? 40 : 56;
            Bitmap bitmap = getLocalApkIcon(context.getPackageManager(), path);
            if (bitmap != null) {
                return FeThumbUtils.scaleBitMap(bitmap, FeViewUtils.dpToPx(width), FeViewUtils.dpToPx(height));
            }

            return null;
        }

        return null;
    }

    public static Bitmap getThumbImage(Context ctx, String filePath, int kind) {
        try {
            String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA};

            Cursor cursor = MediaStore.Images.Media.query(
                    ctx.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Images.Media.DATA + "=?",
                    new String[]{filePath}, MediaStore.Images.Media._ID);

            if (cursor == null || cursor.getCount() <= 0)
                return null;

            cursor.moveToFirst();
            int imgId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Images.Media._ID));
            cursor.close();
            return MediaStore.Images.Thumbnails.getThumbnail(
                    ctx.getContentResolver(), imgId,
                    kind, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取音乐thumb
     *
     * @param filePath 文件路径
     * @return 对应的缩略图
     */
    public static Bitmap getMusicThumb(String filePath) {
        Bitmap bitmap = null;

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            byte[] art = retriever.getEmbeddedPicture();

            if (art != null) {
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            }
            retriever.release();

            return bitmap;
        } catch (Exception e) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            System.gc();
            return null;
        } catch (OutOfMemoryError o) {
            System.gc();
            return null;
        }
    }

    public static Bitmap getVideoImage(Context context, String path, int thumbnailsValues) {

        ContentResolver crThumb = context.getContentResolver();
        Cursor cursor = crThumb.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.MediaColumns.DATA + "=?", new String[]{path},
                null);
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            long _id = cursor.getLong(0);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bit = MediaStore.Video.Thumbnails.getThumbnail(crThumb, _id,
                    thumbnailsValues, options);
            cursor.close();
            return bit;
        } else {
            cursor.close();
            return null;
        }
    }

    public static Bitmap getLocalApkIcon(PackageManager pm, String apkPath) {
        Drawable icon;
        Bitmap iconBmp;
        try {
            PackageInfo pi = pm.getPackageArchiveInfo(apkPath,
                    PackageManager.GET_ACTIVITIES);
            pi.applicationInfo.sourceDir = apkPath;
            pi.applicationInfo.publicSourceDir = apkPath;
            icon = pi.applicationInfo.loadIcon(pm);
            iconBmp = drawableToBitmap(icon);
        } catch (Exception e) {
            return null;
        }

        return iconBmp;
    }

    public static Bitmap drawableToBitmap(Drawable thumb) {
        if (thumb == null) {
            return null;
        }

        Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (thumb instanceof VectorDrawable) {
                int w = thumb.getIntrinsicWidth();
                int h = thumb.getIntrinsicHeight();
                Bitmap.Config config = thumb.getOpacity() != PixelFormat.OPAQUE ?
                        Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                bitmap = Bitmap.createBitmap(w, h, config);
                Canvas canvas = new Canvas(bitmap);
                thumb.setBounds(0, 0, w, h);
                thumb.draw(canvas);
            } else {
                bitmap = ((BitmapDrawable) thumb).getBitmap();
            }
        } else {
            bitmap = ((BitmapDrawable) thumb).getBitmap();
        }
        return bitmap;
    }

    /**
     * Constant used to indicate the dimension of mini thumbnail.
     */
    public static final int TARGET_SIZE_MINI_THUMBNAIL = 320;
    /**
     * Constant used to indicate the dimension of micro thumbnail.
     */
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 60;
    public static final int OPTIONS_RECYCLE_INPUT = 0x2;
    private static final int MAX_NUM_PIXELS_THUMBNAIL = 512 * 384;
    private static final int MAX_NUM_PIXELS_MICRO_THUMBNAIL = 60 * 60;
    private static final int UNCONSTRAINED = -1;
    /* Options used internally. */
    private static final int OPTIONS_NONE = 0x0;
    private static final int OPTIONS_SCALE_UP = 0x1;

    /**
     * This method first examines if the thumbnail embedded in EXIF is bigger
     * than our target size. If not, then it'll create a thumbnail from original
     * image. Due to efficiency consideration, we want to let MediaThumbRequest
     * avoid calling this method twice for both kinds, so it only requests for
     * MICRO_KIND and set saveImage to true.
     * <p/>
     * This method always returns a "square thumbnail" for MICRO_KIND thumbnail.
     *
     * @param filePath the path of image file
     * @param kind     could be MINI_KIND or MICRO_KIND
     * @return Bitmap
     * internally.
     */
    public static Bitmap createImageThumbnailForCloud(Context ctx, String filePath, int kind) {
        Bitmap bitmap;
        try {
            bitmap = getImageThumbnail(filePath, 80, 80);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError o) {
            System.gc();
            try {
                bitmap = createImageThumbnailThrowErrorOrException(ctx,
                        filePath, kind);
                return bitmap;
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
        return bitmap;
    }

    public static Bitmap getThumbFromSystem(Context ctx, String filePath, int kind) {
        try {
            String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA};

            Cursor cursor = MediaStore.Images.Media.query(
                    ctx.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Images.Media.DATA + "=?",
                    new String[]{filePath}, MediaStore.Images.Media._ID);

            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            cursor.moveToFirst();
            int imgId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Images.Media._ID));
            cursor.close();
            return MediaStore.Images.Thumbnails.getThumbnail(
                    ctx.getContentResolver(), imgId,
                    kind, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getThumbFromThumbPath(Context ctx, String filePath, int kind) {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeFile(filePath);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 80, 80,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError o) {
            System.gc();
            try {
                bitmap = createImageThumbnailThrowErrorOrException(ctx,
                        filePath, kind);
                return bitmap;
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
        return bitmap;
    }

    private static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {
            bitmap = BitmapFactory.decodeFile(imagePath, options);

            int h = options.outHeight;
            int w = options.outWidth;
            int beWidth = w / width;
            int beHeight = h / height;
            int be;
            if (beWidth < beHeight) {
                be = beWidth;
            } else {
                be = beHeight;
            }
            if (be <= 0) {
                be = 1;
            }
            options.inSampleSize = be;
            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeFile(imagePath, options);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        } catch (OutOfMemoryError e) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        return bitmap;
    }

    public static Bitmap createImageThumbnailThrowErrorOrException(Context ctx,
                                                                   String filePath, int kind) {
        boolean wantMini = (kind == MediaStore.Images.Thumbnails.MINI_KIND);
        int targetSize = wantMini ? TARGET_SIZE_MINI_THUMBNAIL
                : TARGET_SIZE_MICRO_THUMBNAIL;
        int maxPixels = wantMini ? MAX_NUM_PIXELS_THUMBNAIL
                : MAX_NUM_PIXELS_MICRO_THUMBNAIL;
        Bitmap bitmap = getThumbFromSystem(ctx, filePath, kind);
        if (bitmap == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            options.inSampleSize = computeSampleSize(options, targetSize, maxPixels);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeFile(filePath, options);

            if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
                bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL,
                        TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
            }
        }
        return bitmap;
    }

    public static Bitmap createImageThumbnailBySize(Context ctx, String filePath, int width, int height, int kind) {
        Bitmap bitmap;
        try {
            bitmap = getImageThumbnail(filePath, width, height);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError o) {
            System.gc();
            try {
                bitmap = createImageThumbnailThrowErrorOrException(ctx,
                        filePath, kind);
                return bitmap;
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
        return bitmap;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
                .ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
                .min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == UNCONSTRAINED)
                && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height,
                                          int options) {
        if (source == null) {
            return null;
        }
        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return transform(matrix, source, width, height,
                OPTIONS_SCALE_UP | options);
    }

    private static Bitmap transform(Matrix scaler, Bitmap source,
                                    int targetWidth, int targetHeight, int options) {
        boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
        boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
             * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
            Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b2);
            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
                    + Math.min(targetWidth, source.getWidth()), deltaYHalf
                    + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth - src.width()) / 2;
            int dstY = (targetHeight - src.height()) / 2;
            Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
                    - dstY);
            c.drawBitmap(source, src, dst, null);
            if (recycle) {
                source.recycle();
            }
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();
        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect = (float) targetWidth / targetHeight;
        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        }
        Bitmap b1;
        if (scaler != null) {
            // this is used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                    source.getHeight(), scaler, true);
        } else {
            b1 = source;
        }
        if (recycle && b1 != source) {
            source.recycle();
        }
        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);
        Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
                targetHeight);
        if (b2 != b1) {
            if (recycle || b1 != source) {
                b1.recycle();
            }
        }
        return b2;
    }

    public static Bitmap createVideoThumb(String path) {
        try {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(path);
            Bitmap bitmap = media.getFrameAtTime();
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static Bitmap scaleBitMap(Bitmap bitmap, int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / bitmap.getWidth();
        float scaleHeight = ((float) newHeight) / bitmap.getHeight();
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
    }

    public static void setFileThumbAlpha(String name, ImageView thumb) {
        if (thumb != null) {
            thumb.setAlpha(FileUtils.isHide(name) ? HIDE_FILE_ALPHA : NORMAL_FILE_ALPHA);
        }
    }

    public static String getMiMeType(String name) {
        String type;
        String extension = name.substring(name.lastIndexOf(".") + 1);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        type = mime.getMimeTypeFromExtension(extension.toLowerCase());

        if (type == null) {
            switch (extension) {
                case "mkv":
                    type = "video/x-matroska";
                    break;

                case "xps":
                    type = "application/xps";
                    break;

                case "umd":
                    type = "application/umd";
                    break;

                case "chm":
                    type = "application/chm";
                    break;

                case "help":
                    type = "application/help";
                    break;

                case "epub":
                    type = "application/epub";
                    break;

                case "7z":
                    type = "application/x-7z-compressed";
                    break;

                case "jar":
                    type = "application/java-archive";
                    break;

                case "log":
                case "conf":
                case "config":
                case "ini":
                case "inf":
                case "sh":
                    type = "text/plain";
                    break;

                case "mp3":
                    type = "audio/mpeg";
                    break;

                case "wav":
                    type = "audio/x-wav";
                    break;

                case "au":
                case "snd":
                    type = "audio/basic";
                    break;
                case "mid":
                case "rmi":
                    type = "audio/mid";
                    break;

                case "aif":
                case "aifc":
                case "aiff":
                    type = "audio/x-aiff";
                    break;

                case "m3u":
                    type = "audio/x-mpegurl";
                    break;

                case "ra":
                case "ram":
                    type = "audio/x-pn-realaudio";
                    break;
                case "db":
                    type = "db";
                    break;
                case "sqlite":
                    type = "sqlite";
                    break;

                default:
                    type = "application/octet7z-compressedream";
                    break;
            }
        }

        return type;
    }

}
