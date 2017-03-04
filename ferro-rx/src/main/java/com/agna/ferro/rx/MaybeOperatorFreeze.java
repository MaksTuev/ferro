package com.agna.ferro.rx;


import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOperator;
import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.ArrayCompositeDisposable;
import io.reactivex.internal.disposables.DisposableHelper;

/**
 * This operator freezes Maybe events (onSuccess, onError, onComplete) when freeze selector emits true,
 * and unfreeze it after freeze selector emits false.
 * If freeze selector does not emit any elements, all events would be frozen
 * If you want reduce num of elements in freeze buffer, you can define replaceFrozenEventPredicate.
 * When Observable frozen and source observable emits normal (onNext) event, before it is added to
 * the end of buffer, it compare with all already buffered events using replaceFrozenEventPredicate,
 * and if replaceFrozenEventPredicate return true, buffered element would be removed.
 *
 * Maybe after this operator can emit event in different threads
 * You should pass this operator in method {@link io.reactivex.Maybe#lift(MaybeOperator)}
 * for apply it
 */
public class MaybeOperatorFreeze<T> implements MaybeOperator<T, T> {


    private final Observable<Boolean> freezeSelector;

    public MaybeOperatorFreeze(Observable<Boolean> freezeSelector) {
        this.freezeSelector = freezeSelector;
    }

    @Override
    public MaybeObserver<? super T> apply(MaybeObserver<? super T> child) throws Exception {
        return new FreezeObserver<>(child, freezeSelector);
    }

    private static final class FreezeObserver<T> implements MaybeObserver<T> {

        private final MaybeObserver<T> child;
        private final Observable<Boolean> freezeSelector;

        private final ArrayCompositeDisposable compositeDisposable = new ArrayCompositeDisposable(2);
        private Disposable s;

        private boolean frozen = true;
        private boolean done = false;
        private T successValue = null;
        private Throwable error = null;

        private FreezeObserver(MaybeObserver<T> child,
                               Observable<Boolean> freezeSelector) {
            this.child = child;
            this.freezeSelector = freezeSelector;
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
        public void onSuccess(T event) {
            if (isFinished()) {
                return;
            }
            synchronized (this) {
                successValue = event;
                if (!frozen) {
                    forceOnSuccess(successValue);
                }
            }
        }

        private void forceOnComplete() {
            compositeDisposable.dispose();
            child.onComplete();
        }

        private void forceOnSuccess(T event) {
            compositeDisposable.dispose();
            child.onSuccess(event);
        }

        private void forceOnError(Throwable e) {
            compositeDisposable.dispose();
            child.onError(e);
        }

        private synchronized void setFrozen(boolean frozen) {
            this.frozen = frozen;
            if (!frozen) {
                if (successValue != null) {
                    forceOnSuccess(successValue);
                } else if (error != null) {
                    forceOnError(error);
                } else if (done) {
                    forceOnComplete();
                }
            }
        }

        private boolean isFinished() {
            return done || error != null || successValue != null;
        }

    }
}
