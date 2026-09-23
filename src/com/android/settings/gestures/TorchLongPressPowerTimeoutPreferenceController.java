/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod project
 * SPDX-FileCopyrightText: The LineageOS project
 * SPDX-FileCopyrightText: 2024-2026 The Clover Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.gestures;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class TorchLongPressPowerTimeoutPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String SETTING_KEY = "torch_long_press_power_timeout";

    public TorchLongPressPowerTimeoutPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return TorchLongPressPowerPreferenceController.isSupported(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        String value = String.valueOf(
                Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY, 0));
        listPreference.setValue(value);
        updateSummary(listPreference, value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY,
                    Integer.parseInt((String) newValue));
        } catch (NumberFormatException e) {
            return false;
        }
        updateSummary((ListPreference) preference, (String) newValue);
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_system;
    }

    private static void updateSummary(ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        if (index >= 0) {
            preference.setSummary(preference.getEntries()[index]);
        }
    }
}
