/*
 * SPDX-FileCopyrightText: 2026 BMobile contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.setupdesign.GlifLayout;

public final class SetupWizardAnimationHelper {

    private static final long STAGGER_MS = 120L;
    private static final long DURATION_MS = 420L;

    private SetupWizardAnimationHelper() {
    }

    public static boolean areAnimationsEnabled(Context context) {
        if (!ValueAnimator.areAnimatorsEnabled()) {
            return false;
        }
        float scale = Settings.Global.getFloat(
                context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f);
        return scale > 0f;
    }

    public static void runStaggeredEntrance(ViewGroup container, boolean enabled) {
        if (container == null || !enabled) {
            return;
        }
        final int childCount = container.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = container.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(child.getResources().getDisplayMetrics().density * 24f);
            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * STAGGER_MS)
                    .setDuration(DURATION_MS)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    public static void runGlifEntrance(GlifLayout layout, boolean enabled) {
        if (layout == null || !enabled) {
            return;
        }
        View header = layout.findViewById(com.google.android.setupdesign.R.id.sud_layout_header);
        View description = layout.findViewById(
                com.google.android.setupdesign.R.id.sud_layout_description);
        animateSingle(header, 0, enabled);
        animateSingle(description, STAGGER_MS, enabled);
        View icon = layout.findViewById(com.google.android.setupdesign.R.id.sud_layout_icon);
        animateSingle(icon, STAGGER_MS / 2, enabled);
    }

    public static void applyParallax(View hero, float position, boolean enabled) {
        if (hero == null || !enabled) {
            return;
        }
        if (position < -1f || position > 1f) {
            return;
        }
        final int width = hero.getWidth();
        if (width == 0) {
            return;
        }
        hero.setTranslationX(-position * width * 0.25f);
    }

    public static void applyParallax(LottieAnimationView lottie, float position, boolean enabled) {
        applyParallax((View) lottie, position, enabled);
    }

    private static void animateSingle(View view, long delay, boolean enabled) {
        if (view == null || !enabled) {
            return;
        }
        view.setAlpha(0f);
        view.setTranslationY(view.getResources().getDisplayMetrics().density * 20f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(DURATION_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
