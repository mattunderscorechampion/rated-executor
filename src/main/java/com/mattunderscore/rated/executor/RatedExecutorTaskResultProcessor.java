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

import com.mattunderscore.executors.ISettableFuture;
import com.mattunderscore.executors.ITaskWrapper;
import com.mattunderscore.executors.ITaskResultProcessor;

/**
 * The task processor for the RatedExecutor sets the results on a {@link Future} and if it is not
 * done submits it back to the executor.
 * 
 * @author Matt Champion
 * @param <V> The type of the result of the task
 * @since 0.1.1
 */
/* package */final class RatedExecutorTaskResultProcessor<V> implements ITaskResultProcessor<V>
{
    private final ISettableFuture<V> future;
    private final IInternalExecutor executor;

    public RatedExecutorTaskResultProcessor(final ISettableFuture<V> future,
            final IInternalExecutor executor)
    {
        this.future = future;
        this.executor = executor;
    }

    @Override
    public void onThrowable(ITaskWrapper task, Throwable t)
    {
        future.setException(t);
        if (!future.isDone())
        {
            executor.submit(task);
        }
        else
        {
            executor.requestStop();
        }
    }

    @Override
    public void onResult(ITaskWrapper task, V result)
    {
        future.setResult(result);
        if (!future.isDone())
        {
            executor.submit(task);
        }
        else
        {
            executor.requestStop();
        }
    }
}
