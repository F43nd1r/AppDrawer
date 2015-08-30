package com.faendir.lightning_launcher.appdrawer;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Lukas on 14.08.2015.
 * preference to select a number
 */
public class NumberPreference extends DialogPreference {

    private static final int DEFAULT_VALUE = 0;
    private EditText editText;
    private int value;

    public NumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_dialog);
        setDialogTitle(getTitle());
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        editText = (EditText) view.findViewById(R.id.editText);
        editText.setText(String.valueOf(value));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            value = Integer.valueOf(editText.getText().toString());
            persistInt(value);
            callChangeListener(value);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            value = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            value = (Integer) defaultValue;
            persistInt(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index,DEFAULT_VALUE);
    }
}
