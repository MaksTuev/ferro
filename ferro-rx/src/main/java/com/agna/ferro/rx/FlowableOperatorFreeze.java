package com.agna.ferro.rx;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.FlowableOperator;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.BiFunction;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.subscribers.SerializedSubscriber;

public class FlowableOperatorFreeze<T> implements FlowableOperator<T, T> {

    private final Observable<Boolean> freezeSelector;
    private final BiFunction<T, T, Boolean> replaceFrozenEventPredicate;

    public FlowableOperatorFreeze(Observable<Boolean> freezeSelector,
                                  BiFunction<T, T, Boolean> replaceFrozenEventPredicate) {
        this.freezeSelector = freezeSelector;
        this.replaceFrozenEventPredicate = replaceFrozenEventPredicate;
    }

    public FlowableOperatorFreeze(Observable<Boolean> freezeSelector) {
        this(freezeSelector, new BiFunction<T, T, Boolean>() {
            @Override
            public Boolean apply(T frozenEvent, T newEvent) {
                return false;
            }
        });
    }

    @Override
    public Subscriber<? super T> apply(Subscriber<? super T> child) throws Exception {
        return new FreezeObserver<>(
                new SerializedSubscriber<>(child),
                replaceFrozenEventPredicate,
                freezeSelector);
    }

    private static final class FreezeObserver<T> implements Subscriber<T>, Subscription {

        private final Subscriber<T> child;
        private final BiFunction<T, T, Boolean> replaceFrozenEventPredicate;
        private final Observable<Boolean> freezeSelector;
        private final List<T> frozenEventsBuffer = new LinkedList<>();

        private Disposable resourceDisposable = DisposableHelper.DISPOSED;
        private Subscription s;

        private boolean frozen = true;
        private boolean done = false;
        private Throwable error = null;

        private FreezeObserver(Subscriber<T> child,
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
        public void onSubscribe(Subscription s) {
            if(SubscriptionHelper.validate(this.s, s)) {
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
                        resourceDisposable = s;
                    }

                    @Override
                    public void onNext(Boolean freeze) {
                        setFrozen(freeze);
                    }
                });

                child.onSubscribe(this);
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

        @Override
        public void request(long n) {
            s.request(n);
        }

        @Override
        public void cancel() {
            s.cancel();
            resourceDisposable.dispose();
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
            resourceDisposable.dispose();
            s.cancel();
            child.onComplete();
        }

        private void forceOnError(Throwable e) {
            resourceDisposable.dispose();
            s.cancel();
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
