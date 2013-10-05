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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
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
    private Queue<TaskWrapper<?>> taskQueue;
    private volatile boolean running;
    private volatile TaskWrapper<?> executingTask;

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
        this.taskQueue = new LinkedBlockingQueue<TaskWrapper<?>>();
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
        this.taskQueue = new LinkedBlockingQueue<TaskWrapper<?>>();
        this.running = false;
    }

    @Override
    public void execute(final Runnable command)
    {
        submit(command);
    }

    /**
     * @param task
     * @return
     * @see com.mattunderscore.rated.executor.IRatedExecutor#submit(java.lang.Runnable)
     */
    @Override
    public Future<?> submit(final Runnable task)
    {
        TaskWrapper<?> wrapper = new RunnableWrapper(task, 1);
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

    @Override
    public <V> Future<V> submit(final Callable<V> task)
    {
        TaskWrapper<V> wrapper = new CallableWrapper<V>(task, 1);
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
        TaskWrapper<?> wrapper = new RunnableWrapper(task, -1);
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

    @Override
    public Future<?> schedule(final Runnable task, final int repetitions)
    {
        TaskWrapper<?> wrapper = new RunnableWrapper(task, repetitions);
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
            TaskWrapper<?> taskWrapper = taskQueue.poll();
            if (taskWrapper == null)
            {
                return;
            }
            executingTask = taskWrapper;
            taskWrapper.execute();
            executingTask = null;
            if (taskWrapper.repetitions != 0)
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
     * Wrapper for tasks. Also serves as a Future.
     * 
     * @author Matt Champion
     * @param <V>
     *            Type of return value
     */
    private abstract class TaskWrapper<V> implements Future<V>
    {
        private volatile int repetitions;
        private volatile boolean done = false;
        private volatile boolean cancelled = false;
        private volatile ExecutionException exception;
        private volatile V result;
        private final CountDownLatch latch = new CountDownLatch(1);

        public TaskWrapper(final int repetitions)
        {
            this.repetitions = repetitions;
        }

        /**
         * Set the result of the task execution
         * @param result
         */
        protected void setResult(V result)
        {
            this.result = result;
            this.done = true;
            int reps = repetitions;
            if (reps > 0)
            {
                repetitions = reps - 1;
            }
            latch.countDown();
        }

        /**
         * Set the exception of the task
         * @param exception
         */
        protected void setException(ExecutionException exception)
        {
            this.exception = exception;
            this.done = true;
            int reps = repetitions;
            if (reps > 0)
            {
                repetitions = reps - 1;
            }
            latch.countDown();
        }

        /**
         * Check if get needs to throw an exception
         * @throws ExecutionException
         */
        private void checkException() throws ExecutionException
        {
            if (cancelled)
            {
                throw new CancellationException();
            }
            else
            {
                ExecutionException theException = exception;
                if (theException != null)
                {
                    throw theException;
                }
            }
        }

        @Override
        public V get() throws InterruptedException, ExecutionException
        {
            latch.await();
            checkException();
            return result;
        }

        @Override
        public V get(final long timeout, final TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException
        {
            latch.await(timeout, unit);
            checkException();
            return result;
        }

        @Override
        public synchronized boolean cancel(final boolean mayInterruptIfRunning)
        {
            if (done || cancelled)
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
                latch.countDown();
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
            else if (repetitions != 0)
            {
                return false;
            }
            else
            {
                return done;
            }
        }

        @Override
        public boolean equals(Object object)
        {
            return this == object;
        }

        /**
         * Execute the task
         */
        public abstract void execute();
    }

    /**
     * TaskWrapper for Runnable tasks.
     * 
     * @author Matt Champion
     */
    private class RunnableWrapper extends TaskWrapper<Object>
    {
        private final Runnable task;

        public RunnableWrapper(final Runnable task, final int repetitions)
        {
            super(repetitions);
            this.task = task;
        }

        @Override
        public int hashCode()
        {
            return task.hashCode();
        }

        @Override
        public void execute()
        {
            try
            {
                task.run();
                setResult(null);
            }
            catch (Throwable t)
            {
                setException(new ExecutionException(t));
            }
        }
    }

    /**
     * TaskWrapper for Callable tasks.
     * 
     * @author Matt Champion
     * @param <V>
     *            Type returned by call
     */
    private class CallableWrapper<V> extends TaskWrapper<V>
    {
        private final Callable<V> task;

        public CallableWrapper(final Callable<V> task, final int repetitions)
        {
            super(repetitions);
            this.task = task;
        }

        @Override
        public int hashCode()
        {
            return task.hashCode();
        }

        @Override
        public void execute()
        {
            try
            {
                V result = task.call();
                setResult(result);
            }
            catch (Throwable t)
            {
                setException(new ExecutionException(t));
            }
        }
    }
}
