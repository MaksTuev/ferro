package com.agna.ferro.rx;


import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.BackpressureStrategy;
import io.reactivex.FlowableOperator;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subscribers.SerializedSubscriber;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FlowableOperatorFreezeTest {

    PublishSubject<Boolean> freezeSelector = PublishSubject.create();

    @Test
    public void freezeOnNextEvent(){
        PublishSubject<Integer> sourceSubject = PublishSubject.<Integer>create();
        Flowable<Integer> flowable = sourceSubject.toFlowable(BackpressureStrategy.MISSING)
                .lift(new FlowableOperatorFreeze<Integer>(freezeSelector));
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();

        flowable.subscribe(subscriber);

        sourceSubject.onNext(1);

        verify(subscriber, never()).onNext(any(Integer.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));

        sourceSubject.onNext(1);

        verify(subscriber, times(2)).onNext(1);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));

        freezeSelector.onNext(true);
        sourceSubject.onNext(1);

        verify(subscriber, times(2)).onNext(1);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeOnNextEventWithReplaceFrozenEventPredicate(){
        PublishSubject<Integer> sourceSubject = PublishSubject.<Integer>create();
        Flowable<Integer> flowable = sourceSubject.toFlowable(BackpressureStrategy.MISSING)
                .lift(new FlowableOperatorFreeze<Integer>(freezeSelector, new BiFunction<Integer, Integer, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return true;
                    }
                }));
        Subscriber<Integer> observer = TestHelper.mockSubscriber();

        flowable.subscribe(observer);

        sourceSubject.onNext(1);
        sourceSubject.onNext(1);
        sourceSubject.onNext(1);

        verify(observer, never()).onNext(any(Integer.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, times(1)).onNext(1);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeCompleteEvent(){
        Flowable flowable = Flowable.empty()
                .lift(new FlowableOperatorFreeze(freezeSelector));
        Subscriber observer = TestHelper.mockSubscriber();

        flowable.subscribe(observer);

        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, never()).onNext(any());
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeErrorEvent(){
        Flowable flowable = Flowable.error(new TestException())
                .lift(new FlowableOperatorFreeze(freezeSelector));
        Subscriber observer = TestHelper.mockSubscriber();

        flowable.subscribe(observer);

        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void selectorError(){
        Flowable flowable = Flowable.empty()
                .lift(new FlowableOperatorFreeze(Observable.<Boolean>error(new TestException())));
        Subscriber observer = TestHelper.mockSubscriber();

        flowable.subscribe(observer);

        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.error(new TestException())
                .lift(new FlowableOperatorFreeze(freezeSelector)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {
            @Override
            public Publisher<Object> apply(@NonNull Flowable<Object> flowable) throws Exception {
                return flowable.lift(new FlowableOperatorFreeze(freezeSelector));
            }
        });
    }


}