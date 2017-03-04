package com.agna.ferro.rx;


import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOperator;
import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.ArrayCompositeDisposable;
import io.reactivex.internal.disposables.DisposableHelper;


/**
 * This operator freezes Completable events (onError, onComplete) when freeze selector emits true,
 * and unfreeze it after freeze selector emits false.
 * If freeze selector does not emit any elements, all events would be frozen
 *
 * Completable after this operator can emit event in different threads
 * You should pass this operator in method {@link io.reactivex.Completable#lift(CompletableOperator)}
 * for apply it
 */
public class CompletableOperatorFreeze implements CompletableOperator {

    private final Observable<Boolean> freezeSelector;

    public CompletableOperatorFreeze(Observable<Boolean> freezeSelector) {
        this.freezeSelector = freezeSelector;
    }

    @Override
    public CompletableObserver apply(CompletableObserver child) throws Exception {
        return new FreezeObserver(child, freezeSelector);
    }

    private static final class FreezeObserver implements CompletableObserver {

        private final CompletableObserver child;
        private final Observable<Boolean> freezeSelector;

        private final ArrayCompositeDisposable compositeDisposable = new ArrayCompositeDisposable(2);
        private Disposable s;

        private boolean frozen = true;
        private boolean done = false;
        private Throwable error = null;

        private FreezeObserver(CompletableObserver child, Observable<Boolean> freezeSelector) {
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
                        forceOnComplete();
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
        public void onComplete() {
            if (isFinished()) {
                return;
            }
            synchronized (this) {
                done = true;
                if (!frozen) {
                    forceOnComplete();
                }
            }
        }

        private void forceOnError(Throwable e) {
            compositeDisposable.dispose();
            child.onError(e);
        }

        private void forceOnComplete(){
            compositeDisposable.dispose();
            child.onComplete();
        }

        private synchronized void setFrozen(boolean frozen) {
            this.frozen = frozen;
            if (!frozen) {
                if (error != null) {
                    forceOnError(error);
                } else if (done) {
                    forceOnComplete();
                }
            }
        }

        private boolean isFinished() {
            return done || error != null;
        }
    }
}
