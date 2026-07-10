/*
 * SPDX-FileCopyrightText: 2026 BMobile contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.settingslib.Utils;

import org.lineageos.setupwizard.util.IntroLottieFit;

import java.util.List;

public class IntroPagerAdapter extends RecyclerView.Adapter<IntroPagerAdapter.IntroViewHolder> {

    private final List<IntroPage> mPages;
    private final boolean mAnimationsEnabled;

    public IntroPagerAdapter(List<IntroPage> pages, boolean animationsEnabled) {
        mPages = pages;
        mAnimationsEnabled = animationsEnabled;
    }

    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.intro_page_item, parent, false);
        return new IntroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
        holder.bind(mPages.get(position), mAnimationsEnabled);
    }

    @Override
    public void onViewRecycled(@NonNull IntroViewHolder holder) {
        holder.stopLottie();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mPages.size();
    }

    static final class IntroViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout mTopContainer;
        private final FrameLayout mBottomContainer;
        private final LottieAnimationView mLottieTop;
        private final LottieAnimationView mLottieBottom;
        private final ImageView mHeroTop;
        private final ImageView mHeroBottom;
        private final TextView mTitle;
        private final TextView mBody;

        IntroViewHolder(@NonNull View itemView) {
            super(itemView);
            mTopContainer = itemView.findViewById(R.id.intro_hero_top_container);
            mBottomContainer = itemView.findViewById(R.id.intro_hero_bottom_container);
            mLottieTop = itemView.findViewById(R.id.intro_lottie_top);
            mLottieBottom = itemView.findViewById(R.id.intro_lottie_bottom);
            mHeroTop = itemView.findViewById(R.id.intro_hero_top);
            mHeroBottom = itemView.findViewById(R.id.intro_hero_bottom);
            mTitle = itemView.findViewById(R.id.intro_title);
            mBody = itemView.findViewById(R.id.intro_body);
        }

        void bind(IntroPage page, boolean animationsEnabled) {
            mTitle.setText(page.titleResId);
            mBody.setText(page.bodyResId);
            mTopContainer.setTranslationX(0f);
            mBottomContainer.setTranslationX(0f);
            stopLottie();
            mHeroTop.setVisibility(View.GONE);
            mHeroBottom.setVisibility(View.GONE);
            mLottieTop.setVisibility(View.GONE);
            mLottieBottom.setVisibility(View.GONE);
            if (page.imageBelowText) {
                mTopContainer.setVisibility(View.GONE);
                mBottomContainer.setVisibility(View.VISIBLE);
                resizeHeroContainer(page, mBottomContainer);
                bindHero(page, mHeroBottom, mLottieBottom, animationsEnabled);
            } else {
                mTopContainer.setVisibility(View.VISIBLE);
                mBottomContainer.setVisibility(View.GONE);
                resizeHeroContainer(page, mTopContainer);
                bindHero(page, mHeroTop, mLottieTop, animationsEnabled);
            }
            if (animationsEnabled) {
                animateTextEntrance();
            }
        }

        private void resizeHeroContainer(IntroPage page, FrameLayout container) {
            IntroLottieFit.applyToContainer(itemView, container, page.heroResId);
        }

        private void bindHero(IntroPage page, ImageView imageView, LottieAnimationView lottieView,
                boolean animationsEnabled) {
            if (page.heroType == IntroPage.HERO_LOTTIE) {
                lottieView.setVisibility(View.VISIBLE);
                lottieView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                lottieView.setScaleX(1f);
                lottieView.setScaleY(1f);
                lottieView.setAnimation(page.heroResId);
                lottieView.loop(animationsEnabled && page.loopLottie);
                if (animationsEnabled) {
                    lottieView.playAnimation();
                } else {
                    lottieView.setProgress(1f);
                }
                return;
            }
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(page.heroResId);
            if (page.heroResId != R.drawable.logo) {
                imageView.setImageTintList(Utils.getColorAccent(itemView.getContext()));
            } else {
                imageView.setImageTintList(null);
            }
        }

        private void animateTextEntrance() {
            mTitle.setAlpha(0f);
            mTitle.setTranslationY(24f);
            mBody.setAlpha(0f);
            mBody.setTranslationY(32f);
            mTitle.animate().alpha(1f).translationY(0f).setStartDelay(200L).setDuration(500L).start();
            mBody.animate().alpha(1f).translationY(0f).setStartDelay(380L).setDuration(500L).start();
        }

        void stopLottie() {
            mLottieTop.cancelAnimation();
            mLottieBottom.cancelAnimation();
        }
    }
}
