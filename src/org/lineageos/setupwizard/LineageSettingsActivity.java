/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static org.lineageos.setupwizard.SetupWizardApp.KEY_SEND_METRICS;
import static org.lineageos.setupwizard.SetupWizardApp.SETUP_FEATURE_DAV_DONE;
import static org.lineageos.setupwizard.SetupWizardApp.SETUP_FEATURE_KIDSHUB_DONE;
import static org.lineageos.setupwizard.SetupWizardApp.SETUP_FEATURE_MICROG_DONE;
import static org.lineageos.setupwizard.SetupWizardApp.SETUP_FEATURE_MONET_DONE;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;

import com.android.settingslib.Utils;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

import org.lineageos.setupwizard.util.SetupFeatureHelper;
import org.lineageos.setupwizard.util.SetupFeatureHelper.Feature;

public class LineageSettingsActivity extends BaseSetupWizardActivity {

    private SetupWizardApp mSetupWizardApp;

    private CheckBox mNavKeys;

    private boolean mSupportsKeyDisabler = false;

    private ActivityResultLauncher<Intent> mFeatureLauncher;
    private String mPendingFeatureDoneKey;

    private final View.OnClickListener mNavKeysClickListener = view -> {
        boolean checked = !mNavKeys.isChecked();
        mNavKeys.setChecked(checked);
        mSetupWizardApp.getSettingsBundle().putBoolean(DISABLE_NAV_KEYS, checked);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFeatureLauncher = registerForActivityResult(
                new StartDecoratedActivityForResult(),
                this::onFeatureActivityResult);
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        setNextText(R.string.next);

        View navKeysRow = findViewById(R.id.nav_keys);
        navKeysRow.setOnClickListener(mNavKeysClickListener);
        mNavKeys = findViewById(R.id.nav_keys_checkbox);
        mSupportsKeyDisabler = isKeyDisablerSupported(this);
        tintFeatureHeroIcon();
        if (mSupportsKeyDisabler) {
            mNavKeys.setChecked(LineageSettings.System.getIntForUser(getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0);
        } else {
            navKeysRow.setVisibility(View.GONE);
        }

        if (getResources().getBoolean(R.bool.setup_feature_microg_enabled)) {
            bindFeatureRow(R.id.setup_feature_microg,
                    R.string.setup_feature_microg_title,
                    R.string.setup_feature_microg_summary,
                    SETUP_FEATURE_MICROG_DONE,
                    Feature.MICROG);
        } else {
            hideFeatureRow(R.id.setup_feature_microg);
        }
        if (getResources().getBoolean(R.bool.setup_feature_dav_enabled)) {
            bindFeatureRow(R.id.setup_feature_dav,
                    R.string.setup_feature_dav_title,
                    R.string.setup_feature_dav_summary,
                    SETUP_FEATURE_DAV_DONE,
                    Feature.DAV);
        } else {
            hideFeatureRow(R.id.setup_feature_dav);
        }
        if (getResources().getBoolean(R.bool.setup_feature_monet_enabled)) {
            bindFeatureRow(R.id.setup_feature_monet,
                    R.string.setup_feature_monet_title,
                    R.string.setup_feature_monet_summary,
                    SETUP_FEATURE_MONET_DONE,
                    Feature.MONET);
        } else {
            hideFeatureRow(R.id.setup_feature_monet);
        }
        if (getResources().getBoolean(R.bool.setup_feature_kidshub_enabled)) {
            bindFeatureRow(R.id.setup_feature_kidshub,
                    R.string.setup_feature_kidshub_title,
                    R.string.setup_feature_kidshub_summary,
                    SETUP_FEATURE_KIDSHUB_DONE,
                    Feature.KIDSHUB);
        } else {
            hideFeatureRow(R.id.setup_feature_kidshub);
        }
        updateFeatureRowStates();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisableNavkeysOption();
        updateFeatureRowStates();
        mSetupWizardApp.getSettingsBundle().putBoolean(KEY_SEND_METRICS, false);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_lineage_settings;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_services;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_features;
    }

    private void bindFeatureRow(int rowId, int titleRes, int summaryRes, String doneKey,
            Feature feature) {
        final View row = findViewById(rowId);
        if (row == null) {
            return;
        }
        row.setVisibility(View.VISIBLE);
        final TextView title = row.findViewById(R.id.setup_feature_title);
        final TextView summary = row.findViewById(R.id.setup_feature_summary);
        if (title != null) {
            title.setText(titleRes);
        }
        if (summary != null) {
            summary.setText(summaryRes);
        }
        row.setOnClickListener(v -> openFeature(feature, doneKey, titleRes));
    }

    private void hideFeatureRow(int rowId) {
        final View row = findViewById(rowId);
        if (row != null) {
            row.setVisibility(View.GONE);
        }
    }

    private void openFeature(Feature feature, String doneKey, int titleRes) {
        final Intent intent = SetupFeatureHelper.resolveLaunchIntent(this, feature);
        if (intent == null) {
            Toast.makeText(this,
                    getString(R.string.setup_feature_not_installed, getString(titleRes)),
                    Toast.LENGTH_LONG).show();
            return;
        }
        mPendingFeatureDoneKey = doneKey;
        try {
            mFeatureLauncher.launch(decorateIntent(intent));
        } catch (ActivityNotFoundException e) {
            mPendingFeatureDoneKey = null;
            Toast.makeText(this,
                    getString(R.string.setup_feature_open_failed, getString(titleRes)),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void onFeatureActivityResult(ActivityResult result) {
        if (mPendingFeatureDoneKey != null) {
            // User opened the feature and returned to setup — mark complete (MicroG/DAV
            // typically finish with RESULT_CANCELED when the user presses back).
            mSetupWizardApp.getSettingsBundle().putBoolean(mPendingFeatureDoneKey, true);
        }
        mPendingFeatureDoneKey = null;
        updateFeatureRowStates();
    }

    private void updateFeatureRowStates() {
        final Bundle bundle = mSetupWizardApp.getSettingsBundle();
        if (getResources().getBoolean(R.bool.setup_feature_microg_enabled)) {
            updateFeatureStatus(R.id.setup_feature_microg,
                    bundle.getBoolean(SETUP_FEATURE_MICROG_DONE));
        }
        if (getResources().getBoolean(R.bool.setup_feature_dav_enabled)) {
            updateFeatureStatus(R.id.setup_feature_dav, bundle.getBoolean(SETUP_FEATURE_DAV_DONE));
        }
        if (getResources().getBoolean(R.bool.setup_feature_monet_enabled)) {
            updateFeatureStatus(R.id.setup_feature_monet,
                    bundle.getBoolean(SETUP_FEATURE_MONET_DONE));
        }
        if (getResources().getBoolean(R.bool.setup_feature_kidshub_enabled)) {
            updateFeatureStatus(R.id.setup_feature_kidshub,
                    bundle.getBoolean(SETUP_FEATURE_KIDSHUB_DONE));
        }
    }

    private void updateFeatureStatus(int rowId, boolean done) {
        final View row = findViewById(rowId);
        if (row == null) {
            return;
        }
        final TextView status = row.findViewById(R.id.setup_feature_status);
        if (status != null) {
            status.setText(done ? R.string.setup_feature_done : R.string.setup_feature_open);
        }
    }

    private void updateDisableNavkeysOption() {
        if (mSupportsKeyDisabler) {
            final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
            boolean enabled = LineageSettings.System.getIntForUser(getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;
            boolean checked = myPageBundle.containsKey(DISABLE_NAV_KEYS) ?
                    myPageBundle.getBoolean(DISABLE_NAV_KEYS) :
                    enabled;
            mNavKeys.setChecked(checked);
            myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked);
        }
    }

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }

    private void tintFeatureHeroIcon() {
        final ImageView hero = findViewById(R.id.services_hero_icon);
        if (hero == null) {
            return;
        }
        final Drawable icon = hero.getDrawable();
        if (icon != null) {
            icon.mutate().setTintList(Utils.getColorAccent(this));
            hero.setImageDrawable(icon);
        }
    }
}
