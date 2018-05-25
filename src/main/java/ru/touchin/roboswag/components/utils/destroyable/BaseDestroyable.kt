package ru.touchin.roboswag.components.utils.destroyable

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Oksana Pokrovskaya on 7/03/18.
 * Simple implementation of [Destroyable]. Could be used to not implement interface but use such object inside.
 */
open class BaseDestroyable : Destroyable {

    private val subscriptions = CompositeDisposable()

    override fun clearSubscriptions() = subscriptions.clear()

    /**
     * Call it on parent's onDestroy method.
     */
    fun onDestroy() = subscriptions.dispose()

    override fun <T> untilDestroy(
            flowable: Flowable<T>,
            onNextAction: (T) -> Unit,
            onErrorAction: (Throwable) -> Unit,
            onCompletedAction: () -> Unit
    ): Disposable = flowable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNextAction, onErrorAction, onCompletedAction)
            .also { subscriptions.add(it) }

    override fun <T> untilDestroy(
            observable: Observable<T>,
            onNextAction: (T) -> Unit,
            onErrorAction: (Throwable) -> Unit,
            onCompletedAction: () -> Unit
    ): Disposable = observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNextAction, onErrorAction, onCompletedAction)
            .also { subscriptions.add(it) }

    override fun <T> untilDestroy(
            single: Single<T>,
            onSuccessAction: (T) -> Unit,
            onErrorAction: (Throwable) -> Unit
    ): Disposable = single
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccessAction, onErrorAction)
            .also { subscriptions.add(it) }

    override fun untilDestroy(
            completable: Completable,
            onCompletedAction: () -> Unit,
            onErrorAction: (Throwable) -> Unit
    ): Disposable = completable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onCompletedAction, onErrorAction)
            .also { subscriptions.add(it) }

    override fun <T> untilDestroy(
            maybe: Maybe<T>,
            onSuccessAction: (T) -> Unit,
            onErrorAction: (Throwable) -> Unit,
            onCompletedAction: () -> Unit
    ): Disposable = maybe
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccessAction, onErrorAction, onCompletedAction)
            .also { subscriptions.add(it) }

}
