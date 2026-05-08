/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.deviceinfo.aboutphone;

import static androidx.core.content.ContextCompat.getMainExecutor;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.deviceinfo.BluetoothAddressPreferenceController;
import com.android.settings.deviceinfo.BuildNumberPreferenceController;
import com.android.settings.deviceinfo.DeviceNamePreferenceController;
import com.android.settings.deviceinfo.FccEquipmentIdPreferenceController;
import com.android.settings.deviceinfo.FeedbackPreferenceController;
import com.android.settings.deviceinfo.IpAddressPreferenceController;
import com.android.settings.deviceinfo.ManualPreferenceController;
import com.android.settings.deviceinfo.RegulatoryInfoPreferenceController;
import com.android.settings.deviceinfo.SafetyInfoPreferenceController;
import com.android.settings.deviceinfo.UptimePreferenceController;
import com.android.settings.deviceinfo.WifiMacAddressPreferenceController;
import com.android.settings.deviceinfo.imei.ImeiInfoPreferenceController;
import com.android.settings.deviceinfo.simstatus.EidStatus;
import com.android.settings.deviceinfo.simstatus.SimEidPreferenceController;
import com.android.settings.deviceinfo.simstatus.SimStatusPreferenceController;
import com.android.settings.deviceinfo.simstatus.SlotSimStatus;
import com.android.settings.flags.Flags;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.fuelgauge.BatteryUtils;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SearchIndexable
public class MyDeviceInfoFragment extends DashboardFragment
        implements DeviceNamePreferenceController.DeviceNamePreferenceHost {

    private static final String LOG_TAG = "MyDeviceInfoFragment";
    private static final String KEY_ABOUT_PHONE_BRANDING = "about_phone_branding";
    private static final String KEY_ABOUT_PHONE_META_PILLS = "about_phone_meta_pills";
    private static final String KEY_ABOUT_PHONE_INFO_CARDS = "about_phone_info_cards";
    static final String KEY_CLOVER_MAINTAINER = "clover_maintainer";
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_EID_INFO = "eid_info";
    private static final String PROP_CLOVER_BUILD_VERSION = "ro.clover.build.version";
    private static final String PROP_CLOVER_DISPLAY_VERSION = "ro.clover.display.version";
    private static final String PROP_CLOVER_MAINTAINER = "ro.clover.maintainer";
    private static final String PROP_CLOVER_RELEASE_TYPE = "ro.clover.releasetype";

    private BuildNumberPreferenceController mBuildNumberPreferenceController;

    private DeviceInfoViewModel mDeviceInfoViewModel;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DEVICEINFO;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(DeviceNamePreferenceController.class).setHost(this /* parent */);
        mBuildNumberPreferenceController = use(BuildNumberPreferenceController.class);
        mBuildNumberPreferenceController.setHost(this /* parent */);
    }

    @Override
    public void onCreate(@Nullable Bundle icicle) {
        super.onCreate(icicle);
        mDeviceInfoViewModel = new ViewModelProvider(requireActivity()).get(DeviceInfoViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindVersionPills();
        bindInfoCards();
        bindMaintainerPreference();
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.my_device_info;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildMainPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildMainPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new DeviceNamePreferenceController(context, KEY_DEVICE_NAME));
        return controllers;
    }

    static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Fragment fragment, Lifecycle lifecycle) {
        // disable catalyst for settings search (i.e. fragment is null)
        boolean isCatalystEnabled = Flags.catalystMyDeviceInfoPrefScreen() && fragment != null;
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        final Executor executor = (fragment == null) ? getMainExecutor(context) :
                Executors.newSingleThreadExecutor();
        androidx.lifecycle.Lifecycle lifecycleObject = (fragment == null) ? null :
                fragment.getLifecycle();
        final SlotSimStatus slotSimStatus = new SlotSimStatus(context, executor, lifecycleObject);

        controllers.add(new IpAddressPreferenceController(context, lifecycle));
        controllers.add(new WifiMacAddressPreferenceController(context, lifecycle));
        controllers.add(new BluetoothAddressPreferenceController(context, lifecycle));
        controllers.add(new RegulatoryInfoPreferenceController(context));
        controllers.add(new SafetyInfoPreferenceController(context));
        controllers.add(new ManualPreferenceController(context));
        controllers.add(new FeedbackPreferenceController(fragment, context));
        controllers.add(new FccEquipmentIdPreferenceController(context));
        controllers.add(new UptimePreferenceController(context, lifecycle));

        Consumer<String> imeiInfoList = imeiKey -> {
            if (Flags.catalystMyDeviceInfoPrefScreen()) {
                return;
            }
            ImeiInfoPreferenceController imeiRecord =
                    new ImeiInfoPreferenceController(context, imeiKey);
            imeiRecord.init(fragment, slotSimStatus);
            controllers.add(imeiRecord);
        };

        if (fragment != null) {
            imeiInfoList.accept(ImeiInfoPreferenceController.DEFAULT_KEY);
        }

        for (int slotIndex = 0; slotIndex < slotSimStatus.size(); slotIndex++) {
            SimStatusPreferenceController slotRecord =
                    new SimStatusPreferenceController(context,
                            slotSimStatus.getPreferenceKey(slotIndex));
            slotRecord.init(fragment, slotSimStatus);
            controllers.add(slotRecord);

            if (fragment != null) {
                imeiInfoList.accept(ImeiInfoPreferenceController.DEFAULT_KEY + (1 + slotIndex));
            }
        }

        if (!isCatalystEnabled) {
            EidStatus eidStatus = new EidStatus(slotSimStatus, context, executor);
            SimEidPreferenceController simEid = new SimEidPreferenceController(context,
                    KEY_EID_INFO);
            simEid.init(slotSimStatus, eidStatus);
            controllers.add(simEid);
        }

        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBuildNumberPreferenceController.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void bindVersionPills() {
        final LayoutPreference pillsPreference = findPreference(KEY_ABOUT_PHONE_META_PILLS);
        if (pillsPreference == null) {
            return;
        }

        final TextView cloverVersionPill =
                pillsPreference.findViewById(R.id.about_phone_clover_version_pill);
        final ImageView cloverVersionIcon =
                pillsPreference.findViewById(R.id.about_phone_clover_version_icon);
        final TextView androidVersionPill =
                pillsPreference.findViewById(R.id.about_phone_android_version_pill);

        if (cloverVersionPill != null) {
            cloverVersionPill.setText(buildCloverVersionPillText());
        }
        if (cloverVersionIcon != null) {
            cloverVersionIcon.setImageResource(isUnofficialBuild()
                    ? R.drawable.verified_off_24px : R.drawable.verified_24px);
        }
        if (androidVersionPill != null) {
            androidVersionPill.setText(buildAndroidVersionPillText());
        }
    }

    private void bindInfoCards() {
        final LayoutPreference infoCardsPreference = findPreference(KEY_ABOUT_PHONE_INFO_CARDS);
        if (infoCardsPreference == null) {
            return;
        }

        bindInfoCardValue(infoCardsPreference, R.id.about_phone_device_name_value,
                buildDeviceNameCardText());
        bindInfoCardValue(infoCardsPreference, R.id.about_phone_memory_value,
                buildMemoryCardText());
        bindInfoCardValue(infoCardsPreference, R.id.about_phone_battery_value,
                buildBatteryCardText());
        bindInfoCardValue(infoCardsPreference, R.id.about_phone_resolution_value,
                buildResolutionCardText());

        final View deviceNameCard = infoCardsPreference.findViewById(
                R.id.about_phone_device_name_card);
        if (deviceNameCard != null) {
            deviceNameCard.setOnClickListener(v -> DeviceNameEditDialog.show(this));
        }

        bindInfoCardClick(infoCardsPreference, R.id.about_phone_memory_card,
                Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        bindInfoCardClick(infoCardsPreference, R.id.about_phone_battery_card,
                Intent.ACTION_POWER_USAGE_SUMMARY);
        bindInfoCardClick(infoCardsPreference, R.id.about_phone_resolution_card,
                Settings.ACTION_DISPLAY_SETTINGS);
    }

    private void bindInfoCardValue(@NonNull LayoutPreference preference, int viewId,
            @NonNull CharSequence value) {
        final TextView valueView = preference.findViewById(viewId);
        if (valueView != null) {
            valueView.setText(value);
        }
    }

    private void bindInfoCardClick(@NonNull LayoutPreference preference, int viewId,
            @NonNull String action) {
        final View card = preference.findViewById(viewId);
        if (card != null) {
            card.setOnClickListener(v -> launchSettingsAction(action));
        }
    }

    private void launchSettingsAction(@NonNull String action) {
        startActivity(new Intent(action).setPackage(requireContext().getPackageName()));
    }

    @NonNull
    private CharSequence buildCloverVersionPillText() {
        final String releaseType = getTitleCaseSystemProperty(PROP_CLOVER_RELEASE_TYPE, "Official");
        final String versionDisplay = getSystemProperty(PROP_CLOVER_DISPLAY_VERSION, null);
        final String versionBuild = getSystemProperty(PROP_CLOVER_BUILD_VERSION, getString(R.string.device_info_default));
        final String version = versionDisplay != null ? versionDisplay : versionBuild;

        return getString(R.string.about_phone_clover_version_pill_format, releaseType,
                version.replaceFirst("^[vV]", ""));
    }

    private boolean isUnofficialBuild() {
        return "UNOFFICIAL".equalsIgnoreCase(getSystemProperty(PROP_CLOVER_RELEASE_TYPE, ""));
    }

    @NonNull
    private CharSequence buildAndroidVersionPillText() {
        return getString(R.string.about_phone_android_version_pill_format,
                Build.VERSION.RELEASE_OR_CODENAME);
    }

    @NonNull
    static CharSequence buildMaintainerCardText(@NonNull Context context) {
        return getSystemProperty(PROP_CLOVER_MAINTAINER,
                context.getString(R.string.device_info_not_available));
    }

    @NonNull
    private CharSequence buildDeviceNameCardText() {
        final String deviceName = Settings.Global.getString(
                requireContext().getContentResolver(), Settings.Global.DEVICE_NAME);
        return deviceName != null ? deviceName : Build.MODEL;
    }

    private void bindMaintainerPreference() {
        bindMaintainerPreference(this);
    }

    static void bindMaintainerPreference(@NonNull DashboardFragment fragment) {
        final Preference maintainerPreference = fragment.findPreference(KEY_CLOVER_MAINTAINER);
        if (maintainerPreference != null) {
            maintainerPreference.setSummary(buildMaintainerCardText(fragment.requireContext()));
        }
    }

    @NonNull
    private CharSequence buildMemoryCardText() {
        final String ramText = formatStorageSize(getAdvertisedRam());

        final StorageManager storageManager = requireContext().getSystemService(StorageManager.class);
        final String romText = storageManager == null
                ? getString(R.string.device_info_not_available)
                : formatStorageSize(storageManager.getPrimaryStorageSize());

        if (isNotAvailable(ramText) || isNotAvailable(romText)) {
            return getString(R.string.device_info_not_available);
        }

        return getString(R.string.about_phone_memory_card_value_format, ramText, romText);
    }

    @NonNull
    private CharSequence buildBatteryCardText() {
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(requireContext());
        final int designCapacityUah = batteryIntent.getIntExtra(
                BatteryManager.EXTRA_DESIGN_CAPACITY, -1);
        if (designCapacityUah <= 0) {
            return getString(R.string.battery_design_capacity_not_available);
        }
        return getString(R.string.battery_design_capacity_summary, designCapacityUah / 1_000);
    }

    @NonNull
    private CharSequence buildResolutionCardText() {
        final DisplayManager displayManager = requireContext().getSystemService(DisplayManager.class);
        final Display display = displayManager == null
                ? null : displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        if (display == null) {
            return getString(R.string.device_info_not_available);
        }

        final DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        return getString(R.string.about_phone_resolution_card_value_format,
                Math.min(metrics.widthPixels, metrics.heightPixels),
                Math.max(metrics.widthPixels, metrics.heightPixels));
    }

    @NonNull
    private String getTitleCaseSystemProperty(@NonNull String key, @NonNull String fallback) {
        final String resolved = getSystemProperty(key, fallback).trim().toLowerCase(Locale.ROOT);
        if (resolved.isEmpty()) {
            return fallback;
        }
        return Character.toUpperCase(resolved.charAt(0)) + resolved.substring(1);
    }

    @NonNull
    private static String getSystemProperty(@NonNull String key, @Nullable String fallback) {
        final String value = SystemProperties.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    @NonNull
    private String formatStorageSize(long bytes) {
        if (bytes <= 0) {
            return getString(R.string.device_info_not_available);
        }

        final long terabyte = 1_000_000_000_000L;
        final long gigabyte = 1_000_000_000L;

        if (bytes >= terabyte) {
            if (bytes % terabyte == 0) {
                return (bytes / terabyte) + " TB";
            } else {
                return String.format(Locale.ROOT, "%.1f TB", (double) bytes / terabyte);
            }
        }

        if (bytes % gigabyte == 0) {
            return (bytes / gigabyte) + " GB";
        } else {
            double gb = (double) bytes / gigabyte;
            if (Math.abs(gb - Math.round(gb)) < 0.05) {
                return Math.round(gb) + " GB";
            }
            return String.format(Locale.ROOT, "%.1f GB", gb);
        }
    }

    private boolean isNotAvailable(@NonNull String value) {
        return getString(R.string.device_info_not_available).contentEquals(value);
    }

    @Override
    public void showDeviceNameWarningDialog(String deviceName) {
        mDeviceInfoViewModel.setDeviceName(deviceName);
        DeviceNameWarningDialog.show(this);
    }

    private long getAdvertisedRam() {
        final long totalMemory = Process.getTotalMemory();
        final long[] tiers = {1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 64};
        for (long tier : tiers) {
            if (totalMemory <= (tier + 0.5) * 1e9) {
                return tier * 1_000_000_000L;
            }
        }
        return Process.getAdvertisedMem();
    }

    CharSequence getCurrentDeviceName() {
        return buildDeviceNameCardText();
    }

    boolean isDeviceNameValid(@NonNull String deviceName) {
        return !deviceName.isBlank()
                && use(DeviceNamePreferenceController.class).isTextValid(deviceName);
    }

    private boolean isCatalystDeviceNameEnabled() {
        return isCatalystEnabled() && Flags.catalystAboutPhoneDeviceName();
    }

    void onDeviceNameEditSubmitted(@NonNull String deviceName) {
        if (!isCatalystDeviceNameEnabled()) {
            use(DeviceNamePreferenceController.class).setPendingDeviceName(deviceName);
        }
        showDeviceNameWarningDialog(deviceName);
    }

    public void onSetDeviceNameConfirm(boolean confirm) {
        if (!isCatalystDeviceNameEnabled()) {
            final DeviceNamePreferenceController controller = use(
                    DeviceNamePreferenceController.class);
            controller.updateDeviceName(confirm);
        } else {
            if (confirm) {
                final String deviceName = mDeviceInfoViewModel.getDeviceName();
                if (deviceName != null) {
                    UtilsKt.updateDeviceName(requireActivity(), deviceName);
                }
            }
        }
        mDeviceInfoViewModel.clearDeviceNme();
        refreshDeviceNameCard();
    }

    private void refreshDeviceNameCard() {
        final LayoutPreference infoCardsPreference = findPreference(KEY_ABOUT_PHONE_INFO_CARDS);
        if (infoCardsPreference == null) {
            return;
        }
        bindInfoCardValue(infoCardsPreference, R.id.about_phone_device_name_value,
                buildDeviceNameCardText());
    }

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        // The redesigned main page is intentionally curated in XML. The existing Catalyst
        // metadata hierarchy still describes the legacy full About phone page and includes
        // moved rows such as IMEI, so hybrid binding would initialize missing preferences.
        return null;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.my_device_info) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildMainPreferenceControllers(context, null /* lifecycle */);
                }
            };
}
