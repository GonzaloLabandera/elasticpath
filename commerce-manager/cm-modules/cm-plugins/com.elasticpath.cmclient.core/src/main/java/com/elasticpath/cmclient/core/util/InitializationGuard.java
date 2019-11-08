/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Guards the initialization of a resource, ensuring only-once initialization when multiple threads are inolved.
 * Use {code}#await(){code} to access the resource, waiting for initializtion to complete if needed.
 */
public class InitializationGuard {

    private final CountDownLatch loadedAndReady = new CountDownLatch(1);
    private final AtomicBoolean initStarted = new AtomicBoolean(false);

    /**
     * Delegate to the param to do the initialization, guarding the call.
     *
     * @param initializationCallable the code that does the initialization.
     */
    public void initialize(final Runnable initializationCallable) {
        if (!initStarted.getAndSet(true)) {
            try {
                initializationCallable.run();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            loadedAndReady.countDown();
        }
    }

    /**
     * Blocks until the initialization is done.
     */
    public void await() {
        while (true) {
            try {
                loadedAndReady.await();
                return;
            } catch (InterruptedException ie) {
                // noop
            }
        }
    }
}