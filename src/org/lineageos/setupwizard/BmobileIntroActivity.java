/*
 * SPDX-FileCopyrightText: 2026 BMobile contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_EMERGENCY_DIAL;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;

import com.google.android.setupcompat.template.FooterButtonStyleUtils;
import com.google.android.setupcompat.util.SystemBarHelper;

import org.lineageos.setupwizard.util.SetupWizardAnimationHelper;
import org.lineageos.setupwizard.util.SetupWizardUtils;

import java.util.Arrays;
import java.util.List;

public class BmobileIntroActivity extends SubBaseActivity {

    private static final String ACTION_ACCESSIBILITY_SETTINGS =
            "android.settings.ACCESSIBILITY_SETTINGS_FOR_SUW";

    private ViewPager2 mPager;
    private LinearLayout mPageDots;
    private Button mNextButton;
    private Button mStartButton;
    private Button mSkipIntroButton;
    private Button mEmergencyButton;
    private boolean mAnimationsEnabled;
    private int mPageCount;

    @Override
    protected void onStartSubactivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onSetupStart();
        SystemBarHelper.setBackButtonVisible(getWindow(), false);
        mAnimationsEnabled = SetupWizardAnimationHelper.areAnimationsEnabled(this);

        mPager = findViewById(R.id.intro_pager);
        mPageDots = findViewById(R.id.intro_page_dots);
        mNextButton = findViewById(R.id.intro_next);
        mStartButton = findViewById(R.id.intro_start);
        mSkipIntroButton = findViewById(R.id.intro_skip_slides);
        mEmergencyButton = findViewById(R.id.emerg_dialer);

        List<IntroPage> pages = Arrays.asList(
                IntroPage.lottie(R.raw.intro_welcome, R.string.intro_slide_welcome_title,
                        R.string.intro_slide_welcome_body, false, true),
                IntroPage.lottie(R.raw.lottie_system_nav_fully_gestural,
                        R.string.intro_slide_privacy_title, R.string.intro_slide_privacy_body,
                        false, true),
                IntroPage.lottie(R.raw.intro_restore, R.string.intro_slide_restore_title,
                        R.string.intro_slide_restore_body, false, false));

        mPageCount = pages.size();
        mPager.setAdapter(new IntroPagerAdapter(pages, mAnimationsEnabled));
        mPager.setOffscreenPageLimit(mPageCount);
        buildPageDots(mPageCount);

        if (mAnimationsEnabled) {
            mPager.setPageTransformer((page, position) -> {
                page.setAlpha(0.5f + (1f - Math.abs(position)) * 0.5f);
            });
        }

        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateFooterForPage(position);
                updatePageDots(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!mAnimationsEnabled || mPageCount <= 1) {
                    return;
                }
                float progress = (position + positionOffset) / (mPageCount - 1);
                animateFooterProgress(progress);
            }
        });

        FooterButtonStyleUtils.applyPrimaryButtonPartnerResource(this, mNextButton, true);
        FooterButtonStyleUtils.applyPrimaryButtonPartnerResource(this, mStartButton, true);
        FooterButtonStyleUtils.applySecondaryButtonPartnerResource(this, mSkipIntroButton, true);

        mNextButton.setOnClickListener(v -> advanceIntro());
        mStartButton.setOnClickListener(v -> onNextPressed());
        mSkipIntroButton.setOnClickListener(v -> mPager.setCurrentItem(mPageCount - 1, true));

        findViewById(R.id.launch_accessibility).setOnClickListener(
                v -> startSubactivity(new Intent(ACTION_ACCESSIBILITY_SETTINGS)));

        if (SetupWizardUtils.hasTelephony(this)) {
            mEmergencyButton.setOnClickListener(
                    v -> startSubactivity(new Intent(ACTION_EMERGENCY_DIAL)));
            FooterButtonStyleUtils.applySecondaryButtonPartnerResource(this, mEmergencyButton, true);
        } else {
            mEmergencyButton.setVisibility(View.GONE);
        }

        Button engSkip = findViewById(R.id.eng_skip_setup);
        if (Build.TYPE.equals("eng")) {
            engSkip.setVisibility(View.VISIBLE);
            engSkip.setOnClickListener(v -> SetupWizardUtils.finishSetupWizard(this));
        }

        updateFooterForPage(0);
        if (mAnimationsEnabled) {
            mStartButton.setAlpha(0f);
            mStartButton.setTranslationY(40f);
        }
    }

    private void advanceIntro() {
        int next = mPager.getCurrentItem() + 1;
        if (next >= mPageCount) {
            onNextPressed();
        } else {
            mPager.setCurrentItem(next, true);
        }
    }

    private void updateFooterForPage(int position) {
        boolean lastPage = position >= mPageCount - 1;
        mNextButton.setVisibility(lastPage ? View.GONE : View.VISIBLE);
        mStartButton.setVisibility(lastPage ? View.VISIBLE : View.GONE);
        mNextButton.setTranslationX(0f);
        mStartButton.setTranslationY(0f);
        if (!mAnimationsEnabled) {
            mStartButton.setAlpha(lastPage ? 1f : 0f);
            mNextButton.setAlpha(lastPage ? 0f : 1f);
        } else if (lastPage) {
            mStartButton.setAlpha(1f);
            mNextButton.setAlpha(0f);
        } else {
            mStartButton.setAlpha(0f);
            mNextButton.setAlpha(1f);
        }
    }

    private void animateFooterProgress(float progress) {
        float startPhase = Math.max(0f, (progress - 0.6f) / 0.4f);
        if (startPhase > 0f && startPhase < 1f) {
            mNextButton.setVisibility(View.VISIBLE);
            mStartButton.setVisibility(View.VISIBLE);
        } else if (startPhase >= 1f) {
            mNextButton.setVisibility(View.GONE);
            mStartButton.setVisibility(View.VISIBLE);
        } else {
            mStartButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.VISIBLE);
        }
        mStartButton.setAlpha(startPhase);
        mStartButton.setTranslationY(40f * (1f - startPhase));
        mNextButton.setAlpha(1f - startPhase);
        mSkipIntroButton.setTranslationX(-80f * progress);
        mNextButton.setTranslationX(80f * progress * (1f - startPhase));
    }

    private void buildPageDots(int count) {
        mPageDots.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (density * 8f);
        int margin = (int) (density * 6f);
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(margin);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.intro_dot_inactive);
            mPageDots.addView(dot);
        }
    }

    private void updatePageDots(int active) {
        for (int i = 0; i < mPageDots.getChildCount(); i++) {
            mPageDots.getChildAt(i).setBackgroundResource(
                    i == active ? R.drawable.intro_dot_active : R.drawable.intro_dot_inactive);
        }
    }

    @Override
    protected void onPause() {
        setLottiePlaying(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAnimationsEnabled && mPager != null) {
            setLottiePlaying(true);
        }
    }

    private void setLottiePlaying(boolean playing) {
        if (mPager == null || mPager.getChildCount() == 0) {
            return;
        }
        View child = mPager.getChildAt(0);
        if (!(child instanceof RecyclerView)) {
            return;
        }
        RecyclerView recyclerView = (RecyclerView) child;
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View page = recyclerView.getChildAt(i);
            LottieAnimationView top = page.findViewById(R.id.intro_lottie_top);
            LottieAnimationView bottom = page.findViewById(R.id.intro_lottie_bottom);
            if (top != null && top.getVisibility() == View.VISIBLE) {
                if (playing) {
                    top.resumeAnimation();
                } else {
                    top.pauseAnimation();
                }
            }
            if (bottom != null && bottom.getVisibility() == View.VISIBLE) {
                if (playing) {
                    bottom.resumeAnimation();
                } else {
                    bottom.pauseAnimation();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.bmobile_intro_activity;
    }

    @Override
    protected int getTitleResId() {
        return -1;
    }

    @Override
    protected void applyForwardTransition() {
        com.google.android.setupdesign.transition.TransitionHelper.applyForwardTransition(
                this, com.google.android.setupdesign.transition.TransitionHelper.TRANSITION_SLIDE,
                true);
    }
}
