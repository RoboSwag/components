package ru.touchin.roboswag.components.navigation.viewcontrollers

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.LayoutRes
import android.support.v4.app.FragmentActivity

abstract class DefaultViewController<TActivity : FragmentActivity, TState : Parcelable>(
        @LayoutRes layoutRes: Int,
        creationContext: CreationContext,
        savedInstanceState: Bundle?
) : ViewController<TActivity, TState>(
        creationContext,
        savedInstanceState
) {

    init {
        setContentView(layoutRes)
    }

}
