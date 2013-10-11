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

/**
 * A complete {@link SingleFuture} for the {@link RatedExecutor}.
 * <P>
 * Supports both {@link Runnable} and {@link Callable} tasks. 
 *
 * @author Matt Champion
 * @param <V>
 * @since 0.1.0
 */
/*package*/ final class RatedSingleFuture<V> extends SingleFuture<V> implements TaskWrapper
{
    /**
     * The executor the future belongs to.
     */
    private final RatedExecutor ratedExecutor;
    /**
     * The callable task
     */
    private final Callable<V> callableTask;
    /**
     * The runnable task
     */
    private final Runnable runnableTask;

    /**
     * Constructor for a Rated Single Future from a runnable task.
     * @param ratedExecutor The rated executor
     * @param runnableTask The task
     */
    public RatedSingleFuture(final RatedExecutor ratedExecutor, final Runnable task)
    {
        super();
        this.ratedExecutor = ratedExecutor;
        this.runnableTask = task;
        this.callableTask = null;
    }

    /**
     * Constructor for a Rated Single Future from a runnable task.
     * @param ratedExecutor The rated executor
     * @param runnableTask The task
     */
    public RatedSingleFuture(final RatedExecutor ratedExecutor, final Callable<V> task)
    {
        super();
        this.ratedExecutor = ratedExecutor;
        this.callableTask = task;
        this.runnableTask = null;
    }

    @Override
    protected boolean performCancellation(boolean mayInterruptIfRunning)
    {
        return ratedExecutor.cancelTask(this,mayInterruptIfRunning);
    }

    @Override
    public int hashCode()
    {
        if (runnableTask == null)
        {
            return callableTask.hashCode();
        }
        else
        {
            return runnableTask.hashCode();
        }
    }

    @Override
    public void execute()
    {
        try
        {
            if (runnableTask == null)
            {
                V result = callableTask.call();
                setResult(result);
            }
            else
            {
                runnableTask.run();
                setResult(null);
            }
        }
        catch (Throwable t)
        {
            setException(t);
        }
    }
}
