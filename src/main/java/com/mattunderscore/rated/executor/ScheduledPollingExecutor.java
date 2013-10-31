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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.GuardedBy;

import com.mattunderscore.executors.ITaskWrapper;

/**
 * {@link PollingExecutor} implementation based on the {@link ScheduledExecutorService}.
 * 
 * @author Matt Champion
 * @since 0.1.1
 */
/*package*/ final class ScheduledPollingExecutor implements PollingExecutor
{
    private final long rate;
    private final TimeUnit unit;
    private final TaskQueue taskQueue;
    private final ScheduledExecutorService service;
    // thisTask is always accessed within a synchronised block
    @GuardedBy(value = "this")
    private ScheduledFuture<?> thisTask;
    // stoppingTask is always accessed within a synchronised block
    @GuardedBy(value = "this")
    private ScheduledFuture<?> stoppingTask;
    // running is always accessed within a synchronised block
    @GuardedBy(value = "this")
    private boolean running = false;

    /*package*/ ScheduledPollingExecutor(final TaskQueue taskQueue, final long rate, final TimeUnit unit)
    {
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.taskQueue = taskQueue;
        this.rate = rate;
        this.unit = unit;
    }

    /*package*/ ScheduledPollingExecutor(final TaskQueue taskQueue, final long rate, final TimeUnit unit,
            final ThreadFactory threadFactory)
    {
        this.service = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.taskQueue = taskQueue;
        this.rate = rate;
        this.unit = unit;
    }

    @Override
    public synchronized void submit(final ITaskWrapper wrapper)
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

    /**
     * Start the execution of the queued tasks.
     */
    private void start()
    {
        if (running)
        {
            return;
        }
        else
        {
            thisTask = service.scheduleAtFixedRate(new ExecutingTask(taskQueue, this), 0, rate,
                    unit);
            running = true;
        }
    }

    @Override
    public synchronized void requestStop()
    {
        if (taskQueue.isEmpty())
        {
            stoppingTask = service.schedule(new StoppingTask(), rate, unit);
        }
    }

    @Override
    public synchronized void stop()
    {
        if (running)
        {
            thisTask.cancel(false);
            running = false;
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
            stop();
        }
    }
}
