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
import java.util.concurrent.RunnableFuture;

/**
 * Utility class for the construction of Future and ITaskWrapper object pairs.
 * @author Matt Champion
 */
public final class Futures
{
    private Futures()
    {
    }

    /**
     * Provides a constructor for an entwined task wrapper and future.
     * <P>
     * This allows the task wrapper and future to be created as a pair. This is desirable as they
     * cannot be completely decoupled but they do have separate responsibilities.
     * 
     * @param canceller
     * @param task
     * @return Tuple of task wrapper and future
     */
    public static RunnableFuture<Void> createTaskAndFuture(
            final ITaskCanceller canceller, final Runnable task)
    {
        final SingleFuture<Void> future = new SingleFuture<Void>(canceller);
        new RunnableTaskWrapper(task, future);
        return future;
    }

    /**
     * Provides a constructor for an entwined task wrapper and future.
     * <P>
     * This allows the task wrapper and future to be created as a pair. This is desirable as they
     * cannot be completely decoupled but they do have separate responsibilities.
     * 
     * @param canceller
     * @param task
     * @return Tuple of task wrapper and future
     */
    public static <V> RunnableFuture<V> createTaskAndFuture(
            final ITaskCanceller canceller, final Callable<V> task)
    {
        final SingleFuture<V> future = new SingleFuture<V>(canceller);
        new CallableTaskWrapper<V>(task, future);
        return future;
    }

    /**
     * Provides a constructor for an entwined task wrapper and future.
     * <P>
     * This allows the task wrapper and future to be created as a pair. This is desirable as they
     * cannot be completely decoupled but they do have separate responsibilities.
     * 
     * @param canceller
     * @param task
     * @return Tuple of task wrapper and future
     */
    public static IRunnableRepeatingFuture<Void> createTaskAndFuture(
            final ITaskCanceller canceller, final Runnable task, final int repetitions)
    {
        final IRunnableRepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, repetitions);
        new RunnableTaskWrapper(task, future);
        return future;
    }

    /**
     * Provides a constructor for an entwined task wrapper and future.
     * <P>
     * This allows the task wrapper and future to be created as a pair. This is desirable as they
     * cannot be completely decoupled but they do have separate responsibilities.
     * 
     * @param canceller
     * @param task
     * @return Tuple of task wrapper and future
     */
    public static <V> IRunnableRepeatingFuture<V> createTaskAndFuture(
            final ITaskCanceller canceller, final Callable<V> task, final int repetitions)
    {
        final RepeatingFuture<V> future = new RepeatingFuture<V>(canceller, repetitions);
        new CallableTaskWrapper<V>(task, future);
        return future;
    }

    /**
     * Provides a constructor for an entwined task wrapper and future.
     * <P>
     * This allows the task wrapper and future to be created as a pair. This is desirable as they
     * cannot be completely decoupled but they do have separate responsibilities.
     * 
     * @param canceller
     * @param task
     * @return Tuple of task wrapper and future
     */
    public static RunnableFuture<Void> createTaskAndUnboundedFuture(
            final ITaskCanceller canceller, final Runnable task)
    {
        final UnboundedFuture future = new UnboundedFuture(canceller);
        new RunnableTaskWrapper(task, future);
        return future;
    }
}
