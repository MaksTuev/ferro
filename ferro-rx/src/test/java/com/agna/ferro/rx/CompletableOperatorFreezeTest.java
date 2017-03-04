package com.agna.ferro.rx;


import com.sun.org.apache.xpath.internal.operations.Bool;

import org.junit.Test;
import org.mockito.Mockito;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOperator;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.ArrayCompositeDisposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CompletableOperatorFreezeTest {

    PublishSubject<Boolean> freezeSelector = PublishSubject.create();

    @Test
    public void freezeCompleteEvent(){
        Completable completable = Completable.complete()
                .lift(new CompletableOperatorFreeze(freezeSelector));
        CompletableObserver observer = TestHelper.mockCompletableObserver();

        completable.subscribe(observer);

        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void freezeErrorEvent(){
        Completable completable = Completable.error(new TestException())
                .lift(new CompletableOperatorFreeze(freezeSelector));
        CompletableObserver observer = TestHelper.mockCompletableObserver();

        completable.subscribe(observer);

        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));

        freezeSelector.onNext(false);

        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void selectorError(){
        Completable completable = Completable.complete()
                .lift(new CompletableOperatorFreeze(Observable.<Boolean>error(new TestException())));
        CompletableObserver observer = TestHelper.mockCompletableObserver();

        completable.subscribe(observer);

        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Completable.error(new TestException())
                .lift(new CompletableOperatorFreeze(freezeSelector)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletable(new Function<Completable, CompletableSource>() {
            @Override
            public CompletableSource apply(Completable o) throws Exception {
                return o.lift(new CompletableOperatorFreeze(freezeSelector));
            }
        });
    }

}
