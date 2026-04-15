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
import android.os.SystemProperties;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class CloverMaintainerPreferenceController extends BasePreferenceController {

    static final String MAINTAINER_PROPERTY = "ro.clover.maintainer";

    public CloverMaintainerPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return hasMaintainer() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        final String maintainer = SystemProperties.get(MAINTAINER_PROPERTY, "");
        return TextUtils.isEmpty(maintainer)
                ? mContext.getString(R.string.device_info_default)
                : maintainer;
    }

    private boolean hasMaintainer() {
        return !TextUtils.isEmpty(SystemProperties.get(MAINTAINER_PROPERTY, ""));
    }
}
