package ru.touchin.roboswag.components.extensions

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View

fun <T : View> RecyclerView.ViewHolder.findViewById(@IdRes resId: Int): T = itemView.findViewById(resId)

fun RecyclerView.ViewHolder.getText(@StringRes resId: Int): CharSequence = itemView.context.getText(resId)

fun RecyclerView.ViewHolder.getString(@StringRes resId: Int): String = itemView.context.getString(resId)

fun RecyclerView.ViewHolder.getString(@StringRes resId: Int, vararg args: Any): String = itemView.context.getString(resId, args)

@ColorInt
fun RecyclerView.ViewHolder.getColor(@ColorRes resId: Int): Int = ContextCompat.getColor(itemView.context, resId)

fun RecyclerView.ViewHolder.getColorStateList(@ColorRes resId: Int): ColorStateList? = ContextCompat.getColorStateList(itemView.context, resId)

fun RecyclerView.ViewHolder.getDrawable(@ColorRes resId: Int): Drawable? = ContextCompat.getDrawable(itemView.context, resId)
