package ru.touchin.roboswag.components.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import ru.touchin.roboswag.components.utils.lifecycle.BaseDestroyable
import ru.touchin.roboswag.components.utils.lifecycle.Destroyable
import ru.touchin.roboswag.components.navigation.AbstractState
import ru.touchin.roboswag.core.log.Lc
import ru.touchin.roboswag.core.utils.ShouldNotHappenException

open class StateViewModel<TState : AbstractState>(application: Application) : AndroidViewModel(application), Destroyable {

    private val baseDestroyable = BaseDestroyable()

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

    final override fun <T> untilDestroy(observable: Observable<T>): Disposable =
            baseDestroyable.untilDestroy(observable)

    final override fun <T> untilDestroy(observable: Observable<T>, onNextAction: Consumer<T>): Disposable =
            baseDestroyable.untilDestroy(observable, onNextAction)

    final override fun <T> untilDestroy(observable: Observable<T>, onNextAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable =
            baseDestroyable.untilDestroy(observable, onNextAction, onErrorAction)

    final override fun <T> untilDestroy(observable: Observable<T>, onNextAction: Consumer<T>, onErrorAction: Consumer<Throwable>, onCompletedAction: Action): Disposable =
            baseDestroyable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction)

    final override fun <T> untilDestroy(single: Single<T>): Disposable =
            baseDestroyable.untilDestroy(single)

    final override fun <T> untilDestroy(single: Single<T>, onSuccessAction: Consumer<T>): Disposable =
            baseDestroyable.untilDestroy(single, onSuccessAction)

    final override fun <T> untilDestroy(single: Single<T>, onSuccessAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable =
            baseDestroyable.untilDestroy(single, onSuccessAction, onErrorAction)

    final override fun untilDestroy(completable: Completable): Disposable =
            baseDestroyable.untilDestroy(completable)

    final override fun untilDestroy(completable: Completable, onCompletedAction: Action): Disposable =
            baseDestroyable.untilDestroy(completable, onCompletedAction)

    final override fun untilDestroy(completable: Completable, onCompletedAction: Action, onErrorAction: Consumer<Throwable>): Disposable =
            baseDestroyable.untilDestroy(completable, onCompletedAction, onErrorAction)

    final override fun <T> untilDestroy(maybe: Maybe<T>): Disposable =
            baseDestroyable.untilDestroy(maybe)

    final override fun <T> untilDestroy(maybe: Maybe<T>, onSuccessAction: Consumer<T>): Disposable =
            baseDestroyable.untilDestroy(maybe, onSuccessAction)

    final override fun <T> untilDestroy(maybe: Maybe<T>, onSuccessAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable =
            baseDestroyable.untilDestroy(maybe, onSuccessAction, onErrorAction)

    override fun onCleared() {
        baseDestroyable.onDestroy()
        Lc.d(javaClass.simpleName, "ViewModel destroyed")
    }
}
