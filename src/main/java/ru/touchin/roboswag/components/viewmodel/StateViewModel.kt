package ru.touchin.roboswag.components.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import ru.touchin.roboswag.components.utils.lifecycle.BaseDestroyable
import ru.touchin.roboswag.components.utils.lifecycle.Destroyable
import ru.touchin.roboswag.components.navigation.AbstractState
import ru.touchin.roboswag.core.log.Lc
import ru.touchin.roboswag.core.utils.ShouldNotHappenException

open class StateViewModel<TState : AbstractState>(application: Application,
                                                  val baseDestroyable: BaseDestroyable = BaseDestroyable())
    : AndroidViewModel(application), Destroyable by baseDestroyable {

    var state: TState? = null
        get() {
            if (field == null) {
                throw ShouldNotHappenException("You can not access state if it is not set yet")
            }
            return field
        }
        private set(value) {
            field = value
            baseDestroyable.onCreate()
        }

    fun state() = state!!

    init {
        untilDestroy(Observable.just(Any()), Consumer { onStateCreated() })
    }

    protected fun onStateCreated() {

    }

    override fun onCleared() {
        baseDestroyable.onDestroy()
        Lc.d(javaClass.simpleName, "ViewModel destroyed")
    }
}
