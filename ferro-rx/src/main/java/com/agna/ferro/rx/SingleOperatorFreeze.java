package com.agna.ferro.rx;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOperator;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.ArrayCompositeDisposable;
import io.reactivex.internal.disposables.DisposableHelper;

/**
 * This operator freezes Single events (onError, onSuccess) when freeze selector emits true,
 * and unfreeze it after freeze selector emits false.
 * If freeze selector does not emit any elements, all events would be frozen
 *
 * Single after this operator can emit event in different threads
 * You should pass this operator in method {@link io.reactivex.Single#lift(SingleOperator)} for apply it
 */
public class SingleOperatorFreeze<T> implements SingleOperator<T, T> {

    private final Observable<Boolean> freezeSelector;

    public SingleOperatorFreeze(Observable<Boolean> freezeSelector) {
        this.freezeSelector = freezeSelector;
    }

    @Override
    public SingleObserver<? super T> apply(SingleObserver<? super T> child) throws Exception {
        return new FreezeObserver<>(child, freezeSelector);
    }

    private static final class FreezeObserver<T> implements SingleObserver<T> {

        private final SingleObserver<T> child;
        private final Observable<Boolean> freezeSelector;

        private final ArrayCompositeDisposable compositeDisposable = new ArrayCompositeDisposable(2);
        private Disposable s;

        private boolean frozen = true;
        private T successValue = null;
        private Throwable error = null;

        private FreezeObserver(SingleObserver<T> child, Observable<Boolean> freezeSelector) {
            this.child = child;
            this.freezeSelector = freezeSelector;
        }

        @Override
        public void onError(Throwable e) {
            if (isFinished()) {
                return;
            }
            synchronized (this) {
                error = e;
                if (!frozen) {
                    forceOnError(e);
                }
            }
        }

        @Override
        public void onSubscribe(Disposable s) {
            if(DisposableHelper.validate(this.s, s)) {
                this.s = s;
                freezeSelector.subscribe(new Observer<Boolean>() {
                    @Override
                    public void onComplete() {
                        forceOnError(new IllegalStateException("selector completed before source emit event"));
                    }

                    @Override
                    public void onError(Throwable e) {
                        forceOnError(e);
                    }

                    @Override
                    public void onSubscribe(Disposable s) {
                        compositeDisposable.setResource(1, s);
                    }

                    @Override
                    public void onNext(Boolean freeze) {
                        setFrozen(freeze);
                    }
                });

                compositeDisposable.setResource(0, s);
                child.onSubscribe(compositeDisposable);
            }
        }

        @Override
        public void onSuccess(T event) {
            if (isFinished()) {
                return;
            }
            synchronized (this) {
                successValue = event;
                if (!frozen) {
                    forceOnSuccess(event);
                }
            }
        }

        private void forceOnError(Throwable e) {
            compositeDisposable.dispose();
            child.onError(e);
        }

        private void forceOnSuccess(T event){
            compositeDisposable.dispose();
            child.onSuccess(event);
        }

        private synchronized void setFrozen(boolean frozen) {
            this.frozen = frozen;
            if (!frozen) {
                if (error != null) {
                    forceOnError(error);
                } else if (successValue != null) {
                    forceOnSuccess(successValue);
                }
            }
        }

        private boolean isFinished() {
            return successValue != null || error != null;
        }
    }
}
