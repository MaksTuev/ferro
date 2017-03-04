package com.agna.ferro.rx;


import org.junit.Test;
import org.mockito.Mockito;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOperator;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.ArrayCompositeDisposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.subjects.PublishSubject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MaybeOperatorFreezeTest {
    
    PublishSubject<Boolean> freezeSelector = PublishSubject.create();

    @Test
    public void freezeSuccessEvent(){
        Maybe<Integer> maybe = Maybe.just(1)
                .lift(new MaybeOperatorFreeze<Integer>(freezeSelector));
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();

        maybe.subscribe(observer);

        verify(observer, never()).onSuccess(any(Integer.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, times(1)).onSuccess(1);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeCompleteEvent(){
        Maybe maybe = Maybe.empty()
                .lift(new MaybeOperatorFreeze(freezeSelector));
        MaybeObserver observer = TestHelper.mockMaybeObserver();

        maybe.subscribe(observer);

        verify(observer, never()).onSuccess(any());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, never()).onSuccess(any());
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeErrorEvent(){
        Maybe maybe = Maybe.error(new TestException())
                .lift(new MaybeOperatorFreeze(freezeSelector));
        MaybeObserver observer = TestHelper.mockMaybeObserver();

        maybe.subscribe(observer);

        verify(observer, never()).onSuccess(any());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, never()).onSuccess(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void selectorError(){
        Maybe maybe = Maybe.empty()
                .lift(new MaybeOperatorFreeze(Observable.<Boolean>error(new TestException())));
        MaybeObserver observer = TestHelper.mockMaybeObserver();

        maybe.subscribe(observer);

        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.error(new TestException())
                .lift(new MaybeOperatorFreeze(freezeSelector)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {
            @Override
            public MaybeSource<Object> apply(@NonNull Maybe<Object> maybe) throws Exception {
                return maybe.lift(new MaybeOperatorFreeze(freezeSelector));

            }
        });
    }
}
