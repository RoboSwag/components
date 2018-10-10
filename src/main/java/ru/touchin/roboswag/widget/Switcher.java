package ru.touchin.roboswag.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.NoSuchElementException;

import ru.touchin.roboswag.components.R;

public class Switcher extends FrameLayout {

    @IdRes
    private final int defaultChild;

    @Nullable
    private Animation inAnimation;

    @Nullable
    private Animation outAnimation;

    public Switcher(@NonNull final Context context) {
        this(context, null);
    }

    public Switcher(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.Switcher, 0, R.style.Switcher);
        inAnimation = AnimationUtils.loadAnimation(context, array.getResourceId(R.styleable.Switcher_android_inAnimation, View.NO_ID));
        outAnimation = AnimationUtils.loadAnimation(context, array.getResourceId(R.styleable.Switcher_android_outAnimation, View.NO_ID));
        defaultChild = array.getResourceId(R.styleable.Switcher_defaultChild, View.NO_ID);
        array.recycle();
    }

    @Override
    public void addView(@NonNull final View child, final int index, @Nullable final ViewGroup.LayoutParams params) {
        if (child.getId() == defaultChild || defaultChild == View.NO_ID && getChildCount() == 0) {
            child.setVisibility(View.VISIBLE);
        } else {
            child.setVisibility(View.GONE);
        }
        super.addView(child, index, params);
    }

    public void showChild(@IdRes final int id) {
        boolean found = false;
        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);
            if (child.getId() == id) {
                found = true;
                setVisibilityWithAnimation(child, View.VISIBLE);
            } else {
                setVisibilityWithAnimation(child, View.GONE);
            }
        }
        if (!found) {
            throw new NoSuchElementException();
        }
    }

    private void setVisibilityWithAnimation(@NonNull final View view, final int targetVisibility) {
        final Animation animation = targetVisibility == View.VISIBLE ? inAnimation : outAnimation;
        if (view.getVisibility() != targetVisibility) {
            if (ViewCompat.isLaidOut(this) && animation != null) {
                view.startAnimation(animation);
            }
            view.setVisibility(targetVisibility);
        }
    }

}
