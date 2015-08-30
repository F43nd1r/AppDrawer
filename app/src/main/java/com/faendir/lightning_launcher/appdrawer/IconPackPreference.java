package com.faendir.lightning_launcher.appdrawer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

/**
 * Created by Lukas on 15.08.2015.
 * Preference to select an icon pack
 */
public class IconPackPreference extends DialogPreference {

    private static final String DEFAULT_VALUE = "";

    private final Context context;
    private IconPackAdapter adapter;
    private String value;

    public IconPackPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setDialogLayoutResource(R.layout.icon_pack_dialog);
        setDialogTitle(getTitle());
    }

    @Override
    protected View onCreateDialogView() {
        PackageManager manager = context.getPackageManager();
        ListView listView = (ListView) super.onCreateDialogView();
        Intent i = new Intent("org.adw.launcher.THEMES");
        List<ResolveInfo> list = manager.queryIntentActivities(i, PackageManager.GET_META_DATA);
        adapter = new IconPackAdapter(context,list);
        listView.setAdapter(adapter);
        String sel = getPersistedString(DEFAULT_VALUE);
        if(!DEFAULT_VALUE.equals(sel)) {
            i.setPackage(sel);
            listView.setSelection(adapter.getPosition(manager.resolveActivity(i, 0)));
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                value = adapter.getItem(position).activityInfo.packageName;
                persistString(value);
                callChangeListener(value);
                getDialog().dismiss();
            }
        });
        return listView;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            value = this.getPersistedString(DEFAULT_VALUE);
        } else {
            value = (String) defaultValue;
            persistString(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
}
