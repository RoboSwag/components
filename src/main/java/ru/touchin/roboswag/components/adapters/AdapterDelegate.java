/*
 *  Copyright (c) 2017 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package ru.touchin.roboswag.components.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.ViewGroup;

import ru.touchin.roboswag.components.utils.lifecycle.Stopable;

/**
 * Objects of such class controls creation and binding of specific type of RecyclerView's ViewHolders.
 * Default {@link #getItemViewType} is generating on construction of object.
 *
 * @param <TViewHolder> Type of {@link BindableViewHolder} of delegate.
 */
@SuppressWarnings("PMD.TooManyMethods")
//TooManyMethods: it's ok
public abstract class AdapterDelegate<TViewHolder extends BindableViewHolder> {

    @NonNull
    private final Stopable parentStopable;
    private final int defaultItemViewType;

    public AdapterDelegate(@NonNull final Stopable parentStopable) {
        this.parentStopable = parentStopable;
        this.defaultItemViewType = ViewCompat.generateViewId();
    }

    /**
     * Returns parent {@link Stopable} that this delegate created from (e.g. Activity or ViewController).
     *
     * @return Parent {@link Stopable}.
     */
    @NonNull
    public Stopable getParentStopable() {
        return parentStopable;
    }

    /**
     * Unique ID of AdapterDelegate.
     *
     * @return Unique ID.
     */
    public int getItemViewType() {
        return defaultItemViewType;
    }

    /**
     * Creates ViewHolder to bind item to it later.
     *
     * @param parent Container of ViewHolder's view.
     * @return New ViewHolder.
     */
    @NonNull
    public abstract TViewHolder onCreateViewHolder(@NonNull final ViewGroup parent);
}
