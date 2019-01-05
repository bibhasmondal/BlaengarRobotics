package in.blrobotics.blaengarrobotics;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;

public class MyListPreference extends ListPreference {

    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // Setting up theme
        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("theme_switch", false)) {
            builder.getContext().setTheme(R.style.DarkDialogTheme);
        }
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    public void setEntries(CharSequence[] sequence) {
        super.setEntries(sequence);
    }

    @Override
    public void setEntryValues(CharSequence[] sequence) {
        super.setEntryValues(sequence);
    }
}
