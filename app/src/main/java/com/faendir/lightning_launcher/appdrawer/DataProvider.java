package com.faendir.lightning_launcher.appdrawer;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Lukas on 15.08.2015.
 * Exposes settings to lightning launcher
 */
public class DataProvider extends ContentProvider {

    private static final String TAG = DataProvider.class.getSimpleName();
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String AUTHORITY = "com.faendir.lightning_launcher.appdrawer.provider";

    private static final int ROWS = 0;
    private static final int COLUMNS = 1;
    private static final int ICON = 2;
    private static final int GROUP_ITEMS = 3;

    private static final int FULL_QUALITY = 100;
    private static final Random random = new Random();

    static {
        URI_MATCHER.addURI(AUTHORITY, "rows", ROWS);
        URI_MATCHER.addURI(AUTHORITY, "columns", COLUMNS);
        URI_MATCHER.addURI(AUTHORITY, "icon", ICON);
        URI_MATCHER.addURI(AUTHORITY,"groupItems",GROUP_ITEMS);
    }

    private SharedPreferences sharedPref;

    @Override
    public boolean onCreate() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        return true;

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (URI_MATCHER.match(uri)) {
            case ROWS: {
                MatrixCursor cursor = new MatrixCursor(new String[]{"rows"});
                cursor.addRow(new Integer[]{sharedPref.getInt(getContext().getString(R.string.pref_rows), 0)});
                return cursor;
            }
            case COLUMNS: {
                MatrixCursor cursor = new MatrixCursor(new String[]{"columns"});
                cursor.addRow(new Integer[]{sharedPref.getInt(getContext().getString(R.string.pref_columns), 0)});
                return cursor;
            }
            case ICON: {
                MatrixCursor cursor = new MatrixCursor(new String[]{"icon"});
                Drawable drawable = getIcon(ComponentName.unflattenFromString(selection));
                Bitmap bitmap = drawableToBitmap(drawable);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, FULL_QUALITY, stream);
                byte[] byteArray = stream.toByteArray();
                cursor.addRow(new byte[][]{byteArray});
                return cursor;
            }
            case GROUP_ITEMS: {
                MatrixCursor cursor = new MatrixCursor(new String[]{"groupItems"});
                cursor.addRow(new Integer[]{sharedPref.getBoolean(getContext().getString(R.string.pref_groupItems),false)?1:0});
                return cursor;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


    private Drawable getIcon(ComponentName name) {
        try {
            try {
                if (BuildConfig.DEBUG) Log.d(TAG, "Get: " + name.flattenToShortString());
                String iconPack = sharedPref.getString(getContext().getString(R.string.pref_iconPack), null);
                if (sharedPref.getBoolean(getContext().getString(R.string.pref_usePack), false) && iconPack != null) {
                    Resources resources = getContext().getPackageManager().getResourcesForApplication(iconPack);
                    AssetManager assetManager = resources.getAssets();
                    XmlPullParser parser;
                    if (Arrays.asList(assetManager.list("")).contains("appfilter.xml")) {
                        InputStream inputStream = assetManager.open("appfilter.xml");
                        XmlPullParserFactory f = XmlPullParserFactory.newInstance();
                        f.setNamespaceAware(true);
                        parser = f.newPullParser();
                        parser.setInput(inputStream, "utf-8");
                    } else {
                        parser = resources.getXml(resources.getIdentifier("appfilter", "xml", iconPack));
                    }
                    int eventType = parser.getEventType();
                    Drawable iconBack = null;
                    Drawable iconMask = null;
                    Drawable iconUpon = null;
                    double scale = 1;
                    while (eventType != XmlResourceParser.END_DOCUMENT) {
                        if (eventType == XmlResourceParser.START_TAG) {
                            try {
                                switch (parser.getName()) {
                                    case "item":
                                        String cmp = parser.getAttributeValue(0);
                                        ComponentName componentName = ComponentName.unflattenFromString(cmp.substring(cmp.indexOf('{') + 1, cmp.length() - 1));
                                        if (componentName != null && componentName.flattenToShortString().equals(name.flattenToShortString())) {
                                            if (BuildConfig.DEBUG)
                                                Log.d(TAG, "Trying to load " + parser.getAttributeValue(1));
                                            Drawable drawable = getDrawableFromIdentifier(resources, parser.getAttributeValue(1), iconPack);
                                            if (BuildConfig.DEBUG)
                                                Log.d(TAG, "Got themed icon from pack");
                                            return drawable;
                                        }
                                        break;
                                    case "iconback":
                                        iconBack = selectRandomDrawable(resources, parser, iconPack);
                                        break;
                                    case "iconmask":
                                        iconMask = selectRandomDrawable(resources, parser, iconPack);
                                        break;
                                    case "iconupon":
                                        iconUpon = selectRandomDrawable(resources, parser, iconPack);
                                        break;
                                    case "scale":
                                        scale = Double.valueOf(parser.getAttributeValue(0));
                                    default:
                                        break;
                                }
                            } catch (Exception ignored) {
                                if (BuildConfig.DEBUG) Log.d(TAG, "Ignored: ", ignored);
                            }
                        }
                        eventType = parser.next();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        Drawable icon = getContext().getPackageManager().getActivityIcon(name);
                        Bitmap iconBmp = drawableToBitmap(icon);
                        Bitmap dest = Bitmap.createBitmap(iconBmp.getWidth(), iconBmp.getHeight(), iconBmp.getConfig());
                        Canvas canvas = new Canvas(dest);
                        Paint paint = new Paint();
                        int distX = (int) Math.round((1 - scale) / 2 * iconBmp.getWidth());
                        int distY = (int) Math.round((1 - scale) / 2 * iconBmp.getHeight());
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
                        canvas.drawBitmap(iconBmp, new Rect(0, 0, iconBmp.getWidth(), iconBmp.getHeight()),
                                new Rect(distX, distY, iconBmp.getWidth() - distX, iconBmp.getHeight() - distY), paint);
                        if (BuildConfig.DEBUG) Log.d(TAG, "Scale: " + scale);
                        Rect size = new Rect(0, 0, iconBmp.getWidth(), iconBmp.getHeight());
                        if (iconMask != null) {
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                            Bitmap mask = drawableToBitmap(iconMask);
                            canvas.drawBitmap(mask, new Rect(0, 0, mask.getWidth(), mask.getHeight()), size, paint);
                            if (BuildConfig.DEBUG) Log.d(TAG, "Applied Mask");
                        }
                        if (iconBack != null) {
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            Bitmap back = drawableToBitmap(iconBack);
                            canvas.drawBitmap(back, new Rect(0, 0, back.getWidth(), back.getHeight()), size, paint);
                            if (BuildConfig.DEBUG) Log.d(TAG, "Applied Back");
                        }
                        if (iconUpon != null) {
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                            Bitmap upon = drawableToBitmap(iconUpon);
                            canvas.drawBitmap(upon, new Rect(0, 0, upon.getWidth(), upon.getHeight()), size, paint);
                            if (BuildConfig.DEBUG) Log.d(TAG, "Applied Upon");
                        }
                        return new BitmapDrawable(resources, dest);
                    }
                }
            } catch (Exception ignored) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Ignored: ", ignored);
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "Return normal icon");
            return getContext().getPackageManager().getActivityIcon(name);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private Drawable getDrawableFromIdentifier(Resources resources, String id, String pkg) {
        int resourceId = resources.getIdentifier(id, "drawable", pkg);
        //noinspection deprecation
        return resources.getDrawable(resourceId);
    }

    private Drawable selectRandomDrawable(Resources resources, XmlPullParser parser, String pkg) {
        int count = parser.getAttributeCount();
        int rand = random.nextInt(count);
        return getDrawableFromIdentifier(resources, parser.getAttributeValue(rand), pkg);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap result;
        if (drawable instanceof BitmapDrawable) result = ((BitmapDrawable) drawable).getBitmap();
        else {
            result = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return result;
    }
}
