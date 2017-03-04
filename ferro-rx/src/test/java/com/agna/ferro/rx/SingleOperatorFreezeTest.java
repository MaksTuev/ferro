package com.agna.ferro.rx;


import org.junit.Test;
import org.mockito.ArgumentMatchers;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOperator;
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

public class SingleOperatorFreezeTest {

    PublishSubject<Boolean> freezeSelector = PublishSubject.create();

    @Test
    public void freezeSuccessEvent(){
        Single<Integer> single = Single.just(1)
                .lift(new SingleOperatorFreeze<Integer>(freezeSelector));
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();

        single.subscribe(observer);

        verify(observer, never()).onSuccess(any(Integer.class));
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, times(1)).onSuccess(1);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeErrorEvent(){
        Single single = Single.error(new TestException())
                .lift(new SingleOperatorFreeze(freezeSelector));
        SingleObserver observer = TestHelper.mockSingleObserver();

        single.subscribe(observer);

        verify(observer, never()).onSuccess(any());
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, never()).onSuccess(any());
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void selectorError(){
        Single single = Single.just(1)
                .lift(new SingleOperatorFreeze(Observable.<Boolean>error(new TestException())));
        SingleObserver observer = TestHelper.mockSingleObserver();

        single.subscribe(observer);

        verify(observer, never()).onSuccess(any());
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.error(new TestException())
                .lift(new SingleOperatorFreeze(freezeSelector)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Object>, SingleSource<Object>>() {
            @Override
            public SingleSource<Object> apply(@NonNull Single<Object> single) throws Exception {
                return single.lift(new SingleOperatorFreeze(freezeSelector));

            }
        });
    }
}
