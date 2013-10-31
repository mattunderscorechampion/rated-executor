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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A base implementation for a {@link Future}.
 * <P>
 * This provides some support for cancellation, checking that the task is done, getting and setting
 * the result. It ensures that completed tasks cannot be cancelled. It makes sure that cancelled
 * tasks are treated as done. It helps with the setting of the result. It ensures that
 * {@link #get()} and {@link #get(long, TimeUnit)} throw the correct exceptions. Hooks are provided
 * to access the result, block until the result is set to determine if the task is done and to
 * perform the cancellation.
 * <P>
 * This is also a {@link ITaskWrapper} because the task, executor and the future are tightly
 * coupled. The task must be executed and the result passed to the future, cancellation is passed
 * from the future to the executor and the task. After the executor runs the task it must set the
 * {@link Future} result.
 * 
 * @author Matt Champion
 * @param <V>
 *            Type of the value returned by {{@link #get()}.
 * @since 0.1.0
 */
/* package */ abstract class BaseFuture<V> implements ISettableFuture<V>, RunnableFuture<V>
{
    /**
     * Has the cancelled task been cancelled.
     */
    private volatile boolean cancelled = false;
    private ITaskWrapper task;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        if (isDone())
        {
            return false;
        }
        else
        {
            cancelled = processCancellation(mayInterruptIfRunning);
            return cancelled;
        }
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public boolean isDone()
    {
        if (cancelled)
        {
            return true;
        }
        else
        {
            return taskDone();
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException, CancellationException
    {
        await();
        checkCancellationException();
        TaskExecutionResult<V> result = getResult();
        checkExecutionException(result);
        return result.result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException, CancellationException
    {
        if (!await(timeout, unit))
        {
            throw new TimeoutException();
        }
        checkCancellationException();
        TaskExecutionResult<V> result = getResult();
        checkExecutionException(result);
        return result.result;
    }

    /**
     * Checks if the {@link CancellationException} needs to be thrown.
     * 
     * @throws CancellationException
     *             If it needs to be thrown
     */
    protected void checkCancellationException() throws CancellationException
    {
        if (cancelled)
        {
            throw new CancellationException();
        }
    }

    /**
     * Checks if a {@link ExecutionException} needs to be thrown.
     * 
     * @param result
     *            The result of the task
     * @throws ExecutionException
     *             If it needs to be thrown
     */
    protected void checkExecutionException(TaskExecutionResult<V> result) throws ExecutionException
    {
        if (result.exception != null)
        {
            throw result.exception;
        }
    }

    @Override
    public void setResult(V result)
    {
        processResult(new TaskExecutionResult<V>(result));
    }

    @Override
    public void setException(Throwable result)
    {
        processResult(new TaskExecutionResult<V>(result));
    }

    @Override
    public void setTask(ITaskWrapper wrapper)
    {
        task = wrapper;
    }

    @Override
    public void run()
    {
        task.execute();
    }

    protected ITaskWrapper getTask()
    {
        return task;
    }

    @Override
    public int hashCode()
    {
        return task.hashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        return this == object;
    }

    /**
     * Process the result being set.
     * 
     * @param result
     *            The result
     */
    protected abstract void processResult(TaskExecutionResult<V> result);

    /**
     * Process cancellation of the task.
     * 
     * @param mayInterruptIfRunning
     *            If the thread should be interrupted if it is running
     * @return Was the cancellation successful
     */
    protected abstract boolean processCancellation(boolean mayInterruptIfRunning);

    /**
     * Indicate if the task has been executed.
     * 
     * @return True if the task has executed
     */
    protected abstract boolean taskDone();

    /**
     * Blocks the thread until the result is set or the task is cancelled.
     * 
     * @throws InterruptedException
     *             The thread was Interrupted while waiting
     */
    protected abstract void await() throws InterruptedException;

    /**
     * Blocks the thread until the result is set or the task is cancelled. Takes a timeout as the
     * maximum amount of time it will block.
     * 
     * @param timeout
     *            Duration of the timeout
     * @param unit
     *            Unit of the timeout
     * @return True if the timeout was not reached
     * @throws InterruptedException
     *             The thread was Interrupted while waiting
     */
    protected abstract boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Return the result of the task execution. Assumes that it is set.
     * 
     * @return The result
     */
    protected abstract TaskExecutionResult<V> getResult();
}
