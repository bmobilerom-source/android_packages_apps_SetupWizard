/*
 * SPDX-FileCopyrightText: 2026 BMobile contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

public final class IntroPage {

    public static final int HERO_LOTTIE = 0;
    public static final int HERO_DRAWABLE = 1;

    public final int heroType;
    public final int heroResId;
    public final int titleResId;
    public final int bodyResId;
    public final boolean imageBelowText;
    public final boolean loopLottie;

    public IntroPage(int heroType, int heroResId, int titleResId, int bodyResId,
            boolean imageBelowText, boolean loopLottie) {
        this.heroType = heroType;
        this.heroResId = heroResId;
        this.titleResId = titleResId;
        this.bodyResId = bodyResId;
        this.imageBelowText = imageBelowText;
        this.loopLottie = loopLottie;
    }

    public static IntroPage lottie(int rawResId, int titleResId, int bodyResId,
            boolean imageBelowText, boolean loop) {
        return new IntroPage(HERO_LOTTIE, rawResId, titleResId, bodyResId, imageBelowText, loop);
    }

    public static IntroPage drawable(int drawableResId, int titleResId, int bodyResId,
            boolean imageBelowText) {
        return new IntroPage(HERO_DRAWABLE, drawableResId, titleResId, bodyResId, imageBelowText,
                false);
    }
}
