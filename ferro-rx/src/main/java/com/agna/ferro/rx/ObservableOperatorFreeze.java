package com.agna.ferro.rx;


import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.SingleOperator;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.BiFunction;
import io.reactivex.internal.disposables.ArrayCompositeDisposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.observers.SerializedObserver;

/**
 * This operator freezes Observable events (onNext, onError, onComplete) when freeze selector emits true,
 * and unfreeze it after freeze selector emits false.
 * If freeze selector does not emit any elements, all events would be frozen
 * If you want reduce num of elements in freeze buffer, you can define replaceFrozenEventPredicate.
 * When Observable frozen and source observable emits normal (onNext) event, before it is added to
 * the end of buffer, it compare with all already buffered events using replaceFrozenEventPredicate,
 * and if replaceFrozenEventPredicate return true, buffered element would be removed.
 *
 * Observable after this operator can emit event in different threads
 * You should pass this operator in method {@link io.reactivex.Observable#lift(ObservableOperator)}
 * for apply it
 */
public class ObservableOperatorFreeze<T> implements ObservableOperator<T, T> {

    private final Observable<Boolean> freezeSelector;
    private final BiFunction<T, T, Boolean> replaceFrozenEventPredicate;

    public ObservableOperatorFreeze(Observable<Boolean> freezeSelector,
                          BiFunction<T, T, Boolean> replaceFrozenEventPredicate) {
        this.freezeSelector = freezeSelector;
        this.replaceFrozenEventPredicate = replaceFrozenEventPredicate;
    }

    public ObservableOperatorFreeze(Observable<Boolean> freezeSelector) {
        this(freezeSelector, new BiFunction<T, T, Boolean>() {
            @Override
            public Boolean apply(T frozenEvent, T newEvent) {
                return false;
            }
        });
    }

    @Override
    public Observer<? super T> apply(Observer<? super T> child) throws Exception {
        return new FreezeObserver<>(
                new SerializedObserver<>(child),
                replaceFrozenEventPredicate,
                freezeSelector);
    }

    private static final class FreezeObserver<T> implements Observer<T> {

        private final Observer<T> child;
        private final BiFunction<T, T, Boolean> replaceFrozenEventPredicate;
        private final Observable<Boolean> freezeSelector;
        private final List<T> frozenEventsBuffer = new LinkedList<>();

        private final ArrayCompositeDisposable compositeDisposable = new ArrayCompositeDisposable(2);
        private Disposable s;

        private boolean frozen = true;
        private boolean done = false;
        private Throwable error = null;

        private FreezeObserver(Observer<T> child,
                               BiFunction<T, T, Boolean> replaceFrozenEventPredicate,
                               Observable<Boolean> freezeSelector) {
            this.child = child;
            this.replaceFrozenEventPredicate = replaceFrozenEventPredicate;
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
        public void onNext(T event) {
            if (isFinished()) {
                return;
            }
            synchronized (this) {
                if (frozen) {
                    bufferEvent(event);
                } else {
                    child.onNext(event);
                }
            }
        }

        private void bufferEvent(T event) {
            for (ListIterator<T> it = frozenEventsBuffer.listIterator(); it.hasNext(); ) {
                T frozenEvent = it.next();
                try {
                    if (replaceFrozenEventPredicate.apply(frozenEvent, event)) {
                        it.remove();
                    }
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    forceOnError(ex);
                    return;
                }
            }
            frozenEventsBuffer.add(event);
        }

        private void forceOnComplete() {
            compositeDisposable.dispose();
            child.onComplete();
        }

        private void forceOnError(Throwable e) {
            compositeDisposable.dispose();
            child.onError(e);
        }

        private synchronized void setFrozen(boolean frozen) {
            this.frozen = frozen;
            if (!frozen) {
                emitFrozenEvents();
                if (error != null) {
                    forceOnError(error);
                }
                if (done) {
                    forceOnComplete();
                }
            }
        }

        private void emitFrozenEvents() {
            for (T event : frozenEventsBuffer) {
                child.onNext(event);
            }
            frozenEventsBuffer.clear();
        }

        private boolean isFinished() {
            return done || error != null;
        }
    }
}

