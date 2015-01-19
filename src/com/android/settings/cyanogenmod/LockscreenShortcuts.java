package com.android.settings.cyanogenmod;

import android.annotation.Nullable;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.internal.util.cm.LockscreenShortcutsHelper;
import com.android.internal.util.cm.NavigationRingConstants;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

import static com.android.internal.util.cm.NavigationRingConstants.ACTION_APP;
import static com.android.internal.util.cm.NavigationRingConstants.ACTION_NONE;

public class LockscreenShortcuts extends Fragment implements View.OnClickListener, ShortcutPickHelper.OnPickListener {

    private static final int[] sIconIds = new int[]{R.id.left_button, R.id.right_button};

    private ActionHolder mActions;
    private ShortcutPickHelper mPicker;
    private LockscreenShortcutsHelper mShortcutHelper;
    private View mSelectedView;
    private ColorStateList mDefaultTintList;

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        onTargetChange(uri);
    }

    private class ActionHolder {
        private ArrayList<CharSequence> mAvailableEntries = new ArrayList<CharSequence>();
        private ArrayList<String> mAvailableValues = new ArrayList<String>();

        public void addAction(String action, int entryResId) {
            mAvailableEntries.add(getString(entryResId));
            mAvailableValues.add(action);
        }

        public int getActionIndex(String action) {
            int count = mAvailableValues.size();
            for (int i = 0; i < count; i++) {
                if (TextUtils.equals(mAvailableValues.get(i), action)) {
                    return i;
                }
            }

            return -1;
        }

        public String getAction(int index) {
            if (index > mAvailableValues.size()) {
                return null;
            }

            return mAvailableValues.get(index);
        }

        public CharSequence[] getEntries() {
            return mAvailableEntries.toArray(new CharSequence[mAvailableEntries.size()]);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lockscreen_shortcuts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPicker = new ShortcutPickHelper(getActivity(), this);
        mShortcutHelper = new LockscreenShortcutsHelper(getActivity(), null);
        createActionList();
        initiateViews(view);
        setUnlockIcon(view);
        updateDrawables();
    }

    private void setUnlockIcon(View view) {
        ImageView unlock = (ImageView) view.findViewById(R.id.middle_button);
        mDefaultTintList = unlock.getImageTintList();
        Drawable d = mShortcutHelper.getDrawableFromSystemUI("ic_lock_24dp");
        if (d != null) {
            unlock.setImageDrawable(d);
        }
    }

    private void initiateViews(View view) {
        for (int id : sIconIds) {
            view.findViewById(id).setOnClickListener(this);
        }
    }

    private void updateDrawables() {
        List<LockscreenShortcutsHelper.TargetInfo> items = mShortcutHelper.getDrawablesForTargets();
        for (int i = 0; i < sIconIds.length; i++) {
            LockscreenShortcutsHelper.TargetInfo item = items.get(i);
            int id = sIconIds[i];
            ImageView v = (ImageView) getView().findViewById(id);
            v.setColorFilter(item.colorFilter);
            v.setImageDrawable(item.icon);
            v.setTag(item.uri);
            if (NavigationRingConstants.ACTION_NONE.equals(item.uri)) {
                v.setImageTintList(mDefaultTintList);
            } else {
                v.setImageTintList(null);
            }
        }
    }

    private void createActionList() {
        mActions = new ActionHolder();
        mActions.addAction(ACTION_NONE, R.string.lockscreen_default_target);

        mActions.addAction(ACTION_APP, R.string.select_application);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        mSelectedView = v;

        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onTargetChange(mActions.getAction(item));
                dialog.dismiss();
            }
        };

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.navring_choose_action_title)
                .setItems(mActions.getEntries(), l)
                .create();

        dialog.show();
    }

    private void onTargetChange(String uri) {
        if (uri.equals(ACTION_APP)) {
            final String label = getResources().getString(R.string.lockscreen_target_empty);
            final Intent.ShortcutIconResource iconResource =
                    Intent.ShortcutIconResource.fromContext(getActivity(), android.R.drawable.ic_delete);
            mPicker.pickShortcut(
                    new String[] { label },
                    new Intent.ShortcutIconResource[] { iconResource },
                    getId());
        } else {
            mSelectedView.setTag(uri);
            saveCustomActions();
            updateDrawables();
        }
    }

    private void saveCustomActions() {
        ArrayList<String> targets = new ArrayList<String>();
        for (int id : sIconIds) {
            View v = getView().findViewById(id);
            String uri = (String) v.getTag();
            targets.add(uri);
        }
        mShortcutHelper.saveTargets(targets);
    }

}
