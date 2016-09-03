/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.ViewGroup;

import com.android.internal.util.cm.ScreenType;

public class TouchscreenGestureSettings extends PreferenceActivity {
	
    public static final String CATEGORY_GESTURES = "category_gestures";
    public static PreferenceCategory gestureCat;

    private static final String KEY_AMBIENT_DISPLAY_ENABLE = "ambient_display_enable";
    private static final String KEY_GESTURE_HAND_WAVE = "gesture_hand_wave";
    private static final String KEY_GESTURE_PICK_UP = "gesture_pick_up";
    private static final String KEY_GESTURE_POCKET = "gesture_pocket";
    private static final String KEY_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback";
    private static final String KEY_PROXIMITY_WAKE = "proximity_wake_enable";

    private SwitchPreference mAmbientDisplayPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mHapticFeedback;
    private SwitchPreference mPickupPreference;
    private SwitchPreference mPocketPreference;
    private SwitchPreference mProximityWakePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.touchscreen_panel);
        boolean dozeEnabled = isDozeEnabled();
        mAmbientDisplayPreference = (SwitchPreference) findPreference(KEY_AMBIENT_DISPLAY_ENABLE);
        mAmbientDisplayPreference.setChecked(dozeEnabled);
        mAmbientDisplayPreference.setOnPreferenceChangeListener(mAmbientDisplayPrefListener);
        mHandwavePreference = (SwitchPreference) findPreference(KEY_GESTURE_HAND_WAVE);
        mHandwavePreference.setEnabled(dozeEnabled);
        mHandwavePreference.setOnPreferenceChangeListener(mGesturePrefListener);
        mPickupPreference = (SwitchPreference) findPreference(KEY_GESTURE_PICK_UP);
        mPickupPreference.setEnabled(dozeEnabled);
        mPocketPreference = (SwitchPreference) findPreference(KEY_GESTURE_POCKET);
        mPocketPreference.setEnabled(dozeEnabled);
        mProximityWakePreference = (SwitchPreference) findPreference(KEY_PROXIMITY_WAKE);
        mProximityWakePreference.setOnPreferenceChangeListener(mGesturePrefListener);
        mHapticFeedback = (SwitchPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        mHapticFeedback.setOnPreferenceChangeListener(mHapticPrefListener);

         gestureCat = (PreferenceCategory) findPreference(CATEGORY_GESTURES);
        if (gestureCat != null) {
            gestureCat.setEnabled(CMActionsSettings.areGesturesEnabled());
        }

        ((ViewGroup)getListView().getParent()).setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHapticFeedback.setChecked(CMSettings.System.getInt(getContentResolver(),
                CMSettings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, 1) != 0);
    }

    private boolean isDozeEnabled() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.DOZE_ENABLED, 1) != 0;
    }

    private Preference.OnPreferenceChangeListener mAmbientDisplayPrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean enable = (boolean) newValue;
            boolean ret = enableDoze(enable);
            if (ret) {
                mHandwavePreference.setEnabled(enable);
                mPickupPreference.setEnabled(enable);
                mPocketPreference.setEnabled(enable);
            }
            return ret;
        }
    };

    private Preference.OnPreferenceChangeListener mGesturePrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ((boolean) newValue) {
                final String key = preference.getKey();
                if (KEY_GESTURE_HAND_WAVE.equals(key)) {
                    mProximityWakePreference.setChecked(false);
                } else if (KEY_PROXIMITY_WAKE.equals(key)) {
                    mHandwavePreference.setChecked(false);
                }
            }
            return true;
        }
    };

    private Preference.OnPreferenceChangeListener mHapticPrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            if (KEY_HAPTIC_FEEDBACK.equals(key)) {
                final boolean value = (Boolean) newValue;
                Settings.System.putInt(getContentResolver(), KEY_HAPTIC_FEEDBACK, value ? 1 : 0);
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // If running on a phone, remove padding around the listview
        if (!ScreenType.isTablet(this)) {
            getListView().setPadding(0, 0, 0, 0);
        }

        mHapticFeedback.setChecked(
                Settings.System.getInt(getContentResolver(), KEY_HAPTIC_FEEDBACK, 1) != 0);
    }
}
