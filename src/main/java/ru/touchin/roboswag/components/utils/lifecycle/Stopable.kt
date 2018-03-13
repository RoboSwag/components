package ru.touchin.roboswag.components.utils.lifecycle

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

/**
 * Created by Oksana Pokrovskaya on 7/03/18.
 * Interface that should be implemented by lifecycle-based elements ([android.app.Activity], [android.support.v4.app.Fragment] etc.)
 * to not manually manage subscriptions.
 * Use [.untilStop] method to subscribe to observable where you want and unsubscribe onStop.
 * Use [.untilDestroy] method to subscribe to observable where you want and unsubscribe onDestroy.
 */
interface Stopable : Destroyable {

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable [Observable] to subscribe until onStop;
     * @param <T>        Type of emitted by observable items;
     * @return [Disposable] which will unsubscribes from observable onStop.
    </T> */
    fun <T> untilStop(observable: Observable<T>): Disposable

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction on every emitted item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   [Observable] to subscribe until onStop;
     * @param onNextAction Action which will raise on every [Emitter.onNext] item;
     * @param <T>          Type of emitted by observable items;
     * @return [Disposable] which will unsubscribes from observable onStop.
    </T> */
    fun <T> untilStop(observable: Observable<T>, onNextAction: Consumer<T>): Disposable

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction and onErrorAction on observable events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    [Observable] to subscribe until onStop;
     * @param onNextAction  Action which will raise on every [Emitter.onNext] item;
     * @param onErrorAction Action which will raise on every [Emitter.onError] throwable;
     * @param <T>           Type of emitted by observable items;
     * @return [Disposable] which will unsubscribes from observable onStop.
    </T> */
    fun <T> untilStop(observable: Observable<T>, onNextAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable        [Observable] to subscribe until onStop;
     * @param onNextAction      Action which will raise on every [Emitter.onNext] item;
     * @param onErrorAction     Action which will raise on every [Emitter.onError] throwable;
     * @param onCompletedAction Action which will raise at [Emitter.onComplete] on completion of observable;
     * @param <T>               Type of emitted by observable items;
     * @return [Disposable] which is wrapping source observable to unsubscribe from it onStop.
    </T> */
    fun <T> untilStop(observable: Observable<T>,
                      onNextAction: Consumer<T>, onErrorAction: Consumer<Throwable>, onCompletedAction: Action): Disposable

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single [Single] to subscribe until onStop;
     * @param <T>    Type of emitted by single item;
     * @return [Disposable] which will unsubscribes from single onStop.
    </T> */
    fun <T> untilStop(single: Single<T>): Disposable

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single and calls onSuccessAction on the emitted item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          [Single] to subscribe until onStop;
     * @param onSuccessAction Action which will raise on every [SingleEmitter.onSuccess] item;
     * @param <T>             Type of emitted by single item;
     * @return [Disposable] which will unsubscribes from single onStop.
    </T> */
    fun <T> untilStop(single: Single<T>, onSuccessAction: Consumer<T>): Disposable

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single and calls onSuccessAction and onErrorAction on single events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          [Single] to subscribe until onStop;
     * @param onSuccessAction Action which will raise on every [SingleEmitter.onSuccess] item;
     * @param onErrorAction   Action which will raise on every [SingleEmitter.onError] throwable;
     * @param <T>             Type of emitted by observable items;
     * @return [Disposable] which is wrapping source single to unsubscribe from it onStop.
    </T> */
    fun <T> untilStop(single: Single<T>, onSuccessAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable [Completable] to subscribe until onStop;
     * @return [Disposable] which will unsubscribes from completable onStop.
     */
    fun untilStop(completable: Completable): Disposable

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable and calls onCompletedAction on completable item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       [Completable] to subscribe until onStop;
     * @param onCompletedAction Action which will raise at [CompletableEmitter.onComplete] on completion of observable;
     * @return [Disposable] which is wrapping source completable to unsubscribe from it onStop.
     */
    fun untilStop(completable: Completable, onCompletedAction: Action): Disposable

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable and calls onCompletedAction and onErrorAction on completable item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       [Completable] to subscribe until onStop;
     * @param onCompletedAction Action which will raise at [CompletableEmitter.onComplete] on completion of observable;
     * @param onErrorAction     Action which will raise on every [CompletableEmitter.onError] throwable;
     * @return [Disposable] which is wrapping source completable to unsubscribe from it onStop.
     */
    fun untilStop(completable: Completable, onCompletedAction: Action, onErrorAction: Consumer<Throwable>): Disposable

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onStop.
     * It is automatically subscribing to the maybe.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe [Maybe] to subscribe until onStop;
     * @return [Disposable] which will unsubscribes from completable onStop.
     */
    fun <T> untilStop(maybe: Maybe<T>): Disposable

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onStop.
     * It is automatically subscribing to the maybe and calls onCompletedAction on maybe item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe           [Maybe] to subscribe until onStop;
     * @param onSuccessAction Action which will raise at [MaybeEmitter.onSuccess] ()} on completion of observable;
     * @return [Disposable] which is wrapping source maybe to unsubscribe from it onStop.
     */
    fun <T> untilStop(maybe: Maybe<T>, onSuccessAction: Consumer<T>): Disposable

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onStop.
     * It is automatically subscribing to the maybe and calls onCompletedAction and onErrorAction on maybe item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe           [Maybe] to subscribe until onStop;
     * @param onSuccessAction Action which will raise at [MaybeEmitter.onSuccess] ()} on completion of observable;
     * @param onErrorAction   Action which will raise on every [MaybeEmitter.onError] throwable;
     * @return [Disposable] which is wrapping source maybe to unsubscribe from it onStop.
     */
    fun <T> untilStop(maybe: Maybe<T>, onSuccessAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable
}