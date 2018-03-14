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

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by Denis Karmyshakov 14.03.2018.
 * ViewHolder that implements {@link LifecycleOwner} and uses parent lifecycle
 * object as bridge ([android.app.Activity], [android.support.v4.app.Fragment] etc.).
 */
open class BindableViewHolder(
        private val lifecycleOwner: LifecycleOwner,
        itemView: View
) : RecyclerView.ViewHolder(itemView), LifecycleOwner by lifecycleOwner
