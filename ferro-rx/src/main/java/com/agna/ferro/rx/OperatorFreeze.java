/*
 * Copyright 2016 Maxim Tuev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agna.ferro.rx;


import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func2;
import rx.observers.SerializedSubscriber;

/**
 * This operator freezes all rx events (onNext, onError, onComplete) when freeze selector emits true,
 * and unfreeze it after freeze selector emits false.
 * If freeze selector does not emit any elements, all events would be frozen
 * If you want reduce num of elements in freeze buffer, you can define replaceFrozenEventPredicate.
 * When Observable frozen and source observable emits normal (onNext) event, before it is added to
 * the end of buffer, it compare with all already buffered events using replaceFrozenEventPredicate,
 * and if replaceFrozenEventPredicate return true, buffered element would be removed.
 *
 * Observable after this operator can emit event in different threads
 */

public class OperatorFreeze<T> implements Observable.Operator<T, T> {

    private final Observable<Boolean> freezeSelector;
    private final Func2<T, T, Boolean> replaceFrozenEventPredicate;

    public OperatorFreeze(Observable<Boolean> freezeSelector,
                          Func2<T, T, Boolean> replaceFrozenEventPredicate) {
        this.freezeSelector = freezeSelector;
        this.replaceFrozenEventPredicate = replaceFrozenEventPredicate;
    }

    public OperatorFreeze(Observable<Boolean> freezeSelector) {
        this(freezeSelector, new Func2<T, T, Boolean>() {
            @Override
            public Boolean call(T frozenEvent, T newEvent) {
                return false;
            }
        });

    }


    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {

        final FreezeSubscriber<T> freezeSubscriber = new FreezeSubscriber<>(
                new SerializedSubscriber<>(child),
                replaceFrozenEventPredicate);

        final Subscriber<Boolean> freezeSelectorSubscriber = new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                freezeSubscriber.forceOnComplete();
            }

            @Override
            public void onError(Throwable e) {
                freezeSubscriber.forceOnError(e);
            }

            @Override
            public void onNext(Boolean freeze) {
                freezeSubscriber.setFrozen(freeze);
            }
        };
        child.add(freezeSubscriber);
        child.add(freezeSelectorSubscriber);
        freezeSelector.unsafeSubscribe(freezeSelectorSubscriber);

        return freezeSubscriber;

    }


    private static final class FreezeSubscriber<T> extends Subscriber<T> {

        private final Subscriber<T> child;
        private final Func2<T, T, Boolean> replaceFrozenEventPredicate;
        private final List<T> frozenEventsBuffer = new LinkedList<>();

        private boolean frozen = true;
        private boolean done = false;
        private Throwable error = null;

        public FreezeSubscriber(Subscriber<T> child, Func2<T, T, Boolean> replaceFrozenEventPredicate) {
            this.child = child;
            this.replaceFrozenEventPredicate = replaceFrozenEventPredicate;
        }

        @Override
        public void onCompleted() {
            if (done || error != null) {
                return;
            }
            synchronized (this) {
                if (frozen) {
                    done = true;
                } else {
                    child.onCompleted();
                    unsubscribe();
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            if (done || error != null) {
                return;
            }
            synchronized (this) {
                if (frozen) {
                    error = e;
                } else {
                    child.onError(e);
                    unsubscribe();
                }
            }
        }

        @Override
        public void onNext(T event) {
            if (done || error != null) {
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
                    if (replaceFrozenEventPredicate.call(frozenEvent, event)) {
                        it.remove();
                    }
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    unsubscribe();
                    onError(ex);
                    return;
                }
            }
            frozenEventsBuffer.add(event);
        }

        public void forceOnComplete() {
            child.onCompleted();
            unsubscribe();
        }

        public void forceOnError(Throwable e) {
            child.onError(e);
            unsubscribe();
        }

        public synchronized void setFrozen(boolean frozen) {
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
    }
}
