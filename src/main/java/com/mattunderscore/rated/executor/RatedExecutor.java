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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A rated executor. It will execute tasks at a fixed rate.
 * <P>
 * When a {@link Runnable} task is submitted it is placed on a queue. Tasks will be removed from the
 * queue at a fixed rate. A repeating task will be placed back on the queue when complete. If a
 * repeating task is placed on the queue it will be executed at an interval equal to the rate of the
 * executor. If two repeating tasks are added to the executor each task will execute at an interval
 * twice the executor rate and their execution will be separated from each other by an interval
 * equal to the rate of the executor. This executor is single threaded.
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
    private Queue<RunnableWrapper> taskQueue;
    private volatile boolean running;
    private volatile RunnableWrapper executingTask;

    /**
     * Create a new RatedExecutor that will execute tasks at a fixed rate.
     * 
     * @param rate
     * @param unit
     */
    public RatedExecutor(final long rate, final TimeUnit unit)
    {
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.rate = rate;
        this.unit = unit;
        this.taskQueue = new LinkedBlockingQueue<RunnableWrapper>();
        this.running = false;
    }

    /**
     * Create a new RatedExecutor that will execute tasks at a fixed rate.
     * <P>
     * The thread will be constructed with the thread factory.
     * 
     * @param period
     * @param unit
     * @param threadFactory
     */
    public RatedExecutor(final long period, final TimeUnit unit, final ThreadFactory threadFactory)
    {
        this.service = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.rate = period;
        this.unit = unit;
        this.taskQueue = new LinkedBlockingQueue<RunnableWrapper>();
        this.running = false;
    }

    /**
     * @param task
     * @return
     * @see com.mattunderscore.rated.executor.IRatedExecutor#submit(java.lang.Runnable)
     */
    @Override
    public Future<?> submit(final Runnable task)
    {
        RunnableWrapper wrapper = new RunnableWrapper(task, false);
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
     * @param task
     * @return
     * @see com.mattunderscore.rated.executor.IRatedExecutor#schedule(java.lang.Runnable)
     */
    @Override
    public Future<?> schedule(final Runnable task)
    {
        RunnableWrapper wrapper = new RunnableWrapper(task, true);
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
     * Being trying to consume tasks from the queue.
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
            RunnableWrapper taskWrapper = taskQueue.poll();
            if (taskWrapper == null)
            {
                return;
            }
            executingTask = taskWrapper;
            taskWrapper.task.run();
            executingTask = null;
            if (taskWrapper.repeat)
            {
                taskQueue.add(taskWrapper);
            }
            else
            {
                synchronized (taskWrapper)
                {
                    taskWrapper.done = true;
                }
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
     * Future that will be returned and placed on the queue.
     * <P>
     * 
     * @author Matt Champion
     */
    private class RunnableWrapper implements Future<Object>
    {
        private final Runnable task;
        private final boolean repeat;
        private boolean done;
        private boolean cancelled;

        public RunnableWrapper(final Runnable task, final boolean repeat)
        {
            this.task = task;
            this.repeat = repeat;
            this.done = false;
            this.cancelled = false;
        }

        @Override
        public synchronized boolean cancel(final boolean mayInterruptIfRunning)
        {
            if (done)
            {
                return false;
            }
            else
            {
                if (executingTask == this)
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
                taskQueue.remove(this);
                cancelled = true;
                return true;
            }
        }

        @Override
        public synchronized boolean isCancelled()
        {
            return cancelled;
        }

        @Override
        public synchronized boolean isDone()
        {
            if (cancelled)
            {
                return true;
            }
            else if (repeat)
            {
                return false;
            }
            else
            {
                return done;
            }
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException
        {
            // Only runnables passed in
            return null;
        }

        @Override
        public Object get(final long timeout, final TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException
        {
            // Only runnables passed in
            return null;
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
    }
}
