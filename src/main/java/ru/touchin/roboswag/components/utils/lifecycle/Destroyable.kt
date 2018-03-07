package ru.touchin.roboswag.components.utils.lifecycle

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

/**
 * Created by Oksana Pokrovskaya on 7/03/18.
 * Interface that should be implemented by lifecycle-based elements ([android.arch.lifecycle.ViewModel] etc.)
 * to not manually manage subscriptions.
 * Use [.untilDestroy] method to subscribe to observable where you want and unsubscribe onDestroy.
 */
interface Destroyable {

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable [Observable] to subscribe until onDestroy;
     * @param <T>        Type of emitted by observable items;
     * @return [Disposable] which is wrapping source maybe to unsubscribe from it onDestroy.
    </T> */
    fun <T> untilDestroy(observable: Observable<T>): Disposable

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction on every emitted item.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   [Observable] to subscribe until onDestroy;
     * @param onNextAction Action which will raise on every [Emitter.onNext] item;
     * @param <T>          Type of emitted by observable items;
     * @return [Disposable] which is wrapping source observable to unsubscribe from it onDestroy.
    </T> */
    fun <T> untilDestroy(observable: Observable<T>, onNextAction: Consumer<T>): Disposable

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction and onErrorAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    [Observable] to subscribe until onDestroy;
     * @param onNextAction  Action which will raise on every [Emitter.onNext] item;
     * @param onErrorAction Action which will raise on every [Emitter.onError] throwable;
     * @param <T>           Type of emitted by observable items;
     * @return [Disposable] which is wrapping source observable to unsubscribe from it onDestroy.
    </T> */
    fun <T> untilDestroy(observable: Observable<T>, onNextAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable        [Observable] to subscribe until onDestroy;
     * @param onNextAction      Action which will raise on every [Emitter.onNext] item;
     * @param onErrorAction     Action which will raise on every [Emitter.onError] throwable;
     * @param onCompletedAction Action which will raise at [Emitter.onComplete] on completion of observable;
     * @param <T>               Type of emitted by observable items;
     * @return [Disposable] which is wrapping source observable to unsubscribe from it onDestroy.
    </T> */
    fun <T> untilDestroy(observable: Observable<T>,
                         onNextAction: Consumer<T>, onErrorAction: Consumer<Throwable>, onCompletedAction: Action): Disposable

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single.
     * Don't forget to process errors if single can emit them.
     *
     * @param single [Single] to subscribe until onDestroy;
     * @param <T>    Type of emitted by single items;
     * @return [Disposable] which is wrapping source single to unsubscribe from it onDestroy.
    </T> */
    fun <T> untilDestroy(single: Single<T>): Disposable

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single and calls onSuccessAction on every emitted item.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          [Single] to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every [SingleEmitter.onSuccess] item;
     * @param <T>             Type of emitted by single items;
     * @return [Disposable] which is wrapping source single to unsubscribe from it onDestroy.
    </T> */
    fun <T> untilDestroy(single: Single<T>, onSuccessAction: Consumer<T>): Disposable

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single and calls onSuccessAction and onErrorAction on single events.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          [Single] to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every [SingleEmitter.onSuccess] item;
     * @param onErrorAction   Action which will raise on every [SingleEmitter.onError] throwable;
     * @param <T>             Type of emitted by single items;
     * @return [Disposable] which is wrapping source single to unsubscribe from it onDestroy.
    </T> */
    fun <T> untilDestroy(single: Single<T>, onSuccessAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable [Completable] to subscribe until onDestroy;
     * @return [Disposable] which is wrapping source completable to unsubscribe from it onDestroy.
     */
    fun untilDestroy(completable: Completable): Disposable

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable and calls onCompletedAction on completable item.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       [Completable] to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every [CompletableEmitter.onComplete] item;
     * @return [Disposable] which is wrapping source single to unsubscribe from it onDestroy.
     */
    fun untilDestroy(completable: Completable, onCompletedAction: Action): Disposable

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable and calls onCompletedAction and onErrorAction on completable events.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       [Completable] to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every [CompletableEmitter.onComplete] item;
     * @param onErrorAction     Action which will raise on every [CompletableEmitter.onError] throwable;
     * @return [Disposable] which is wrapping source completable to unsubscribe from it onDestroy.
     */
    fun untilDestroy(completable: Completable, onCompletedAction: Action, onErrorAction: Consumer<Throwable>): Disposable

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to the maybe.
     * Don't forget to process errors if maybe can emit them.
     *
     * @param maybe [Maybe] to subscribe until onDestroy;
     * @return [Disposable] which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    fun <T> untilDestroy(maybe: Maybe<T>): Disposable

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to the maybe and calls onCompletedAction on maybe item.
     * Don't forget to process errors if maybe can emit them.
     *
     * @param maybe           [Maybe] to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every [MaybeEmitter.onSuccess] ()} item;
     * @return [Disposable] which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    fun <T> untilDestroy(maybe: Maybe<T>, onSuccessAction: Consumer<T>): Disposable

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to the maybe and calls onSuccessAction and onErrorAction on maybe events.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe           [Maybe] to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every [MaybeEmitter.onSuccess] ()} item;
     * @param onErrorAction   Action which will raise on every [MaybeEmitter.onError] throwable;
     * @return [Disposable] which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    fun <T> untilDestroy(maybe: Maybe<T>, onSuccessAction: Consumer<T>, onErrorAction: Consumer<Throwable>): Disposable

}