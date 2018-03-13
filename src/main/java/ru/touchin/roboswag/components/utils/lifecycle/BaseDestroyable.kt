package ru.touchin.roboswag.components.utils.lifecycle

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.subjects.BehaviorSubject
import ru.touchin.roboswag.core.log.Lc
import ru.touchin.roboswag.core.utils.ShouldNotHappenException

/**
 * Created by Oksana Pokrovskaya on 7/03/18.
 * Simple implementation of [Destroyable]. Could be used to not implement interface but use such object inside.
 */
open class BaseDestroyable : Destroyable {

    companion object {
        private const val UNTIL_DESTROY_METHOD = "untilDestroy"
    }

    protected val isCreatedSubject = BehaviorSubject.create<Boolean>()!!

    /**
     * Call it on parent's onCreate method.
     */
    fun onCreate() {
        isCreatedSubject.onNext(true)
    }

    /**
     * Call it on parent's onDestroy method.
     */
    fun onDestroy() {
        isCreatedSubject.onNext(false)
    }

    override fun <T> untilDestroy(observable: Observable<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(observable, Functions.emptyConsumer(),
                getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD), Functions.EMPTY_ACTION)
    }

    override fun <T> untilDestroy(observable: Observable<T>,
                                  onNextAction: Consumer<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(observable, onNextAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD), Functions.EMPTY_ACTION)
    }

    override fun <T> untilDestroy(observable: Observable<T>,
                                  onNextAction: Consumer<T>,
                                  onErrorAction: Consumer<Throwable>): Disposable =
        untilDestroy(observable, onNextAction, onErrorAction, Functions.EMPTY_ACTION)

    override fun <T> untilDestroy(observable: Observable<T>,
                                  onNextAction: Consumer<T>,
                                  onErrorAction: Consumer<Throwable>,
                                  onCompletedAction: Action): Disposable =
        until(observable, isCreatedSubject.map { created -> !created }, onNextAction, onErrorAction, onCompletedAction)

    override fun <T> untilDestroy(single: Single<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(single, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD))
    }

    override fun <T> untilDestroy(single: Single<T>, onSuccessAction: Consumer<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(single, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD))
    }

    override fun <T> untilDestroy(single: Single<T>,
                                  onSuccessAction: Consumer<T>,
                                  onErrorAction: Consumer<Throwable>): Disposable =
        until(single.toObservable(), isCreatedSubject.map { created -> !created }, onSuccessAction, onErrorAction, Functions.EMPTY_ACTION)

    override fun untilDestroy(completable: Completable): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(completable, Functions.EMPTY_ACTION, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD))
    }

    override fun untilDestroy(completable: Completable, onCompletedAction: Action): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(completable, onCompletedAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD))
    }

    override fun untilDestroy(completable: Completable,
                              onCompletedAction: Action,
                              onErrorAction: Consumer<Throwable>): Disposable =
        until(completable.toObservable(), isCreatedSubject.map { created -> !created },
                Functions.emptyConsumer<Any>(), onErrorAction, onCompletedAction)

    override fun <T> untilDestroy(maybe: Maybe<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(maybe, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD))
    }

    override fun <T> untilDestroy(maybe: Maybe<T>, onSuccessAction: Consumer<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilDestroy(maybe, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD))
    }

    override fun <T> untilDestroy(maybe: Maybe<T>,
                                  onSuccessAction: Consumer<T>,
                                  onErrorAction: Consumer<Throwable>): Disposable =
        until(maybe.toObservable(), isCreatedSubject.map { created -> !created }, onSuccessAction, onErrorAction, Functions.EMPTY_ACTION)

    protected fun <T> until(observable: Observable<T>,
                          conditionSubject: Observable<Boolean>,
                          onNextAction: Consumer<T>,
                          onErrorAction: Consumer<Throwable>,
                          onCompletedAction: Action): Disposable {
        val actualObservable: Observable<T> = if (onNextAction === Functions.emptyConsumer<Any>()
                && onErrorAction === Functions.emptyConsumer<Any>() as Consumer<*>
                && onCompletedAction === Functions.EMPTY_ACTION) {
            observable
        } else {
            observable.observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(onCompletedAction)
                    .doOnNext(onNextAction)
                    .doOnError(onErrorAction)
        }

        return isCreatedSubject.firstOrError()
                .flatMapObservable { created -> if (created) actualObservable else Observable.empty() }
                .takeUntil(conditionSubject.filter { condition -> condition })
                .onErrorResumeNext { throwable: Throwable ->
                    if (throwable is RuntimeException) {
                        Lc.assertion(throwable)
                    }
                    Observable.empty<T>()
                }
                .subscribe()
    }

    private fun getActionThrowableForAssertion(codePoint: String, method: String): Consumer<Throwable> =
        Consumer { t -> Lc.assertion(ShouldNotHappenException("Unexpected error on $method at $codePoint", t)) }
}