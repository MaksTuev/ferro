package com.agna.ferro.rx;


import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.ObservableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.ArrayCompositeDisposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.observers.SerializedObserver;
import io.reactivex.subjects.PublishSubject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ObservableOperatorFreezeTest {

    PublishSubject<Boolean> freezeSelector = PublishSubject.create();

    @Test
    public void freezeOnNextEvent(){
        PublishSubject<Integer> sourceSubject = PublishSubject.<Integer>create();
        Observable<Integer> observable = sourceSubject
                .lift(new ObservableOperatorFreeze<Integer>(freezeSelector));
        Observer<Integer> observer = TestHelper.mockObserver();

        observable.subscribe(observer);

        sourceSubject.onNext(1);

        verify(observer, never()).onNext(any(Integer.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, times(1)).onNext(1);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        sourceSubject.onNext(1);

        verify(observer, times(2)).onNext(1);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(true);
        sourceSubject.onNext(1);

        verify(observer, times(2)).onNext(1);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeOnNextEventWithReplaceFrozenEventPredicate(){
        PublishSubject<Integer> sourceSubject = PublishSubject.<Integer>create();
        Observable<Integer> observable = sourceSubject
                .lift(new ObservableOperatorFreeze<Integer>(freezeSelector, new BiFunction<Integer, Integer, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return true;
                    }
                }));
        Observer<Integer> observer = TestHelper.mockObserver();

        observable.subscribe(observer);

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
        Observable observable = Observable.empty()
                .lift(new ObservableOperatorFreeze(freezeSelector));
        Observer observer = TestHelper.mockObserver();

        observable.subscribe(observer);

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
        Observable observable = Observable.error(new TestException())
                .lift(new ObservableOperatorFreeze(freezeSelector));
        Observer observer = TestHelper.mockObserver();

        observable.subscribe(observer);

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
        Observable observable = Observable.empty()
                .lift(new ObservableOperatorFreeze(Observable.<Boolean>error(new TestException())));
        Observer observer = TestHelper.mockObserver();

        observable.subscribe(observer);

        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.error(new TestException())
                .lift(new ObservableOperatorFreeze(freezeSelector)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> apply(@NonNull Observable<Object> observable) throws Exception {
                return observable.lift(new ObservableOperatorFreeze(freezeSelector));

            }
        });
    }
}

