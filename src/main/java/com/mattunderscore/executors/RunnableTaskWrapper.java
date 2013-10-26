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

import java.util.concurrent.Future;

/**
 * TaskWrapper for Runnable tasks.
 * <P>
 * If any Throwable is caught it will be passed to the Future as the exception. If no exception is
 * caught the result will be set to the null reference.
 * 
 * @author Matt Champion
 * @since 0.1.0
 */
public final class RunnableTaskWrapper implements ITaskWrapper
{
    private final Runnable task;
    private final ISettableFuture<Void> future;

    /**
     * Create a task wrapper from a Runnable task.
     * @param task The Runnable task
     * @param future The Future to pass the result to
     */
    /*package*/ RunnableTaskWrapper(final Runnable task, final ISettableFuture<Void> future)
    {
        this.task = task;
        this.future = future;
        this.future.setTask(this);
    }

    @Override
    public void execute()
    {
        try
        {
            task.run();
            future.setResult(null);
        }
        catch (Throwable t)
        {
            future.setException(t);
        }
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

    @Override
    public Future<?> getFuture()
    {
        return future;
    }
}
