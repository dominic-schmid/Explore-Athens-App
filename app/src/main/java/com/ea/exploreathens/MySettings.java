package com.ea.exploreathens;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SeekBarPreference;
import android.util.Log;
import android.widget.Toast;

import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.fragments.MapsFragment;

public class MySettings extends PreferenceFragmentCompat {

    SharedPreferences sharedPreferences;

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else if(preference instanceof SwitchPreference){ // Otherwise it has to be the show radar togglebutton
                SwitchPreference radarSW = (SwitchPreference) preference;

                SeekBarPreference seekBarRadar = (SeekBarPreference) findPreference("seek_bar_radar"); //Preference Key
                seekBarRadar.setVisible(!radarSW.isChecked());
            } else if(preference instanceof  EditTextPreference){
                EditTextPreference url = (EditTextPreference) preference;
                String newURL = "http://" + url.getText();
                if(!CodeUtility.baseURL.equals(newURL))
                    Toast.makeText(getContext(), "API URL updated to " + newURL, Toast.LENGTH_LONG).show();
                CodeUtility.baseURL = newURL;
                Log.d("preference", "Updated base url to " + newURL);
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setHasOptionsMenu(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Set defaults
        SwitchPreference radarSW = (SwitchPreference) getPreferenceManager().findPreference("radar_switch");
        radarSW.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        SeekBarPreference seekBarRadar = (SeekBarPreference) findPreference("seek_bar_radar"); //Preference Key
        seekBarRadar.setVisible(radarSW.isChecked());

        EditTextPreference reqURL = (EditTextPreference) getPreferenceManager().findPreference("default_get_url");
        reqURL.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String newURL = "http://" + o.toString();
                Log.d("preference", "Base url to " + newURL);
                if(!CodeUtility.baseURL.equals(newURL)) {
                    Toast.makeText(getContext(), "API URL updated to " + newURL, Toast.LENGTH_LONG).show();
                    CodeUtility.baseURL = newURL;
                }
                return true;
            }
        });
        CodeUtility.baseURL = "http://" + reqURL.getText();


        Preference button = getPreferenceManager().findPreference("send_location");
        if (button != null) {
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    PostRequest request = new PostRequest(getContext());
                                    //request.execute("1fe9979d2c2421be", ""+position.latitude, ""+position.longitude);
                                    request.execute(CodeUtility.getAndroidId(getContext()), "" + MapsFragment.currentLat, ""+MapsFragment.currentLng);

                                    //Yes button clicked
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder locationBuilder = new AlertDialog.Builder(getContext());
                    locationBuilder.setMessage(getResources().getString(R.string.send_location_confirmation)).setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
                    //CodeUtility.showSendLocation(getContext());
                    return true;
                }
            });

            Preference locationBtn = getPreferenceManager().findPreference("show_android_id");
            if(locationBtn != null){
                locationBtn.setOnPreferenceClickListener(pref->{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Android ID: " + CodeUtility.getAndroidId(getContext()))
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                });
            }
        }
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(findPreference("language"));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //seekBarRadar = (SeekBarPreference) findPreference("seek_bar_radar"); //Preference Key
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "Deutsch"));
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    /*private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }*/


   /* @SuppressWarnings("deprecation") // TODO this is depracated we should really fix
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LanguagePreferenceFragment extends PreferenceFragment {

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), MySettings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }*/

}
