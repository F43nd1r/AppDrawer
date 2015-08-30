package com.faendir.lightning_launcher.appdrawer;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Map;

/**
 * Created by Lukas on 14.08.2015.
 * Main Settings activity
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        final Preference rows = findPreference(getString(R.string.pref_rows));
        bindPreferenceSummaryToValue(rows);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_columns)));
        final Preference pack = findPreference(getString(R.string.pref_iconPack));
        bindPreferenceSummaryToValue(pack);
        final CheckBoxPreference usePack = (CheckBoxPreference) findPreference(getString(R.string.pref_usePack));
        pack.setEnabled(usePack.isChecked());
        usePack.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                pack.setEnabled((Boolean) newValue);
                return true;
            }
        });
        final CheckBoxPreference groupItems = (CheckBoxPreference) findPreference(getString(R.string.pref_groupItems));
        rows.setEnabled(!groupItems.isChecked());
        if(groupItems.isChecked()) {
            getPreferenceScreen().getSharedPreferences().edit().putInt(getString(R.string.pref_rows),0);
        }
        groupItems.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                rows.setEnabled(!(Boolean)newValue);
                if((Boolean)newValue) {
                    getPreferenceScreen().getSharedPreferences().edit().putInt(getString(R.string.pref_rows),0);
                }
                return true;
            }
        });
    }

    private static final Preference.OnPreferenceChangeListener BIND_CHANGE_LISTENER = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(BIND_CHANGE_LISTENER);
        Map<String,?> map = PreferenceManager
        .getDefaultSharedPreferences(preference.getContext()).getAll();
        Object value = map.get(preference.getKey());
        if(value == null) {
            value = "";
        }
        BIND_CHANGE_LISTENER.onPreferenceChange(preference, value);
    }
}
