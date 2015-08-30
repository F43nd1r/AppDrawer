package com.faendir.lightning_launcher.appdrawer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class setup extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = new Intent();
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_ID, R.raw.setup);
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_NAME, getString(R.string.script_name));
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_FLAGS, 0);
        data.putExtra(Constants.INTENT_EXTRA_EXECUTE_ON_LOAD, true);
        data.putExtra(Constants.INTENT_EXTRA_DELETE_AFTER_EXECUTION, true);
        setResult(RESULT_OK, data);
        finish();
    }
}
