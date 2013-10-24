/* Copyright © 2013 Matthew Champion
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of mattunderscore.com nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL MATTHEW CHAMPION BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.mattunderscore.executors;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation for a {@link Future} of a task that is executed once.
 * <P>
 * It stores a single result and uses a single latch to block the result.
 * @author Matt Champion
 * @param <V> Type of object returned by the {@link #get()} method
 * @since 0.1.0
 */
public final class SingleFuture<V> extends BaseFuture<V>
{
    /**
     * Has the task completed
     */
    private volatile boolean done = false;
    /**
     * The result of the execution
     */
    private volatile TaskExecutionResult<V> result;
    /**
     * Latch to block access to the result
     */
    private final CountDownLatch latch;
    private final ITaskCanceller canceller;
    private ITaskWrapper task;
    
    public SingleFuture(final ITaskCanceller canceller)
    {
        latch = new CountDownLatch(1);
        this.canceller = canceller;
    }
    
    @Override
    protected void processResult(TaskExecutionResult<V> result)
    {
        this.result = result;
        done = true;
        latch.countDown();
    }

    @Override
    protected boolean processCancellation(boolean mayInterruptIfRunning)
    {
        final boolean cancelled = canceller.cancelTask(task, mayInterruptIfRunning);
        latch.countDown();
        return cancelled;
    }

    @Override
    protected boolean taskDone()
    {
        return done;
    }

    @Override
    protected void await() throws InterruptedException
    {
        latch.await();
    }

    @Override
    protected boolean await(long timeout, TimeUnit unit) throws InterruptedException
    {
        return latch.await(timeout, unit);
    }

    @Override
    protected TaskExecutionResult<V> getResult()
    {
        return result;
    }

    @Override
    public void setTask(ITaskWrapper wrapper)
    {
        task = wrapper;
    }
}
