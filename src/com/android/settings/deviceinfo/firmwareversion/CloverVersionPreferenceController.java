/*
 * Copyright (C) 2026 The Clover Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class CloverVersionPreferenceController extends BasePreferenceController {

    static final String VERSION_PROPERTY = "ro.clover.display.version";
    static final String CLOVER_BUILDTYPE_PROPERTY = "ro.clover.releasetype";
    static final String DEVICE_PROPERTY = "ro.product.device";

    public CloverVersionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return hasVersion() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        final String version = SystemProperties.get(VERSION_PROPERTY, "");
        final String buildType = SystemProperties.get(CLOVER_BUILDTYPE_PROPERTY, "");
        final String device = SystemProperties.get(DEVICE_PROPERTY, "Unknown");

        if (!TextUtils.isEmpty(version) && !TextUtils.isEmpty(buildType)) {
            return version + " | " + device + " | " + buildType;
        }
        return mContext.getString(R.string.device_info_default);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setIntent(new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(mContext.getString(R.string.clover_website_uri))));
    }

    private boolean hasVersion() {
        final String version = SystemProperties.get(VERSION_PROPERTY, "");
        final String buildType = SystemProperties.get(CLOVER_BUILDTYPE_PROPERTY, "");
        return !TextUtils.isEmpty(version) && !TextUtils.isEmpty(buildType);
    }
}
