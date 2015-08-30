package com.faendir.lightning_launcher.appdrawer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Lukas on 15.08.2015.
 * HOlds a list of icon packs
 */
class IconPackAdapter extends ArrayAdapter<ResolveInfo> {

    private static final int RESOURCE = R.layout.icon_pack_item;

    private final Context context;

    public IconPackAdapter(Context context, List<ResolveInfo> items) {
        super(context, RESOURCE, items);
        this.context = context;
    }

    @Override
     public final View getView(int position, View convertView,
                               ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(RESOURCE, parent, false);
        }

        bindView(position, convertView);

        return convertView;
    }

    private void bindView(int position, View row) {
        PackageManager pm = context.getPackageManager();
        TextView label = (TextView) row.findViewById(R.id.label);

        label.setText(getItem(position).loadLabel(pm));

        ImageView icon = (ImageView) row.findViewById(R.id.icon);

        icon.setImageDrawable(getItem(position).loadIcon(pm));
    }
}
