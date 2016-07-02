/*
 * Copyright 2015 Maxim Tuev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agna.ferro.mvprx;

import android.support.annotation.CallSuper;

import com.agna.ferro.mvp.BaseView;
import com.agna.ferro.mvp.presenter.MvpPresenter;
import com.agna.ferro.rx.OperatorFreeze;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Presenter with freeze logic.
 * If subscribe to {@link Observable} via one of {@link #subscribe(Observable, Subscriber)} method,
 * all rx events (onNext, onError, onComplete) would be frozen when view destroyed and unfrozen
 * when view recreated (see {@link OperatorFreeze}).
 * When screen finally destroyed, all subscriptions would be automatically unsubscribed.
 */
public class MvpRxPresenter<V extends BaseView> extends MvpPresenter<V> {

    private final CompositeSubscription subscriptions = new CompositeSubscription();
    private final BehaviorSubject<Boolean> freezeSelector = BehaviorSubject.create(false);

    @CallSuper
    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        freezeSelector.onNext(false);
    }

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();
        freezeSelector.onNext(false);
    }

    @CallSuper
    @Override
    public void onStop() {
        super.onStop();
        freezeSelector.onNext(true);
    }

    @CallSuper
    @Override
    protected void onViewDetached() {
        super.onViewDetached();
        freezeSelector.onNext(true);
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    private <T> Subscription subscribe(final Observable<T> observable,
                                       final Subscriber<T> subscriber,
                                       final Observable.Operator<T, T> operator) {
        Subscription subscription = observable
                .lift(operator)
                .subscribe(subscriber);
        subscriptions.add(subscription);
        return subscription;
    }

    private <T> Subscription subscribe(final Observable<T> observable,
                                       final Action1<T> onNext,
                                       final Action1<Throwable> onError,
                                       final Observable.Operator<T, T> operator) {
        return subscribe(observable, new Subscriber<T>() {
            @Override
            public void onCompleted() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public void onNext(T t) {
                onNext.call(t);
            }
        }, operator);
    }

    protected <T> Subscription subscribe(final Observable<T> observable,
                                         final Subscriber<T> subscriber,
                                         final Func2<T, T, Boolean> replaceFrozenEventPredicate) {

        return subscribe(observable, subscriber, createOperatorFreeze(replaceFrozenEventPredicate));
    }

    protected <T> Subscription subscribe(final Observable<T> observable,
                                         final Action1<T> onNext,
                                         final Action1<Throwable> onError,
                                         final Func2<T, T, Boolean> replaceFrozenEventPredicate) {

        return subscribe(observable, onNext, onError, createOperatorFreeze(replaceFrozenEventPredicate));
    }

    protected <T> Subscription subscribe(final Observable<T> observable,
                                         final Subscriber<T> subscriber) {

        return subscribe(observable, subscriber, this.<T>createOperatorFreeze());
    }

    protected <T> Subscription subscribe(final Observable<T> observable,
                                         final Action1<T> onNext,
                                         final Action1<Throwable> onError) {

        return subscribe(observable, onNext, onError, this.<T>createOperatorFreeze());
    }

    protected <T> Subscription subscribeWithoutFreezing(final Observable<T> observable,
                                                        final Subscriber<T> subscriber) {

        Subscription subscription = observable
                .subscribe(subscriber);
        subscriptions.add(subscription);
        return subscription;
    }

    protected <T> Subscription subscribeWithoutFreezing(final Observable<T> observable,
                                                        final Action1<T> onNext,
                                                        final Action1<Throwable> onError) {

        return subscribeWithoutFreezing(observable, new Subscriber<T>() {
            @Override
            public void onCompleted() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public void onNext(T t) {
                onNext.call(t);
            }
        });
    }


    protected <T> OperatorFreeze<T> createOperatorFreeze(Func2<T, T, Boolean> replaceFrozenEventPredicate) {
        return new OperatorFreeze<>(freezeSelector, replaceFrozenEventPredicate);
    }

    protected <T> OperatorFreeze<T> createOperatorFreeze() {
        return new OperatorFreeze<>(freezeSelector);
    }

    protected boolean isSubscriptionInactive(Subscription subscription) {
        return subscription == null || subscription.isUnsubscribed();
    }
}
