package com.android.settings.cyanogenmod.superuser;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.settings.Utils;
import com.koushikdutta.superuser.LogFragment;
import com.koushikdutta.superuser.PolicyFragment;
import com.koushikdutta.superuser.SettingsFragment;

public class PolicyNativeFragment extends com.koushikdutta.superuser.PolicyFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Utils.forcePrepareCustomPreferencesList(container, view, getListView(), false);
        return view;
    }

    public static class EmbeddedSettingsFragment extends SettingsFragment {
        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            adjustListPadding(getListView());
            return view;
        }
    }

    public static class EmbeddedLogFragment extends LogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            adjustListPadding(getListView());
            return view;
        }
    }

    @Override
    protected LogFragment createLogFragment() {
        return new EmbeddedLogFragment();
    }

    @Override
    protected SettingsFragment createSettingsFragment() {
        return new EmbeddedSettingsFragment();
    };

    private static void adjustListPadding(ListView list) {
        final Resources res = list.getResources();
        final int paddingSide = res.getDimensionPixelSize(
                com.android.internal.R.dimen.preference_fragment_padding_side);
        final int paddingBottom = res.getDimensionPixelSize(
                com.android.internal.R.dimen.preference_fragment_padding_bottom);

        list.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        list.setPadding(paddingSide, 0, paddingSide, paddingBottom);
    }
}
