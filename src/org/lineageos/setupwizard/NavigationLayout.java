/*
 * SPDX-FileCopyrightText: 2021-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.setupcompat.template.FooterButtonStyleUtils;

public class NavigationLayout extends RelativeLayout {
    /*
     * An interface to listen to events of the navigation bar,
     * namely when the user clicks on the back or next button.
     */
    public interface NavigationBarListener {
        void onNavigateNext();

        void onSkip();
    }

    private final Button mNextButton;
    private final Button mSkipButton;
    private final StepIndicatorView mStepIndicator;

    public NavigationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.navigation_layout, this);
        mNextButton = findViewById(R.id.navbar_next);
        mSkipButton = findViewById(R.id.navbar_skip);
        mStepIndicator = findViewById(R.id.setup_step_indicator);
        FooterButtonStyleUtils.applyPrimaryButtonPartnerResource(context, mNextButton, true);
        mNextButton.setBackgroundResource(R.drawable.setup_onboarding_pill_button);
        FooterButtonStyleUtils.applySecondaryButtonPartnerResource(context, mSkipButton, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.NavigationLayout, 0, 0);
        final boolean showSkipButton;
        try {
            showSkipButton = a.getBoolean(
                    R.styleable.NavigationLayout_showSkipButton, false);
        } finally {
            a.recycle();
        }

        if (showSkipButton) {
            mSkipButton.setVisibility(View.VISIBLE);
        }
    }

    public Button getSkipButton() {
        return mSkipButton;
    }

    public Button getNextButton() {
        return mNextButton;
    }

    public void setNavigationBarListener(NavigationBarListener listener) {
        mSkipButton.setOnClickListener(view -> listener.onSkip());
        mNextButton.setOnClickListener(view -> listener.onNavigateNext());
    }

    public void setSetupProgress(int activeStep, int stepCount) {
        if (mStepIndicator != null) {
            mStepIndicator.setProgress(activeStep, stepCount);
        }
    }
}
