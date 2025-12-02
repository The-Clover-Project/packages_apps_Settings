/*
 * Copyright (C) 2024-2025 The Clover Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.deviceinfo.firmwareversion

import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.preference.PreferenceBinding

class CloverLogoPreference : PreferenceMetadata, PreferenceBinding {

    override val key: String
        get() = "clover_logo_header"

    override val title: Int
        get() = R.string.clover_firmware_version

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.layoutResource = R.layout.clover_logo
        preference.isSelectable = false
    }
}