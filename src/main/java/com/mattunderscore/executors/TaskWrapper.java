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

import java.util.concurrent.Callable;

/**
 * A task wrapper for {@link Callable} tasks.
 * <P>
 * The handling of the result of the task is provided by the {@link ITaskResultProcessor}.
 * @author Matt Champion
 * @param <V> The type returned by the task
 * @since 0.1.1
 */
public final class TaskWrapper<V> implements ITaskWrapper
{
    private final Callable<V> task;
    private final ITaskResultProcessor<V> processor;

    /**
     * Create the task wrapper
     * @param task The task
     * @param processor How to handle the result
     */
    public TaskWrapper(final Callable<V> task, final ITaskResultProcessor<V> processor)
    {
        this.task = task;
        this.processor = processor;
    }

    @Override
    public void execute()
    {
        try
        {
            final V result = task.call();
            processor.onResult(this, result);
        }
        catch (Throwable t)
        {
            processor.onThrowable(this, t);
        }
    }

    @Override
    public int hashCode()
    {
        return task.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
}
