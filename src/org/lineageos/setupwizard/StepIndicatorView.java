/*
 * SPDX-FileCopyrightText: 2026 BMobile contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.android.settingslib.Utils;

public class StepIndicatorView extends LinearLayout {

    private int mStepCount;
    private int mActiveStep;
    private final int mDotSize;
    private final int mActiveWidth;
    private final int mSpacing;

    public StepIndicatorView(Context context) {
        this(context, null);
    }

    public StepIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        float density = context.getResources().getDisplayMetrics().density;
        mDotSize = (int) (density * 8f);
        mActiveWidth = (int) (density * 28f);
        mSpacing = (int) (density * 8f);
    }

    public void setProgress(int activeStep, int stepCount) {
        if (stepCount <= 0) {
            setVisibility(GONE);
            return;
        }
        mActiveStep = Math.max(0, Math.min(activeStep, stepCount - 1));
        mStepCount = stepCount;
        setVisibility(VISIBLE);
        rebuildDots();
    }

    private void rebuildDots() {
        removeAllViews();
        int accent = Utils.getColorAccent(getContext()).getDefaultColor();
        int inactiveFill = (accent & 0x00FFFFFF) | 0x22000000;
        for (int i = 0; i < mStepCount; i++) {
            android.view.View dot = new android.view.View(getContext());
            boolean active = i == mActiveStep;
            int width = active ? mActiveWidth : mDotSize;
            LayoutParams params = new LayoutParams(width, mDotSize);
            params.setMarginEnd(mSpacing);
            dot.setLayoutParams(params);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(mDotSize / 2f);
            if (active) {
                shape.setColor(accent);
            } else {
                shape.setColor(inactiveFill);
                shape.setStroke(Math.max(1, mDotSize / 8), accent);
            }
            dot.setBackground(shape);
            addView(dot);
        }
    }
}
