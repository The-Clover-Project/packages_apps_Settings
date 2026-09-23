/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod project
 * SPDX-FileCopyrightText: The LineageOS project
 * SPDX-FileCopyrightText: 2024-2026 The Clover Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.gestures;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class TorchLongPressPowerPreferenceController extends TogglePreferenceController {

    private static final String SETTING_KEY = "torch_long_press_power_gesture";

    public TorchLongPressPowerPreferenceController(Context context, String key) {
        super(context, key);
    }

    static boolean isSupported(Context context) {
        boolean configSupported = context.getResources().getBoolean(
                com.android.internal.R.bool.config_supportLongPressPowerWhenNonInteractive);
        boolean hasFlash = context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        return configSupported && hasFlash;
    }

    @Override
    public int getAvailabilityStatus() {
        return isSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY,
                isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_system;
    }
}
