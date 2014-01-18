/* Copyright © 2014 Matthew Champion
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A task wrapper factory. That keeps a list of all the TestTaskWrappers so they can be accessed
 * from the tests.
 * @author Matt Champion
 */
public class TestTaskWrapperFactory implements ITaskWrapperFactory
{
    @SuppressWarnings("rawtypes")
    private final List<TestTaskWrapper> wrappers = new ArrayList<TestTaskWrapper>();

    @Override
    public <V> ITaskWrapper newWrapper(final Callable<V> task)
    {
        return add(task, new DiscardResult<V>());
    }

    @Override
    public ITaskWrapper newWrapper(final Runnable task)
    {
        return add(new RunnableWrapper(task), DiscardResult.VOID_DISCARDER);
    }

    @Override
    public <V> ITaskWrapper newWrapper(final Callable<V> task, final ITaskResultProcessor<V> processor)
    {
        return add(task, processor);
    }

    @Override
    public ITaskWrapper newWrapper(final Runnable task, final ITaskResultProcessor<Void> processor)
    {
        return add(new RunnableWrapper(task), processor);
    }

    /**
     * Create the TestTaskWrapper and add to the list of wrappers created.
     * @param task
     * @param processor
     * @return
     */
    private <V> ITaskWrapper add(final Callable<V> task, final ITaskResultProcessor<V> processor)
    {
        final TestTaskWrapper<V> wrapper = new TestTaskWrapper<V>(task, processor);
        wrappers.add(wrapper);
        return wrapper;
    }

    @SuppressWarnings("rawtypes")
    public synchronized TestTaskWrapper getWrapper(final int i)
    {
        return wrappers.get(i);
    }

    /**
     * The time between the start of the execution of two tasks. Assumes that both have completed.
     * @param firstTask The earliest task.
     * @param firstExecution The earliest execution.
     * @param secondTask The later task.
     * @param secondExecution The later execution.
     * @return The time in nanos.
     */
    public long timeBetween(final int firstTask, final int firstExecution, final int secondTask, final int secondExecution)
    {
        final long mostRecent = startTimestamp(secondTask, secondExecution);
        final long leastRecent = startTimestamp(firstTask, firstExecution);
        return mostRecent - leastRecent;
    }

    /**
     * The starting timestamp of the execution of a task. Assumes that is has completed.
     * @param task The task.
     * @param execution The execution.
     * @return The nano timestamp.
     */
    public long startTimestamp(final int task, final int execution)
    {
        @SuppressWarnings("rawtypes")
        final TestTaskWrapper wrapper = getWrapper(task);
        return wrapper.getStartTimestamp(execution);
    }

    /**
     * Wait for a task to complete.
     * @param task The task wrapper.
     * @param execution The number of task executions.
     * @param maxMilliseconds The maximum timeout.
     */
    public void waitForTask(final int task, final int execution, final long maxMilliseconds)
    {
        try
        {
            @SuppressWarnings("rawtypes")
            final TestTaskWrapper wrapper = getWrapper(task);
            for (int i = 0; i <= execution; i++)
            {
                wrapper.getLatch(i).await(maxMilliseconds, TimeUnit.MILLISECONDS);
            }
        }
        catch (final InterruptedException ex)
        {
            throw new AssertionError(ex);
        }
    }
}
