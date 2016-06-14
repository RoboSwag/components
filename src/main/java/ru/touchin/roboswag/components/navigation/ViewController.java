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

package ru.touchin.roboswag.components.navigation;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment;
import ru.touchin.roboswag.components.observables.ui.BaseUiBindable;
import ru.touchin.roboswag.components.observables.ui.UiBindable;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Class to control view of specific fragment, activity and application by logic bridge.
 *
 * @param <TActivity> Type of activity where such {@link ViewController} could be;
 * @param <TFragment> Type of fragment where such {@link ViewController} could be;
 */
public class ViewController<TActivity extends ViewControllerActivity<?>,
        TFragment extends ViewControllerFragment<?, TActivity>>
        implements UiBindable {

    @NonNull
    private final TActivity activity;
    @NonNull
    private final TFragment fragment;
    @NonNull
    private final ViewGroup container;
    @NonNull
    private final BaseUiBindable baseUiBindable = new BaseUiBindable();
    private boolean destroyed;

    @SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter"})
    //UnusedFormalParameter: savedInstanceState could be used by children
    public ViewController(@NonNull final CreationContext creationContext,
                          @Nullable final Bundle savedInstanceState) {
        this.activity = (TActivity) creationContext.activity;
        this.fragment = (TFragment) creationContext.fragment;
        this.container = creationContext.container;
    }

    /**
     * Returns activity where {@link ViewController} could be.
     *
     * @return Returns activity.
     */
    @NonNull
    public final TActivity getActivity() {
        return activity;
    }

    /**
     * Returns fragment where {@link ViewController} could be.
     *
     * @return Returns fragment.
     */
    @NonNull
    public final TFragment getFragment() {
        return fragment;
    }

    /**
     * Returns view instantiated in {@link #getFragment()} fragment attached to {@link #getActivity()} activity.
     * Use it to inflate your views into at construction of this {@link ViewController}.
     *
     * @return Returns view.
     */
    @NonNull
    public final ViewGroup getContainer() {
        return container;
    }

    /**
     * Returns if {@link ViewController} destroyed or not.
     *
     * @return True if it is destroyed.
     */
    public final boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Return a localized string from the application's package's default string table.
     *
     * @param resId Resource id for the string
     */
    public final String getString(@StringRes final int resId) {
        return getActivity().getString(resId);
    }

    /**
     * Return a localized formatted string from the application's package's default string table, substituting the format arguments as defined in
     * {@link java.util.Formatter} and {@link java.lang.String#format}.
     *
     * @param resId      Resource id for the format string
     * @param formatArgs The format arguments that will be used for substitution.
     */
    public final String getString(@StringRes final int resId, @NonNull final Object... formatArgs) {
        return getActivity().getString(resId, formatArgs);
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar etc.
     * If it will be called or not depends on {@link Fragment#hasOptionsMenu()} and {@link Fragment#isMenuVisible()}.
     *
     * @param menu     The options menu in which you place your items;
     * @param inflater Helper to inflate menu items.
     */
    public void onConfigureNavigation(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        // do nothing
    }

    /**
     * Calls right after construction of {@link ViewController}.
     * Happens at {@link ViewControllerFragment#onActivityCreated(View, ViewControllerActivity, Bundle)}.
     */
    @CallSuper
    public void onCreate() {
        baseUiBindable.onCreate();
    }

    /**
     * Calls when {@link ViewController} have started.
     * Happens at {@link ViewControllerFragment#onStart(View, ViewControllerActivity)}.
     */
    @CallSuper
    public void onStart() {
        baseUiBindable.onStart();
    }

    /**
     * Calls when {@link ViewController} should save it's state.
     * Happens at {@link ViewControllerFragment#onSaveInstanceState(Bundle)}.
     * Try not to use such method for saving state but use {@link ViewControllerFragment#getState()} from {@link #getFragment()}.
     */
    @CallSuper
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        // do nothing
    }

    /**
     * Calls when {@link ViewController} have stopped.
     * Happens at {@link ViewControllerFragment#onStop(View, ViewControllerActivity)}.
     */
    @CallSuper
    public void onStop() {
        baseUiBindable.onStop();
    }

    /**
     * Calls when {@link ViewController} have destroyed.
     * Happens usually at {@link ViewControllerFragment#onDestroyView(View)}. In some cases at {@link ViewControllerFragment#onDestroy()}.
     */
    @CallSuper
    public void onDestroy() {
        baseUiBindable.onDestroy();
        destroyed = true;
    }

    /**
     * Similar to {@link ViewControllerFragment#onOptionsItemSelected(MenuItem)}.
     *
     * @param item Selected menu item;
     * @return True if selection processed.
     */
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return false;
    }

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseUiBindable
    @NonNull
    @Override
    public <T> Subscription bind(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseUiBindable.bind(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable) {
        return baseUiBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseUiBindable.untilStop(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return baseUiBindable.untilStop(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction,
                                      @NonNull final Action0 onCompletedAction) {
        return baseUiBindable.untilStop(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        return baseUiBindable.untilDestroy(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseUiBindable.untilDestroy(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return baseUiBindable.untilDestroy(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return baseUiBindable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @SuppressWarnings("CPD-END")
    //CPD: it is same as in other implementation based on BaseUiBindable
    /**
     * Helper class to simplify constructor override.
     */
    public static class CreationContext {

        @NonNull
        private final ViewControllerActivity activity;
        @NonNull
        private final ViewControllerFragment fragment;
        @NonNull
        private final ViewGroup container;

        public CreationContext(@NonNull final ViewControllerActivity activity,
                               @NonNull final ViewControllerFragment fragment,
                               @NonNull final ViewGroup container) {
            this.activity = activity;
            this.fragment = fragment;
            this.container = container;
        }

    }

}