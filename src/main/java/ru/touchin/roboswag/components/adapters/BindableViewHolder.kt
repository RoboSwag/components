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

package ru.touchin.roboswag.components.adapters

import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View

import ru.touchin.roboswag.components.utils.lifecycle.Stopable
import ru.touchin.roboswag.core.utils.ShouldNotHappenException

/**
 * Created by Gavriil Sitnikov on 12/8/2016.
 * ViewHolder that implements [Stopable] and uses parent bindable object as bridge (Activity, ViewController etc.).
 */
open class BindableViewHolder(private val baseStopable: Stopable, itemView: View)
    : RecyclerView.ViewHolder(itemView), Stopable by baseStopable {

    /**
     * Look for a child view with the given id.  If this view has the given id, return this view.
     *
     * @param id The id to search for;
     * @return The view that has the given id in the hierarchy.
     */
    fun <T : View> findViewById(@IdRes id: Int): T? =
        itemView.findViewById<View>(id) as T?
                ?: throw ShouldNotHappenException("No view for id=" + itemView.resources.getResourceName(id))

    /**
     * Return the string value associated with a particular resource ID.  It
     * will be stripped of any styled text information.
     *
     * @param resId The resource id to search for data;
     * @return String The string data associated with the resource.
     */
    fun getString(@StringRes resId: Int): String =
        itemView.resources.getString(resId)

    /**
     * Return the string value associated with a particular resource ID.  It
     * will be stripped of any styled text information.
     *
     * @param resId      The resource id to search for data;
     * @param formatArgs The format arguments that will be used for substitution.
     * @return String The string data associated with the resource.
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
        itemView.resources.getString(resId, *formatArgs)

    /**
     * Return the color value associated with a particular resource ID.
     * Starting in [android.os.Build.VERSION_CODES.M], the returned
     * color will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return int A single color value in the form 0xAARRGGBB.
     */
    @ColorInt
    fun getColor(@ColorRes resId: Int): Int =
        ContextCompat.getColor(itemView.context, resId)

    /**
     * Returns a drawable object associated with a particular resource ID.
     * Starting in [android.os.Build.VERSION_CODES.LOLLIPOP], the
     * returned drawable will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return Drawable An object that can be used to draw this resource.
     */
    fun getDrawable(@DrawableRes resId: Int): Drawable? =
        ContextCompat.getDrawable(itemView.context, resId)
}
