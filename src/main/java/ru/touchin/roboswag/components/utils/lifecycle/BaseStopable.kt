package ru.touchin.roboswag.components.utils.lifecycle

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.subjects.BehaviorSubject
import ru.touchin.roboswag.core.log.Lc
import ru.touchin.roboswag.core.utils.ShouldNotHappenException

/**
 * Created by Oksana Pokrovskaya on 7/03/18.
 * Simple implementation of [Stopable]. Could be used to not implement interface but use such object inside.
 */
class BaseStopable : Stopable, BaseDestroyable() {

    private val isStartedSubject = BehaviorSubject.create<Boolean>()
    private val isInAfterSaving = BehaviorSubject.createDefault(false)

    /**
     * Call it on parent's onStart method.
     */
    fun onStart() {
        isStartedSubject.onNext(true)
    }

    /**
     * Call it on parent's onResume method.
     * It is needed as sometimes onSaveInstanceState() calling after onPause() with no onStop call. So lifecycle object going in stopped state.
     * In that case onResume will be called after onSaveInstanceState so lifecycle object is becoming started.
     */
    fun onResume() {
        isInAfterSaving.onNext(false)
    }

    /**
     * Call it on parent's onSaveInstanceState method.
     */
    fun onSaveInstanceState() {
        isInAfterSaving.onNext(true)
    }

    /**
     * Call it on parent's onStop method.
     */
    fun onStop() {
        isStartedSubject.onNext(false)
    }

    override fun <T> untilStop(observable: Observable<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(observable, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD), Functions.EMPTY_ACTION)
    }

    override fun <T> untilStop(observable: Observable<T>, onNextAction: Consumer<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(observable, onNextAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD), Functions.EMPTY_ACTION)
    }

    override fun <T> untilStop(observable: Observable<T>,
                               onNextAction: Consumer<T>,
                               onErrorAction: Consumer<Throwable>): Disposable {
        return untilStop(observable, onNextAction, onErrorAction, Functions.EMPTY_ACTION)
    }

    override fun <T> untilStop(observable: Observable<T>,
                               onNextAction: Consumer<T>,
                               onErrorAction: Consumer<Throwable>,
                               onCompletedAction: Action): Disposable {
        return until(observable, isStartedSubject.map { started -> !started }
                .delay { item -> isInAfterSaving.filter { inAfterSaving -> !inAfterSaving } },
                onNextAction, onErrorAction, onCompletedAction)
    }

    override fun <T> untilStop(single: Single<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(single, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD))
    }

    override fun <T> untilStop(single: Single<T>, onSuccessAction: Consumer<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(single, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD))
    }

    override fun <T> untilStop(single: Single<T>,
                               onSuccessAction: Consumer<T>,
                               onErrorAction: Consumer<Throwable>): Disposable {
        return until(single.toObservable(), isStartedSubject.map { started -> !started }
                .delay { item -> isInAfterSaving.filter { inAfterSaving -> !inAfterSaving } },
                onSuccessAction, onErrorAction, Functions.EMPTY_ACTION)
    }

    override fun untilStop(completable: Completable): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(completable, Functions.EMPTY_ACTION, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD))
    }

    override fun untilStop(completable: Completable,
                           onCompletedAction: Action): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(completable, onCompletedAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD))
    }

    override fun untilStop(completable: Completable,
                           onCompletedAction: Action,
                           onErrorAction: Consumer<Throwable>): Disposable {
        return until(completable.toObservable(), isStartedSubject.map { started -> !started }
                .delay { item -> isInAfterSaving.filter { inAfterSaving -> !inAfterSaving } },
                Functions.emptyConsumer<Any>(), onErrorAction, onCompletedAction)
    }

    override fun <T> untilStop(maybe: Maybe<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(maybe, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD))
    }

    override fun <T> untilStop(maybe: Maybe<T>, onSuccessAction: Consumer<T>): Disposable {
        val codePoint = Lc.getCodePoint(this, 2)
        return untilStop(maybe, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD))
    }

    override fun <T> untilStop(maybe: Maybe<T>,
                               onSuccessAction: Consumer<T>,
                               onErrorAction: Consumer<Throwable>): Disposable {
        return until(maybe.toObservable(), isStartedSubject.map { started -> !started }, onSuccessAction, onErrorAction, Functions.EMPTY_ACTION)
    }

    private fun getActionThrowableForAssertion(codePoint: String, method: String): Consumer<Throwable> {
        return Consumer { t -> Lc.assertion(ShouldNotHappenException("Unexpected error on $method at $codePoint", t)) }
    }

    companion object {
        private const val UNTIL_STOP_METHOD = "untilStop"
    }
}