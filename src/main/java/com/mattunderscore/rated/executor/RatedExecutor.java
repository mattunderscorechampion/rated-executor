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

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
public class RatedExecutor implements IRatedExecutor
{
    private final ScheduledExecutorService service;
    private final long rate;
    private final TimeUnit unit;
    private ScheduledFuture<?> thisTask;
    private ScheduledFuture<?> stoppingTask;
    private Queue<BaseFuture<?>> taskQueue;
    private volatile boolean running;
    private volatile TaskWrapper executingTask;

    /**
     * Create a new RatedExecutor that will execute tasks at a fixed rate.
     * 
     * @param rate
     *            The rate value
     * @param unit
     *            The rate units
     */
    public RatedExecutor(final long rate, final TimeUnit unit)
    {
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.rate = rate;
        this.unit = unit;
        this.taskQueue = new LinkedBlockingQueue<BaseFuture<?>>();
        this.running = false;
    }

    /**
     * Create a new RatedExecutor that will execute tasks at a fixed rate.
     * <P>
     * The thread will be constructed with the thread factory.
     * 
     * @param period
     *            The rate value
     * @param unit
     *            The rate units
     * @param threadFactory
     *            A thread factory to construct the executor thread
     */
    public RatedExecutor(final long period, final TimeUnit unit, final ThreadFactory threadFactory)
    {
        this.service = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.rate = period;
        this.unit = unit;
        this.taskQueue = new LinkedBlockingQueue<BaseFuture<?>>();
        this.running = false;
    }

    /**
     * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
     */
    @Override
    public void execute(final Runnable command)
    {
        submit(command);
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#submit(java.lang.Runnable)
     */
    @Override
    public Future<?> submit(final Runnable task)
    {
        RatedSingleFuture<Object> wrapper = new RatedSingleFuture<Object>(this, task);
        synchronized (this)
        {
            if (stoppingTask != null)
            {
                stoppingTask.cancel(false);
            }
            taskQueue.add(wrapper);
            if (!running)
            {
                start();
            }
        }
        return wrapper;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#submit(java.util.concurrent.Callable)
     */
    @Override
    public <V> Future<V> submit(final Callable<V> task)
    {
        RatedSingleFuture<V> wrapper = new RatedSingleFuture<V>(this, task);
        synchronized (this)
        {
            if (stoppingTask != null)
            {
                stoppingTask.cancel(false);
            }
            taskQueue.add(wrapper);
            if (!running)
            {
                start();
            }
        }
        return wrapper;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#schedule(java.lang.Runnable)
     */
    @Override
    public Future<?> schedule(final Runnable task)
    {
        RatedUnboundedRunnableFuture wrapper = new RatedUnboundedRunnableFuture(this, task);
        synchronized (this)
        {
            if (stoppingTask != null)
            {
                stoppingTask.cancel(false);
            }
            taskQueue.add(wrapper);
            if (!running)
            {
                start();
            }
        }
        return wrapper;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#schedule(java.lang.Runnable, int)
     */
    @Override
    public Future<?> schedule(final Runnable task, final int repetitions)
    {
        RatedRepeatingFuture<Object> wrapper = new RatedRepeatingFuture<Object>(this, task, repetitions);
        synchronized (this)
        {
            if (stoppingTask != null)
            {
                stoppingTask.cancel(false);
            }
            taskQueue.add(wrapper);
            if (!running)
            {
                start();
            }
        }
        return wrapper;
    }

    /**
     * @see com.mattunderscore.rated.executor.IRatedExecutor#schedule(java.util.concurrent.Callable, int)
     */
    @Override
    public <V> IRepeatingFuture<V> schedule(final Callable<V> task, final int repetitions)
    {
        RatedRepeatingFuture<V> wrapper = new RatedRepeatingFuture<V>(this, task, repetitions);
        synchronized (this)
        {
            if (stoppingTask != null)
            {
                stoppingTask.cancel(false);
            }
            taskQueue.add(wrapper);
            if (!running)
            {
                start();
            }
        }
        return wrapper;
    }

    /**
     * Begin trying to consume tasks from the queue.
     */
    private synchronized void start()
    {
        if (running)
        {
            return;
        }
        else
        {
            thisTask = service.scheduleAtFixedRate(new ExecutingTask(), 0, rate, unit);
            running = true;
        }
    }

    /**
     * Runnable that consumes tasks from the queue and runs them.
     * 
     * @author Matt Champion
     */
    private class ExecutingTask implements Runnable
    {
        @Override
        public void run()
        {
            BaseFuture<?> taskWrapper = taskQueue.poll();
            if (taskWrapper == null)
            {
                return;
            }
            executingTask = taskWrapper;
            taskWrapper.execute();
            executingTask = null;
            if (!taskWrapper.isDone())
            {
                taskQueue.add(taskWrapper);
            }
            else
            {
                synchronized (RatedExecutor.this)
                {
                    if (taskQueue.isEmpty())
                    {
                        stoppingTask = service.schedule(new StoppingTask(), rate, unit);
                    }
                }
            }
        }
    }

    /**
     * Runnable that halts the scheduled task that consumes rated tasks.
     * 
     * @author Matt Champion
     */
    private class StoppingTask implements Runnable
    {
        @Override
        public void run()
        {
            synchronized (RatedExecutor.this)
            {
                if (running)
                {
                    thisTask.cancel(false);
                    running = false;
                }
            }
        }
    }

    /**
     * Cancel the task passed in.
     * @param wrapper The task to cancel
     * @param mayInterruptIfRunning Interrupt the thread if running
     * @return Was the task cancelled
     */
    /*package*/ boolean cancelTask(TaskWrapper wrapper, boolean mayInterruptIfRunning)
    {
        if (executingTask == wrapper)
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
