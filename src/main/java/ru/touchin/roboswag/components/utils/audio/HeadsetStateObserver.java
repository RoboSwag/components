/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.utils.audio;

import android.bluetooth.BluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 02/11/2015.
 * Simple observer of wired or wireless (bluetooth A2DP) headsets state (connected or not).
 * <br><font color="yellow"> You require android.permission.BLUETOOTH and API level >= 11 if want to observe wireless headset state </font>
 */
public final class HeadsetStateObserver {

    @NonNull
    private final AudioManager audioManager;
    @NonNull
    private final Observable<Boolean> isConnectedObservable;

    public HeadsetStateObserver(@NonNull final Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isConnectedObservable = Observable
                .<IsConnectedReceiver>create(subscriber -> {
                    subscriber.onNext(new IsConnectedReceiver(audioManager));
                    subscriber.onCompleted();
                })
                .switchMap(isConnectedReceiver -> Observable.combineLatest(isConnectedReceiver.isWiredConnectedChangedEvent,
                        isConnectedReceiver.isWirelessConnectedChangedEvent,
                        (isWiredConnected, isWirelessConnected) -> isWiredConnected || isWirelessConnected)
                        .distinctUntilChanged()
                        .doOnSubscribe(() -> {
                            final IntentFilter headsetStateIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                                headsetStateIntentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
                            }
                            context.registerReceiver(isConnectedReceiver, headsetStateIntentFilter);
                        })
                        .doOnUnsubscribe(() -> context.unregisterReceiver(isConnectedReceiver)))
                .replay(1)
                .refCount();
    }

    /**
     * Returns if wired or wireless headset is connected.
     *
     * @return True if headset is connected.
     */
    @SuppressWarnings("deprecation")
    public boolean isConnected() {
        return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
    }

    /**
     * Observes connection state of headset.
     *
     * @return Returns observable which will provide current connection state and any of it's udpdate.
     */
    @NonNull
    public Observable<Boolean> observeIsConnected() {
        return isConnectedObservable;
    }

    private static class IsConnectedReceiver extends BroadcastReceiver {

        @NonNull
        private final BehaviorSubject<Boolean> isWiredConnectedChangedEvent;
        @NonNull
        private final BehaviorSubject<Boolean> isWirelessConnectedChangedEvent;

        @SuppressWarnings("deprecation")
        public IsConnectedReceiver(@NonNull final AudioManager audioManager) {
            isWiredConnectedChangedEvent = BehaviorSubject.create(audioManager.isWiredHeadsetOn());
            isWirelessConnectedChangedEvent = BehaviorSubject.create(audioManager.isBluetoothA2dpOn());
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()) && !isInitialStickyBroadcast()) {
                isWiredConnectedChangedEvent.onNext(intent.getIntExtra("state", 0) != 0);
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB &&
                    BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
                final int bluetoothState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                switch (bluetoothState) {
                    case BluetoothA2dp.STATE_DISCONNECTED:
                        isWirelessConnectedChangedEvent.onNext(false);
                        break;
                    case BluetoothA2dp.STATE_CONNECTED:
                        isWirelessConnectedChangedEvent.onNext(true);
                        break;
                    default:
                        break;
                }
            }
        }

    }

}
