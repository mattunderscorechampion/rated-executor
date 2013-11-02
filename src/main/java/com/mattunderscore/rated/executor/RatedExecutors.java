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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.mattunderscore.executors.UniversalExecutor;

/**
 * Utility class that allows the construction of {@link RatedExecutor} objects.
 * 
 * @author Matt Champion
 * @since 0.1.0
 */
public final class RatedExecutors
{
    /**
     * Private constructor
     */
    private RatedExecutors()
    {
    }

    /**
     * Creates a new rated executor.
     * <P>
     * This executor is single threaded, if a task takes longer than the executor rate it will delay
     * scheduled tasks.
     * 
     * @param rate
     *            The rate of the executor
     * @param unit
     *            The time unit of the rate
     * @return The executor
     */
    public static IRatedExecutor ratedExecutor(final long rate, final TimeUnit unit)
    {
        final TaskQueue queue = new TaskQueue();
        final InternalExecutor executor = new ScheduledInternalExecutor(queue, rate, unit);
        return new RatedExecutor(queue, executor);
    }

    /**
     * Creates a new rated executor.
     * <P>
     * This executor is single threaded, if a task takes longer than the executor rate it will delay
     * scheduled tasks.
     * 
     * @param rate
     *            The rate of the executor
     * @param unit
     *            The time unit of the rate
     * @param factory
     *            The thread factory used to create the thread
     * @return The executor
     */
    public static IRatedExecutor ratedExecutor(final long rate, final TimeUnit unit,
            final ThreadFactory factory)
    {
        final TaskQueue queue = new TaskQueue();
        final InternalExecutor executor = new ScheduledInternalExecutor(queue, rate, unit, factory);
        return new RatedExecutor(queue, executor);
    }

    /**
     * Creates a new rated executor.
     * <P>
     * This provides a simple interface that allows tasks to be executed but does not return a
     * Future. This executor is single threaded, if a task takes longer than the executor rate it
     * will delay scheduled tasks.
     * 
     * @param rate
     *            The rate of the executor
     * @param unit
     *            The time unit of the rate
     * @return The executor
     */
    public static UniversalExecutor simpleRatedExecutor(final long rate, final TimeUnit unit)
    {
        final TaskQueue queue = new TaskQueue();
        final InternalExecutor executor = new ScheduledInternalExecutor(queue, rate, unit);
        return new SimpleRatedExecutor(executor);
    }

    /**
     * Creates a new rated executor.
     * <P>
     * This provides a simple interface that allows tasks to be executed but does not return a
     * Future. This executor is single threaded, if a task takes longer than the executor rate it
     * will delay scheduled tasks.
     * 
     * @param rate
     *            The rate of the executor
     * @param unit
     *            The time unit of the rate
     * @param factory
     *            The thread factory used to create the thread
     * @return The executor
     */
    public static UniversalExecutor simpleRatedExecutor(final long rate, final TimeUnit unit,
            final ThreadFactory factory)
    {
        final TaskQueue queue = new TaskQueue();
        final InternalExecutor executor = new ScheduledInternalExecutor(queue, rate, unit, factory);
        return new SimpleRatedExecutor(executor);
    }
}
