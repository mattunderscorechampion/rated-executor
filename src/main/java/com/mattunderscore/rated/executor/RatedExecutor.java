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

package com.mattunderscore.rated.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.mattunderscore.executors.DiscardResult;
import com.mattunderscore.executors.IRepeatingFuture;
import com.mattunderscore.executors.ISettableFuture;
import com.mattunderscore.executors.ITaskCanceller;
import com.mattunderscore.executors.ITaskWrapper;
import com.mattunderscore.executors.RepeatingFuture;
import com.mattunderscore.executors.RunnableWrapper;
import com.mattunderscore.executors.SingleFuture;
import com.mattunderscore.executors.TaskWrapper;
import com.mattunderscore.executors.UnboundedFuture;

/**
 * A rated executor, it will execute tasks at a fixed rate.
 * <P>
 * When a {@link Runnable} or {@link Callable} task is submitted it is placed on a queue. Tasks will
 * be removed from the queue at a fixed rate. A repeating task will be placed back on the queue when
 * complete. If a single repeating task is submitted it will be executed at an interval equal to the
 * rate of the executor. If two repeating tasks are submitted to the executor each task will execute
 * at an interval twice the executor rate and their execution will be separated from each other by
 * an interval equal to the rate of the executor. This executor is single threaded, if a task takes
 * longer than the executor rate it will delay scheduled tasks.
 * 
 * @author Matt Champion
 * @since 0.0.1
 */
/* package */final class RatedExecutor implements IRatedExecutor, ITaskCanceller
{
    private final InternalExecutor executor;
    private final TaskQueue taskQueue;

    /**
     * Create a new RatedExecutor that will execute tasks at a fixed rate.
     * 
     * @param rate
     *            The rate value
     * @param unit
     *            The rate units
     */
    public RatedExecutor(final TaskQueue taskQueue, final InternalExecutor executor)
    {
        this.taskQueue = taskQueue;
        this.executor = executor;
    }

    /**
     * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
     */
    @Override
    public void execute(final Runnable task)
    {
        final Callable<Void> wrappedTask = new RunnableWrapper(task);
        final TaskWrapper<Void> thing = new TaskWrapper<Void>(wrappedTask,
                DiscardResult.voidDiscarder);
        executor.submit(thing);
    }

    @Override
    public <V> void execute(final Callable<V> task)
    {
        final TaskWrapper<V> thing = new TaskWrapper<V>(task, new DiscardResult<V>());
        executor.submit(thing);
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#submit(java.lang.Runnable)
     */
    @Override
    public Future<?> submit(final Runnable task)
    {
        final Callable<Void> wrappedTask = new RunnableWrapper(task);
        final ISettableFuture<Void> future = new SingleFuture<Void>(this);
        final RatedExecutorTaskResultProcessor<Void> processor = new RatedExecutorTaskResultProcessor<Void>(
                future, executor);
        final TaskWrapper<Void> thing = new TaskWrapper<Void>(wrappedTask, processor);
        future.setTask(thing);
        executor.submit(thing);
        return future;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#submit(java.util.concurrent.Callable)
     */
    @Override
    public <V> Future<V> submit(final Callable<V> task)
    {
        final ISettableFuture<V> future = new SingleFuture<V>(this);
        final RatedExecutorTaskResultProcessor<V> processor = new RatedExecutorTaskResultProcessor<V>(
                future, executor);
        final TaskWrapper<V> thing = new TaskWrapper<V>(task, processor);
        future.setTask(thing);
        executor.submit(thing);
        return future;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#schedule(java.lang.Runnable)
     */
    @Override
    public Future<?> schedule(final Runnable task)
    {
        final Callable<Void> wrappedTask = new RunnableWrapper(task);
        final ISettableFuture<Void> future = new UnboundedFuture(this);
        final RatedExecutorTaskResultProcessor<Void> processor = new RatedExecutorTaskResultProcessor<Void>(
                future, executor);
        final TaskWrapper<Void> thing = new TaskWrapper<Void>(wrappedTask, processor);
        future.setTask(thing);
        executor.submit(thing);
        return future;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#schedule(java.lang.Runnable, int)
     */
    @Override
    public IRepeatingFuture<?> schedule(final Runnable task, final int repetitions)
    {
        final Callable<Void> wrappedTask = new RunnableWrapper(task);
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(this, repetitions);
        final RatedExecutorTaskResultProcessor<Void> processor = new RatedExecutorTaskResultProcessor<Void>(
                future, executor);
        final TaskWrapper<Void> thing = new TaskWrapper<Void>(wrappedTask, processor);
        future.setTask(thing);
        executor.submit(thing);
        return future;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#schedule(java.util.concurrent.Callable,
     *      int)
     */
    @Override
    public <V> IRepeatingFuture<V> schedule(final Callable<V> task, final int repetitions)
    {
        final RepeatingFuture<V> future = new RepeatingFuture<V>(this, repetitions);
        final RatedExecutorTaskResultProcessor<V> processor = new RatedExecutorTaskResultProcessor<V>(
                future, executor);
        final TaskWrapper<V> thing = new TaskWrapper<V>(task, processor);
        future.setTask(thing);
        executor.submit(thing);
        return future;
    }

    @Override
    public boolean cancelTask(final ITaskWrapper wrapper, final boolean mayInterruptIfRunning)
    {
        if (taskQueue.isCurrentTask(wrapper))
        {
            if (mayInterruptIfRunning)
            {
                // TODO: interrupt
                return false;
            }
            else
            {
                return false;
            }
        }
        taskQueue.remove(wrapper);
        return true;
    }
}
