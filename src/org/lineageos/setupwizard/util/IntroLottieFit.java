/*
 * SPDX-FileCopyrightText: 2026 BMobile contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.util;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import org.lineageos.setupwizard.R;

/**
 * Sizes intro hero containers so any Lottie composition fits inside the slide
 * without per-asset scale hacks.
 */
public final class IntroLottieFit {

    private IntroLottieFit() {
    }

    public static final class Size {
        public final int widthPx;
        public final int heightPx;

        Size(int widthPx, int heightPx) {
            this.widthPx = widthPx;
            this.heightPx = heightPx;
        }
    }

    public static Size compute(Resources res, int rawResId, int parentWidthPx) {
        final float compW;
        final float compH;
        if (rawResId == R.raw.lottie_system_nav_fully_gestural) {
            compW = 412f;
            compH = 300f;
        } else if (rawResId == R.raw.intro_welcome || rawResId == R.raw.intro_restore) {
            compW = 500f;
            compH = 500f;
        } else {
            compW = 1f;
            compH = 1f;
        }

        final int horizontalPadding = (int) (res.getDimension(R.dimen.intro_content_padding_horizontal) * 2f);
        final int maxWidth = parentWidthPx > 0
                ? parentWidthPx - horizontalPadding
                : (int) res.getDimension(R.dimen.intro_hero_max_width);
        final int maxHeight = (int) res.getDimension(R.dimen.intro_hero_max_height);

        // Wide heroes sit in a padded column; reserve slack so page parallax cannot clip edges.
        final int fitWidth = isWide(rawResId)
                ? (int) (maxWidth * (1f - res.getFraction(R.fraction.intro_hero_parallax_reserve, 1, 1)))
                : maxWidth;

        final float scale = Math.min(fitWidth / compW, maxHeight / compH);
        return new Size(Math.max(1, (int) (compW * scale)), Math.max(1, (int) (compH * scale)));
    }

    public static boolean isWide(int rawResId) {
        return rawResId == R.raw.lottie_system_nav_fully_gestural;
    }

    public static void applyToContainer(View itemView, View container, int rawResId) {
        final Runnable apply = () -> {
            final Resources res = itemView.getResources();
            final Size size = compute(res, rawResId, itemView.getWidth());
            final ViewGroup.LayoutParams lp = container.getLayoutParams();
            if (isWide(rawResId)) {
                // Fill the padded content column so the wide asset cannot spill past the screen edge.
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = size.heightPx;
            } else {
                lp.width = size.widthPx;
                lp.height = size.heightPx;
            }
            container.setLayoutParams(lp);
        };
        if (itemView.getWidth() > 0) {
            apply.run();
        } else {
            itemView.post(apply);
        }
    }
}
