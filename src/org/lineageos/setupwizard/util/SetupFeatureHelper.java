/*
 * SPDX-FileCopyrightText: 2026 BMobile contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public final class SetupFeatureHelper {

    public static final String MICROG_PACKAGE = "com.google.android.gms";
    public static final String MICROG_SETTINGS_ACTIVITY = "org.microg.gms.ui.SettingsActivity";

    public static final String DAV_PACKAGE = "at.bitfire.davdroid";
    public static final String DAV_ACCOUNTS_ACTIVITY = "at.bitfire.davdroid.ui.AccountsActivity";

    public static final String SETTINGS_PACKAGE = "com.android.settings";
    public static final String MONET_GALLERY_ACTIVITY =
            "com.android.settings.display.MonetPresetGalleryActivity";

    public static final String KIDSHUB_PACKAGE = "com.bmobile.kidshub";
    public static final String KIDSHUB_ACTIVITY = "com.bmobile.kidshub.ui.HomeActivity";

    private SetupFeatureHelper() {
    }

    public static boolean isMicrogInstalled(Context context) {
        return isPackageInstalled(context, MICROG_PACKAGE)
                && resolveActivity(context, microgSettingsIntent()) != null;
    }

    public static boolean isDavInstalled(Context context) {
        if (!isPackageInstalled(context, DAV_PACKAGE)) {
            return false;
        }
        return resolveActivity(context, davAccountsIntent()) != null
                || context.getPackageManager().getLaunchIntentForPackage(DAV_PACKAGE) != null;
    }

    public static boolean isMonetGalleryAvailable(Context context) {
        return resolveActivity(context, monetGalleryIntent()) != null;
    }

    public static boolean isKidsHubAvailable(Context context) {
        return resolveActivity(context, kidsHubIntent()) != null;
    }

    public static Intent microgSettingsIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName(MICROG_PACKAGE, MICROG_SETTINGS_ACTIVITY));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    public static Intent davAccountsIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(DAV_PACKAGE, DAV_ACCOUNTS_ACTIVITY);
        return intent;
    }

    public static Intent davLauncherIntent(Context context) {
        return context.getPackageManager().getLaunchIntentForPackage(DAV_PACKAGE);
    }

    public static Intent monetGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(SETTINGS_PACKAGE, MONET_GALLERY_ACTIVITY);
        return intent;
    }

    public static Intent kidsHubIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(KIDSHUB_PACKAGE, KIDSHUB_ACTIVITY);
        return intent;
    }

    public static Intent resolveLaunchIntent(Context context, Feature feature) {
        switch (feature) {
            case MICROG:
                return isMicrogInstalled(context) ? microgSettingsIntent() : null;
            case DAV:
                if (!isDavInstalled(context)) {
                    return null;
                }
                Intent accounts = davAccountsIntent();
                if (resolveActivity(context, accounts) != null) {
                    return accounts;
                }
                return davLauncherIntent(context);
            case MONET:
                return isMonetGalleryAvailable(context) ? monetGalleryIntent() : null;
            case KIDSHUB:
                return isKidsHubAvailable(context) ? kidsHubIntent() : null;
            default:
                return null;
        }
    }

    private static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static ResolveInfo resolveActivity(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public enum Feature {
        MICROG,
        DAV,
        MONET,
        KIDSHUB
    }
}
