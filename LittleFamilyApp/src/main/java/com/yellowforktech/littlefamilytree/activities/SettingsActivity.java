package com.yellowforktech.littlefamilytree.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.LittleFamilyNotificationService;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements TextToSpeech.OnInitListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    protected TextToSpeech tts;
    protected ListPreference voicePref;
    protected static LittlePerson selectedPerson;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        tts = new TextToSpeech(this, this);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        setupSimplePreferencesScreen();
    }

    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(0.9f);

            /*
            if (Build.VERSION.SDK_INT > 20) {
                Set<Voice> voices = tts.getVoices();
                String[] voiceList = new String[voices.size()];
                int i = 0;
                for (Voice v : voices) {
                    voiceList[i] = v.getName();
                    i++;
                }
                voicePref.setEntries(voiceList);
                voicePref.setEntryValues(voiceList);
            }
            */

        } else {
            tts = null;
            //Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
            Log.e("LittleFamilyActivity", "Error intializing speech");
        }
    }

    public void setVoicePref(ListPreference voicePref) {
        this.voicePref = voicePref;
    }

    public TextToSpeech getTts() {
        return tts;
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
        ListPreference voicePref = (ListPreference) findPreference("tts_voice");
        if (Build.VERSION.SDK_INT > 20) {
            Set<Voice> voices = tts.getVoices();
            if (voices!=null) {
                String[] voiceList = new String[voices.size()];
                int i = 0;
                for (Voice v : voices) {
                    voiceList[i] = v.getName();
                    i++;
                }
                voicePref.setEntries(voiceList);
                voicePref.setEntryValues(voiceList);
            } else {
                getPreferenceScreen().removePreference(voicePref);
            }
        } else {
            getPreferenceScreen().removePreference(voicePref);
        }

        Preference testVoice = findPreference("tts_voice_test");
        testVoice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String message = String.format(getResources().getString(R.string.name_born_in_date), "Abraham Lincoln", "Hodgenville, Kentucky", "February 12, 1809");
                if (tts!=null) {
                    if (Build.VERSION.SDK_INT > 20) {
                        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    else {
                        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                return true;
            }
        });

        Preference manageCreds = findPreference("manage_creds");
        manageCreds.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String serviceType = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString(DataService.SERVICE_TYPE, null);
                if (serviceType.equals(DataService.SERVICE_TYPE_PHPGEDVIEW)) {
                    Intent intent = new Intent( SettingsActivity.this, PGVLoginActivity.class );
                    startActivity(intent);
                } else {
                    Intent intent = new Intent( SettingsActivity.this, FSLoginActivity.class );
                    startActivity(intent);
                }
                return true;
            }
        });

        Preference managePeople = findPreference("manage_people");
        managePeople.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent( SettingsActivity.this, PersonSearchActivity.class );
                intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
                startActivity(intent);
                return true;
            }
        });

        Preference parentsGuide = findPreference("parents_guide");
        parentsGuide.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent( SettingsActivity.this, ParentsGuideActivity.class );
                startActivity(intent);
                return true;
            }
        });

        Preference website = findPreference("website");
        website.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse("http://www.littlefamilytree.com"));
                startActivity(intent);
                return true;
            }
        });

        /*
        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);
        */
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("service_type"));
        bindPreferenceSummaryToValue(findPreference("sync_background"));
        bindPreferenceSummaryToValue(findPreference("sync_cellular"));
        bindPreferenceSummaryToValue(findPreference("sync_delay"));
        bindPreferenceSummaryToValue(findPreference("tts_voice"));
        bindPreferenceSummaryToValue(findPreference("quiet_mode"));
        bindPreferenceSummaryToValue(findPreference("enable_notifications"));
        //bindPreferenceSummaryToValue(findPreference("tts_voice_test"));
        Preference versionPref = findPreference("version");
        if (versionPref!=null) {
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                versionPref.setSummary(versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(this.getClass().getSimpleName(), "Error getting version number", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);

                if (preference.getKey().equals("enable_notifications")) {
                    Boolean onOff = (Boolean) value;
                    if (onOff) {
                        Intent myIntent = new Intent(preference.getContext(), LittleFamilyNotificationService.class);
                        preference.getContext().startService(myIntent);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(LittleFamilyNotificationService.ACTION);
                        intent.putExtra("RQS", LittleFamilyNotificationService.RQS_STOP_SERVICE);
                        preference.getContext().sendBroadcast(intent);
                    }
                }
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference==null) return;
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference.getKey().equals("sync_background") || preference.getKey().equals("sync_cellular")
                || preference.getKey().equals("quiet_mode") || preference.getKey().equals("enable_notifications")) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), true));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            ListPreference pref = (ListPreference) findPreference("tts_voice");
            ((SettingsActivity)getActivity()).setVoicePref(pref);
            //if (Build.VERSION.SDK_INT > 20) {
            //} else {
                getPreferenceScreen().removePreference(pref);
            //}

            Preference testVoice = findPreference("tts_voice_test");
            testVoice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String message = String.format(getResources().getString(R.string.name_born_in_date), "Abraham Lincoln", "Hodgenville, Kentucky", "February 12, 1809");
                    if (((SettingsActivity) getActivity()).getTts() != null) {
                        if (Build.VERSION.SDK_INT > 20) {
                            ((SettingsActivity) getActivity()).getTts().speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                        } else {
                            ((SettingsActivity) getActivity()).getTts().speak(message, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                    return true;
                }
            });

            Preference manageCreds = findPreference("manage_creds");
            manageCreds.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String serviceType = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(DataService.SERVICE_TYPE, null);
                    if (serviceType.equals(DataService.SERVICE_TYPE_PHPGEDVIEW)) {
                        Intent intent = new Intent( getActivity(), PGVLoginActivity.class );
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent( getActivity(), FSLoginActivity.class );
                        startActivity(intent);
                    }
                    return true;
                }
            });

            Preference managePeople = findPreference("manage_people");
            managePeople.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent( getActivity(), PersonSearchActivity.class );
                    intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
                    startActivity(intent);
                    return true;
                }
            });

            Preference parentsGuide = findPreference("parents_guide");
            parentsGuide.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent( getActivity(), ParentsGuideActivity.class );
                    startActivity(intent);
                    return true;
                }
            });

            Preference website = findPreference("website");
            website.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse("http://www.littlefamilytree.com"));
                    startActivity(intent);
                    return true;
                }
            });

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("service_type"));
            bindPreferenceSummaryToValue(findPreference("sync_background"));
            bindPreferenceSummaryToValue(findPreference("sync_cellular"));
            bindPreferenceSummaryToValue(findPreference("sync_delay"));
            bindPreferenceSummaryToValue(findPreference("tts_voice"));
            bindPreferenceSummaryToValue(findPreference("quiet_mode"));
            bindPreferenceSummaryToValue(findPreference("enable_notifications"));

            Preference versionPref = findPreference("version");
            if (versionPref!=null) {
                try {
                    String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
                    versionPref.setSummary(versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(this.getClass().getSimpleName(), "Error getting version number", e);
                }
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (GeneralPreferenceFragment.class.getName().equals(fragmentName)) return true;
        return super.isValidFragment(fragmentName);
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }
    */

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }
    */
}
