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

package ru.touchin.roboswag.components.navigation.activities;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.View;

import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * to support navigation based on {@link ru.touchin.roboswag.components.navigation.ViewController}s.
 *
 */
public abstract class ViewControllerActivity extends BaseActivity {

    @Override
    @Deprecated
    // use {@link #reconfigureNavigation}
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    @Override
    @Deprecated
    // use {@link #reconfigureNavigation}
    public void supportInvalidateOptionsMenu() {
        super.supportInvalidateOptionsMenu();
    }

    /**
     * Invalidates navigation and calls {@link #onConfigureNavigation} for all navigation elements.
     */
    public void reconfigureNavigation() {
        super.supportInvalidateOptionsMenu();
    }

    @Override
    @Deprecated
    // use {@link #onConfigureNavigation}
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        onConfigureNavigation(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar, AppBar etc.
     * It is calling before it's {@link ru.touchin.roboswag.components.navigation.ViewController}'s.
     *
     * @param menu The options menu in which you place your menu items.
     */
    public void onConfigureNavigation(@NonNull final Menu menu) {
        // do nothing
    }

    @NonNull
    @Override
    public <T extends View> T findViewById(@IdRes final int id) {
        final T viewById = super.findViewById(id);
        if (viewById == null) {
            throw new ShouldNotHappenException("No view for id=" + getResources().getResourceName(id));
        }
        return viewById;
    }

}
