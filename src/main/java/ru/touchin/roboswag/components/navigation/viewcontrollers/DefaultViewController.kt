package ru.touchin.roboswag.components.navigation.viewcontrollers

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.LayoutRes
import android.support.v4.app.FragmentActivity
import ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment

abstract class DefaultViewController<TActivity : FragmentActivity, TState : Parcelable>(
        @LayoutRes layoutRes: Int,
        creationContext: CreationContext,
        savedInstanceState: Bundle?
) : ViewController<TActivity, ViewControllerFragment<TState, TActivity>, TState>(
        creationContext,
        savedInstanceState
) {

    init {
        setContentView(layoutRes)
    }

}
