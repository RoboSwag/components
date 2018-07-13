/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.navigation.fragments;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.lang.reflect.Constructor;

import ru.touchin.roboswag.components.R;
import ru.touchin.roboswag.components.navigation.viewcontrollers.ViewController;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Fragment instantiated in specific activity of {@link TActivity} type that is holding {@link ViewController} inside.
 *
 * @param <TState>    Type of object which is representing it's fragment state;
 * @param <TActivity> Type of {@link FragmentActivity} where fragment could be attached to.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class ViewControllerFragment<TActivity extends FragmentActivity, TState extends Parcelable> extends ViewFragment<TActivity> {

    private static final String VIEW_CONTROLLER_CLASS_EXTRA = "VIEW_CONTROLLER_CLASS_EXTRA";
    private static final String VIEW_CONTROLLER_STATE_EXTRA = "VIEW_CONTROLLER_STATE_EXTRA";

    private static boolean inDebugMode;
    private static long acceptableUiCalculationTime = 100;

    /**
     * Enables debugging features like serialization of {@link #getState()} every creation.
     */
    public static void setInDebugMode() {
        inDebugMode = true;
    }

    /**
     * Sets acceptable UI calculation time so there will be warnings in logs if ViewController's inflate/layout actions will take more than that time.
     * Works only if {@link #setInDebugMode()} called.
     * It's 100ms by default.
     */
    public static void setAcceptableUiCalculationTime(final long acceptableUiCalculationTime) {
        ViewControllerFragment.acceptableUiCalculationTime = acceptableUiCalculationTime;
    }

    @NonNull
    private static <T extends Parcelable> T reserialize(@NonNull final T parcelable) {
        Parcel parcel = Parcel.obtain();
        parcel.writeParcelable(parcelable, 0);
        final byte[] serializableBytes = parcel.marshall();
        parcel.recycle();
        parcel = Parcel.obtain();
        parcel.unmarshall(serializableBytes, 0, serializableBytes.length);
        parcel.setDataPosition(0);
        final T result = parcel.readParcelable(Thread.currentThread().getContextClassLoader());
        parcel.recycle();
        return result;
    }

    /**
     * Creates {@link Bundle} which will store state.
     *
     * @param state State to use into ViewController.
     * @return Returns bundle with state inside.
     */
    @NonNull
    public static Bundle args(@NonNull final Class<? extends ViewController> viewControllerClass, @Nullable final Parcelable state) {
        final Bundle result = new Bundle();
        result.putSerializable(VIEW_CONTROLLER_CLASS_EXTRA, viewControllerClass);
        result.putParcelable(VIEW_CONTROLLER_STATE_EXTRA, state);
        return result;
    }

    @Nullable
    private ViewController viewController;
    private Class<ViewController<TActivity, TState>> viewControllerClass;
    private TState state;
    @Nullable
    private ActivityResult pendingActivityResult;

    /**
     * Returns specific {@link Parcelable} which contains state of fragment and it's {@link ViewController}.
     *
     * @return Object represents state.
     */
    @NonNull
    public TState getState() {
        return state;
    }

    @NonNull
    public Class<ViewController<TActivity, TState>> getViewControllerClass() {
        return viewControllerClass;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(!isChildFragment());

        //noinspection unchecked
        viewControllerClass = (Class<ViewController<TActivity, TState>>) getArguments().getSerializable(VIEW_CONTROLLER_CLASS_EXTRA);
        state = savedInstanceState != null
                ? savedInstanceState.getParcelable(VIEW_CONTROLLER_STATE_EXTRA)
                : (getArguments() != null ? getArguments().getParcelable(VIEW_CONTROLLER_STATE_EXTRA) : null);
        if (state != null) {
            if (inDebugMode) {
                state = reserialize(state);
            }
        } else {
            Lc.assertion("State is required and null");
        }
    }

    @NonNull
    private ViewController createViewController(
            @NonNull final FragmentActivity activity,
            @NonNull final PlaceholderView view,
            @Nullable final Bundle savedInstanceState
    ) {
        if (viewControllerClass.getConstructors().length != 1) {
            throw new ShouldNotHappenException("There should be single constructor for " + viewControllerClass);
        }
        final Constructor<?> constructor = viewControllerClass.getConstructors()[0];
        final ViewController.CreationContext creationContext = new ViewController.CreationContext(activity, this, view);
        final long creationTime = inDebugMode ? SystemClock.elapsedRealtime() : 0;
        try {
            switch (constructor.getParameterTypes().length) {
                case 2:
                    return (ViewController) constructor.newInstance(creationContext, savedInstanceState);
                case 3:
                    return (ViewController) constructor.newInstance(this, creationContext, savedInstanceState);
                default:
                    throw new ShouldNotHappenException("Wrong constructor parameters count: " + constructor.getParameterTypes().length);
            }
        } catch (@NonNull final Exception exception) {
            throw new ShouldNotHappenException(exception);
        } finally {
            checkCreationTime(creationTime);
        }
    }

    private void checkCreationTime(final long creationTime) {
        if (inDebugMode) {
            final long creationPeriod = SystemClock.elapsedRealtime() - creationTime;
            if (creationPeriod > acceptableUiCalculationTime) {
                UiUtils.UI_METRICS_LC_GROUP.w("Creation of %s took too much: %dms", viewControllerClass, creationPeriod);
            }
        }
    }

    @NonNull
    @Override
    public final View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return new PlaceholderView(inflater.getContext(), viewControllerClass.getName());
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        viewController = createViewController(requireActivity(), (PlaceholderView) getView(), savedInstanceState);
        viewController.onCreate();
        if (pendingActivityResult != null) {
            viewController.onActivityResult(pendingActivityResult.requestCode, pendingActivityResult.resultCode, pendingActivityResult.data);
            pendingActivityResult = null;
        }
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        if (nextAnim == R.anim.fragment_slide_in_right_animation || nextAnim == R.anim.fragment_slide_out_right_animation) {
            ViewCompat.setTranslationZ(getView(), 1F);
        } else {
            ViewCompat.setTranslationZ(getView(), 0F);
        }
        if (viewController != null) {
            return viewController.onCreateAnimation(transit, enter, nextAnim);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(final int transit, final boolean enter, final int nextAnim) {
        if (viewController != null) {
            return viewController.onCreateAnimator(transit, enter, nextAnim);
        } else {
            return null;
        }
    }

    @Override
    public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (viewController != null) {
            viewController.onViewStateRestored(savedInstanceState);
        }
    }

    @Override
    protected void onStart(@NonNull final View view, @NonNull final TActivity activity) {
        super.onStart(view, activity);
        if (viewController != null) {
            viewController.onStart();
        }
    }

    @Override
    protected void onAppear(@NonNull final View view, @NonNull final TActivity activity) {
        super.onAppear(view, activity);
        if (viewController != null) {
            viewController.onAppear();
        }
    }

    @Override
    protected void onResume(@NonNull final View view, @NonNull final TActivity activity) {
        super.onResume(view, activity);
        if (viewController != null) {
            viewController.onResume();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (viewController != null) {
            viewController.onLowMemory();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (viewController != null) {
            viewController.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return (viewController != null && viewController.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(@NonNull final View view, @NonNull final TActivity activity) {
        super.onPause(view, activity);
        if (viewController != null) {
            viewController.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (viewController != null) {
            viewController.onSaveInstanceState(savedInstanceState);
        }
        savedInstanceState.putParcelable(VIEW_CONTROLLER_STATE_EXTRA, state);
    }

    @Override
    protected void onDisappear(@NonNull final View view, @NonNull final TActivity activity) {
        super.onDisappear(view, activity);
        if (viewController != null) {
            viewController.onDisappear();
        }
    }

    @Override
    protected void onStop(@NonNull final View view, @NonNull final TActivity activity) {
        if (viewController != null) {
            viewController.onStop();
        }
        super.onStop(view, activity);
    }

    @Override
    public void onDestroyView() {
        if (viewController != null) {
            viewController.onDestroy();
            viewController = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (viewController != null) {
            viewController.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        if (viewController != null) {
            viewController.onActivityResult(requestCode, resultCode, data);
        } else {
            pendingActivityResult = new ActivityResult(requestCode, resultCode, data);
        }
    }

    private static class PlaceholderView extends FrameLayout {

        @NonNull
        private final String tagName;
        private long lastMeasureTime;

        public PlaceholderView(@NonNull final Context context, @NonNull final String tagName) {
            super(context);
            this.tagName = tagName;
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (inDebugMode && lastMeasureTime == 0) {
                lastMeasureTime = SystemClock.uptimeMillis();
            }
        }

        @Override
        protected void onDraw(@NonNull final Canvas canvas) {
            super.onDraw(canvas);
            if (inDebugMode && lastMeasureTime > 0) {
                final long layoutTime = SystemClock.uptimeMillis() - lastMeasureTime;
                if (layoutTime > acceptableUiCalculationTime) {
                    UiUtils.UI_METRICS_LC_GROUP.w("Measure and layout of %s took too much: %dms", tagName, layoutTime);
                }
                lastMeasureTime = 0;
            }
        }

    }

    private static class ActivityResult {
        public final int requestCode;
        public final int resultCode;
        @Nullable
        public final Intent data;

        ActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

}
